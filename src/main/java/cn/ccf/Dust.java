package cn.ccf;

import cn.ccf.common.Constant;
import cn.ccf.common.ResponseCodeConst;
import cn.ccf.common.SerialPortManager;
import cn.ccf.common.SystemConf;
import cn.ccf.exception.NotASerialPort;
import cn.ccf.httpclient.ApiService;
import cn.ccf.httpclient.HttpResult;
import cn.ccf.utils.Conversion;
import gnu.io.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TooManyListenersException;

public class Dust implements Runnable, SerialPortEventListener {

    private static int dustBaudRate;

    private static int internal;

    private ApiService apiService = new ApiService();


    static {
        dustBaudRate = Integer.parseInt(SystemConf.get("dust.baudRate"));
        internal = Integer.parseInt(SystemConf.get("dust.internal"));
    }
    private SerialPort serialPort;

    private String dustPort;
    private String dustNum;
    private String dustAddr;

    public Dust(String dustPort, String dustNum, String dustAddr) {
        this.dustPort = dustPort;
        this.dustNum = dustNum;
        this.dustAddr = dustAddr;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Dust.class);

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
                    byte[] bytes = SerialPortManager.readFromPort(serialPort, 7);
                    if (bytes == null) {
                        break;
                    }
                    String hexString = Conversion.bytes2HexString(bytes);
                    if (StringUtils.startsWith(hexString, dustAddr + "0302")) {
                        DecimalFormat df = new DecimalFormat("0.0");
                        String value = df.format((float)Integer.parseInt(hexString.substring(6, 10), 16)/10);
                        String unit = "mg/m³";

                        Map<String, String> params = new HashMap<>();
                        params.put("sensorId", dustNum);
                        params.put("unit", unit);
                        params.put("sensorType", "dust");
                        params.put("num", value);
                        HttpResult result = apiService.doPost(Constant.SERVER_IP_PORT_CONTEXT + "/api/saveDust.action", params);
                        if (result.getCode() == ResponseCodeConst.ERROR_PARAM.getCode()) {
                            LOGGER.error("粉尘数据保存数据库失败" + new Date().toLocaleString());
                        }
                    }
                } catch (Exception readDataFromSerialPortFailure) {
                    readDataFromSerialPortFailure.printStackTrace();
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
            serialPort = SerialPortManager.openPort(dustPort, dustBaudRate);
            SerialPortManager.addEventListener(serialPort, this);
            // 粉尘发送指令
            byte[] command;
            switch (dustAddr) {
                case "01":
                    command = Conversion.hexToByte("0103000D000115C9");
                    break;
                case "02":
                    command = Conversion.hexToByte("0203000D000115fa");
                    break;
                case "03":
                    command = Conversion.hexToByte("0303000D0001142b");
                    break;
                default:
                    throw new RuntimeException("粉尘设备地址配置错误");
            }

            while (true) {
                SerialPortManager.sendToPort(serialPort, command);
                Thread.sleep(internal);
            }
        } catch (Exception e) {
            LOGGER.error("读取" + dustPort + "串口出错。。。。。。");
            e.printStackTrace();
        }
    }

}
