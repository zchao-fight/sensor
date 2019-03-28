package cn.ccf;

import cn.ccf.common.Constant;
import cn.ccf.common.ResponseCodeConst;
import cn.ccf.common.SerialPortManager;
import cn.ccf.common.SystemConf;
import cn.ccf.exception.ReadDataFromSerialPortFailure;
import cn.ccf.exception.SerialPortInputStreamCloseFailure;
import cn.ccf.httpclient.ApiService;
import cn.ccf.httpclient.HttpResult;
import cn.ccf.utils.Conversion;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Gas implements SerialPortEventListener, Runnable {

    private static int gasBaudRate;

    private static int internal;

    private ApiService apiService = new ApiService();

    static {
        gasBaudRate = Integer.parseInt(SystemConf.get("gas.baudRate"));
        internal = Integer.parseInt(SystemConf.get("gas.internal"));
    }

    private SerialPort serialPort;

    private static final Logger LOGGER = LoggerFactory.getLogger(Gas.class);


    private String gasPort;
    private String gasNum;
    private String gasAddr;

    public Gas(String gasPort, String gasNum, String gasAddr) {
        this.gasPort = gasPort;
        this.gasNum = gasNum;
        this.gasAddr = gasAddr;
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

                    byte[] bytes = SerialPortManager.readFromPortWithVariation(serialPort, 8);
                    if (bytes == null) {
                        break;
                    }
                    String hexString = Conversion.bytes2HexString(bytes);
                    if (StringUtils.startsWith(hexString, "A0FF0500" + gasAddr)) {

//                        B11~B0 数据位(最大能显示4095)
                        String valueStr = hexString.substring(12, 14);
                        int value = Integer.parseInt(valueStr, 16);
                        String unit = "PPM";

                        Map<String, String> params = new HashMap<>();
                        params.put("sensorId", gasNum);
                        params.put("unit", unit);
                        params.put("sensorType", "gas");
                        params.put("num", String.valueOf(value));
                        HttpResult result = apiService.doPost(Constant.SERVER_IP_PORT_CONTEXT + "/api/saveGas.action", params);
                        if (result.getCode() == ResponseCodeConst.ERROR_PARAM.getCode()) {
                            LOGGER.error("气体数据保存数据库失败" + new Date().toLocaleString());
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
            serialPort = SerialPortManager.openPort(gasPort, gasBaudRate);
            SerialPortManager.addEventListener(serialPort, this);

            // 粉尘发送指令
            byte[] command;
            switch (gasAddr) {
                case "01":
                    command = Conversion.hexToByte("A0000500010000A6");
                    break;
                case "02":
                    command = Conversion.hexToByte("A0000500020000A7");
                    break;
                case "03":
                    command = Conversion.hexToByte("A0000500030000A8");
                    break;
                default:
                    throw new RuntimeException("气体设备地址配置错误");
            }

            while (true) {
                SerialPortManager.sendToPort(serialPort, command);
                Thread.sleep(internal);
            }
        } catch (Exception e) {
            LOGGER.error("读取" + gasPort + "串口出错。。。。。。");
            e.printStackTrace();
        }
    }

}
