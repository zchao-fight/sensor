
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TooManyListenersException;

public class Resistance implements SerialPortEventListener, Runnable {

    private static int resistanceBaudRate;

    private static int internal;

    private ApiService apiService = new ApiService();


    static {
        resistanceBaudRate = Integer.parseInt(SystemConf.get("resistance.baudRate"));
        internal = Integer.parseInt(SystemConf.get("resistance.internal"));
    }

    private SerialPort serialPort;

    private String resistancePort;

    private String resistanceNum;

    private String resistanceAddr;

    private String addr;

    public Resistance(String resistancePort, String resistanceNum, String resistanceAddr) {
        this.resistancePort = resistancePort;
        this.resistanceNum = resistanceNum;
        this.resistanceAddr = resistanceAddr;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(LaserRange.class);

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
                    byte[] bytes = SerialPortManager.readFromPort(serialPort, 9);
                    if (bytes == null) {
                        break;
                    }
                    String hexString = Conversion.bytes2HexString(bytes);
                    int beforeDot =  Integer.parseInt(hexString.substring(6, 10), 16);

                    DecimalFormat df = new DecimalFormat("#.0000");//设置保留位数

                    String afterDot = df.format((float)Integer.parseInt(hexString.substring(10, 14), 16)/65536);

                    String value = beforeDot + "." + afterDot.substring(1, 5);
                    String unit = "欧姆";

                    Map<String, String> params = new HashMap<>();
                    params.put("sensorId", resistanceNum);
                    params.put("unit", unit);
                    params.put("sensorType", "resistance");
                    params.put("num", value);
                    HttpResult result = apiService.doPost(Constant.SERVER_IP_PORT_CONTEXT + "/api/saveResistance.action", params);
                    if (result.getCode() == ResponseCodeConst.ERROR_PARAM.getCode()) {
                        LOGGER.error("接地电阻数据保存数据库失败" + new Date().toLocaleString());
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
            serialPort = SerialPortManager.openPort(resistancePort, resistanceBaudRate);
            SerialPortManager.addEventListener(serialPort, this);

            // 接地电阻发送指令
            byte[] command;
            switch (resistanceAddr) {
                case "01":
                    command = Conversion.hexToByte("01030062000265d5");
                    break;
                case "02":
                    command = Conversion.hexToByte("02030062000265e6");
                    break;
                default:
                    throw new RuntimeException("接地电阻地址配置错误");
            }
            OutputStream outputStream;

            while (true) {
                outputStream = serialPort.getOutputStream();
                outputStream.write(command);
                outputStream.flush();
//                outputStream.close();
                Thread.sleep(internal);
            }
        } catch (NoSuchPortException | PortInUseException | NotASerialPort | UnsupportedCommOperationException | TooManyListenersException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
