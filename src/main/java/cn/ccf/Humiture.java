package cn.ccf;

import cn.ccf.common.Constant;
import cn.ccf.common.ResponseCodeConst;
import cn.ccf.common.SerialPortManager;
import cn.ccf.common.SystemConf;
import cn.ccf.httpclient.ApiService;
import cn.ccf.httpclient.HttpResult;
import cn.ccf.utils.Conversion;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Humiture implements SerialPortEventListener, Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Humiture.class);
    private SerialPort serialPort;
    private static int internal;
    private static int humitureBaudRate;

    private ApiService apiService = new ApiService();

    static {
        humitureBaudRate = Integer.parseInt(SystemConf.get("humiture.baudRate"));
        internal = Integer.parseInt(SystemConf.get("humiture.internal"));
    }

    private String humiturePort;
    private String channelCount;
    private String humitureNumber;

    public Humiture(String humiturePort, String channelCount, String humitureNumber) {
        this.humiturePort = humiturePort;
        this.channelCount = channelCount;
        this.humitureNumber = humitureNumber;
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
                    byte[] bytes = SerialPortManager.readFromPort(serialPort, 5 + Integer.parseInt(channelCount) * 2);
                    if (bytes == null) {
                        return;
                    }
                    String hexString = Conversion.bytes2HexString(bytes);
                    Map<String, String> params = new HashMap<>();
                    String[] values = new String[Integer.parseInt(channelCount)];

                    DecimalFormat df = new DecimalFormat("#.00");//设置保留位数

                    for (int i = 0; i < Integer.parseInt(channelCount); i++) {
                        if (i % 2 == 0) {
                            // 计算温度
                            String value = df.format((float) Integer.parseInt(hexString.substring(6 + 4 * i, 6 + (i + 1) * 4), 16) / 125 - 20);
                            values[i] = value;
                        } else {
                            // 计算湿度
                            String value = df.format((float) (Integer.parseInt(hexString.substring(6 + 4 * i, 6 + (i + 1) * 4), 16)) / 100);
                            values[i] = value;
                        }
                    }
                    String num = StringUtils.join(values, ",");
                    params.put("num", num);
                    params.put("sensorId", humitureNumber);
                    params.put("sensorType", "humiture");
                    HttpResult result = apiService.doPost(Constant.SERVER_IP_PORT_CONTEXT + "/api/saveHumiture.action", params);
                    if (result.getCode() == ResponseCodeConst.ERROR_PARAM.getCode()) {
                        LOGGER.error("接地电阻数据保存数据库失败" + new Date().toLocaleString());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void run() {
        try {
            serialPort = SerialPortManager.openPortWithParity(humiturePort, humitureBaudRate, SerialPort.PARITY_EVEN);
            SerialPortManager.addEventListener(serialPort, this);

            // 温湿度指令
            String beforeCRC = "01040000000" + channelCount;
            String crc = Conversion.getCRC2(Conversion.hexToByte(beforeCRC));

            while (true) {
                SerialPortManager.sendToPort(serialPort, Conversion.hexToByte(beforeCRC + crc));
                Thread.sleep(internal);
            }
        } catch (Exception e) {
            LOGGER.error("读取" + humiturePort + "串口出错。。。。。。");
            e.printStackTrace();
        }


    }

}
