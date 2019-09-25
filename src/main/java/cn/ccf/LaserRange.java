package cn.ccf;

import cn.ccf.common.Constant;
import cn.ccf.common.ResponseCodeConst;
import cn.ccf.common.SerialPortManager;
import cn.ccf.common.SystemConf;
import cn.ccf.httpclient.ApiService;
import cn.ccf.httpclient.HttpResult;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class LaserRange implements SerialPortEventListener, Runnable {

    private static int laserBaudRate;

    private static int internal;

    private ApiService apiService = new ApiService();

    static {
        laserBaudRate = Integer.parseInt(SystemConf.get("laser.baudRate"));
        internal = Integer.parseInt(SystemConf.get("laser.internal"));
    }

    private SerialPort serialPort;

    private String laserPort;

    private String laserNum;

    private static final Logger LOGGER = LoggerFactory.getLogger(LaserRange.class);

    public LaserRange(String laserPort, String laserNum) {
        this.laserPort = laserPort;
        this.laserNum = laserNum;
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
                try {
                    byte[] bytes = SerialPortManager.readFromPort(serialPort, 16);
                    if (bytes != null && bytes.length == 16) {
                        String content = new String(bytes, "utf-8");
                        if (StringUtils.startsWith(content, "F:Er")) {
                            LOGGER.error(new Date() + "激光测量数据错误");
                        } else {
                            String distance = content.substring(2, 8);
//                            System.out.println(Thread.currentThread().getName() + "#" + content);

                            boolean flag = Pattern.matches("\\d{1,2}\\.\\d{3}", distance.trim());
                            if (flag) {

                                String unit = "m";

                                Map<String, String> params = new HashMap<>();
                                params.put("sensorId", laserNum);
                                params.put("unit", unit);
                                params.put("sensorType", "laser");
                                params.put("num", distance.trim());
                                HttpResult result = apiService.doPost(Constant.SERVER_IP_PORT_CONTEXT + "/api/saveLaser.action", params);
                                if (result.getCode() == ResponseCodeConst.ERROR_PARAM.getCode()) {
                                    LOGGER.error("激光数据保存数据库失败" + new Date().toLocaleString());
                                }
                            }
                        }
                    }

                    break;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            default:
                break;
        }
    }

    @Override
    public void run() {

        // 创建串口
        try {
            serialPort = SerialPortManager.openPort(laserPort, laserBaudRate);
            SerialPortManager.addEventListener(serialPort, this);
            while (true) {
                SerialPortManager.sendToPort(serialPort, "F".getBytes());
                Thread.sleep(internal);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
