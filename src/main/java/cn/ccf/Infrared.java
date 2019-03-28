package cn.ccf;

import cn.ccf.bean.Person;
import cn.ccf.capture.HCNetSDK;
import cn.ccf.common.Constant;
import cn.ccf.common.ErrorExecution;
import cn.ccf.common.SerialPortManager;
import cn.ccf.common.SystemConf;
import com.sun.jna.NativeLong;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Infrared implements Runnable, SerialPortEventListener {


    static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
    NativeLong lUserID;
    HCNetSDK.NET_DVR_CLIENTINFO m_strClientInfo;
    HCNetSDK.NET_DVR_JPEGPARA lJpegPara;

    private Person person;

    private Integer infraredCount = 0;

    private SerialPort serialPort;

    private static final Logger LOGGER = LoggerFactory.getLogger(Infrared.class);

    public Infrared(Person person, SerialPort serialPort) {
        this.serialPort = serialPort;
        this.person = person;

        //进行抓拍硬件初始化
        boolean initSuc = hCNetSDK.NET_DVR_Init();

        String m_sDeviceIP = SystemConf.get("capture.dvrIP");
        int iPort = Integer.parseInt(SystemConf.get("capture.dvrPort"));
        String userName = SystemConf.get("capture.dvrUser");
        String passWord = SystemConf.get("capture.dvrPassword");
        HCNetSDK.NET_DVR_DEVICEINFO_V30 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V30();

        this.lUserID = hCNetSDK.NET_DVR_Login_V30(m_sDeviceIP,
                (short) iPort, userName, passWord, m_strDeviceInfo);

        long userID = lUserID.longValue();

        //获取通道号
        int iChannelNum = Integer.parseInt(SystemConf.get("capture.dvrChannel"));//通道号

        this.m_strClientInfo = new HCNetSDK.NET_DVR_CLIENTINFO();
        this.m_strClientInfo.lChannel = new NativeLong(iChannelNum);

        this.m_strClientInfo.hPlayWnd = null;

        //不回调预览
        NativeLong lPreviewHandle = hCNetSDK.NET_DVR_RealPlay_V30(lUserID,
                m_strClientInfo, null, null, true);
        long previewSucValue = lPreviewHandle.longValue();

        this.lJpegPara = new HCNetSDK.NET_DVR_JPEGPARA();
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPortEvent.BI:    //通讯中断
            case SerialPortEvent.OE:    //溢位错误
            case SerialPortEvent.FE:    //帧错误
            case SerialPortEvent.PE:    //奇偶校验错误
            case SerialPortEvent.CD:    //载波检测
            case SerialPortEvent.CTS:    //清除发送
            case SerialPortEvent.DSR:    //数据设备准备好
            case SerialPortEvent.RI:    //响铃侦测
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:    //输出缓冲区已清空
                break;
            case SerialPortEvent.DATA_AVAILABLE:    //有数据到达

                synchronized (Infrared.class) {
                    try {
                        String response;
                        byte[] bytes = SerialPortManager.readFromPort(serialPort, 9);
                        if (bytes != null && bytes.length == 9) {
                            response = new String(bytes);
                            if (StringUtils.startsWith(response, "!01")) { // 校验合理性 发送报警灯指令与红外读取指令返回值合并，出错
                                try {
                                    infraredCount = Integer.valueOf(response.substring(3, 8));
                                    if (infraredCount > person.getTotal()) {
                                        // 视频抓拍
                                        capturePicture();
                                        ErrorExecution.handle(104); // 报警
                                        person.setTotal(infraredCount);
                                    }
                                } catch (NumberFormatException e) {
                                    if (StringUtils.equals(response, "!01\r!0100") || StringUtils.equals(response, "!01\r>\r!01")) {
                                        LOGGER.info("清空红外计数成功");
                                        break;
                                    } else {
                                        LOGGER.error("io模块读取红外错误");
//                                        e.printStackTrace();
                                    }
                                }

                                /**
                                 * 红外计数另一种方式 状态


                                 String in1 = response.substring(4, 5);
                                 try {
                                 if ((Integer.parseInt(in1, 16) & 1) == 1) {
                                 // 触碰红外
                                 infraredCount++;
                                 System.out.println("刷卡总人数：" + person.getTotal());
                                 if (!Objects.equals(infraredCount, person.getTotal())) {
                                 // 抓拍 TODO
                                 System.out.println("抓拍");

                                 // 恢复刷卡总人数与触碰红外人数相等
                                 infraredCount = person.getTotal();
                                 }
                                 }
                                 } catch (NumberFormatException e) {
                                 LOGGER.error("io模块读取红外错误");
                                 }
                                 */

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void run() {
        // 创建串口
        try {
            SerialPortManager.addEventListener(serialPort, this);


            // 红外读指令
            byte[] command = ("#010" + Constant.LINE_SEPARATOR).getBytes();

            while (true) {
                SerialPortManager.sendToPort(serialPort, command);
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //抓拍
    public int capturePicture() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        String time = sdf.format(new Date());

        String dir = SystemConf.get("capture.path");
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        String fullFilePath = dir + "\\\\" + time + ".jpg";

        boolean isCaptured = hCNetSDK.NET_DVR_CaptureJPEGPicture(lUserID, m_strClientInfo.lChannel, lJpegPara, fullFilePath);
        if (isCaptured) {
            LOGGER.info(time + ":抓拍成功");
        }

        // Error Code
        return hCNetSDK.NET_DVR_GetLastError();
    }

}
