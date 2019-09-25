package cn.ccf;

import cn.ccf.common.Constant;
import cn.ccf.common.ResponseCodeConst;
import cn.ccf.common.SerialPortManager;
import cn.ccf.common.SystemConf;
import cn.ccf.httpclient.ApiService;
import cn.ccf.httpclient.HttpResult;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 接近传感器
 *
 * @author charles
 * @date 2019/7/23 11:46
 */
public class Proximity implements SerialPortEventListener, Runnable {

    private static SerialPort serialPort = null;

    private static final Logger LOGGER = LoggerFactory.getLogger(Proximity.class);

    private String proximityPort;

    private String proximityNum;

    private int proximityCount;


    private static int internal;

    private static int baudRate;

    //    private List<String> list = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F");
    private List<String> valueList = Stream.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F").collect(Collectors.toList());

    private ApiService apiService = new ApiService();

    static {


    }

    static {
        baudRate = Integer.parseInt(SystemConf.get("proximity.baudRate"));
        internal = Integer.parseInt(SystemConf.get("proximity.internal"));

        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Proximity(String proximityPort, String proximityNum, int proximityCount) {
        this.proximityPort = proximityPort;
        this.proximityNum = proximityNum;
        this.proximityCount = proximityCount;
    }


    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        switch (serialPortEvent.getEventType()) {
            case SerialPortEvent.BI:
                break;
            case SerialPortEvent.OE:
                break;
            case SerialPortEvent.FE:
                break;
            case SerialPortEvent.PE:
                break;
            case SerialPortEvent.CD:
                break;
            case SerialPortEvent.CTS:
                break;
            case SerialPortEvent.DSR:
                break;
            case SerialPortEvent.RI:
                break;
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                break;
            case SerialPortEvent.DATA_AVAILABLE:
                synchronized (Proximity.class) {
                    try {
                        String response;
                        byte[] bytes = SerialPortManager.readFromPort(serialPort, 6);
                        if (bytes != null && bytes.length == 6) {
                            response = new String(bytes);

                            String hexTemp = response.substring(4, 5);

                            if (valueList.contains(hexTemp)) {
                                int statusValue = Integer.parseInt(hexTemp, 16);
                                // System.out.println(Integer.toBinaryString(statusValue));

                                // 校验合理性 接近传感器校验输入值
                                if (StringUtils.startsWith(response, ">")) {


                                    Map<String, String> params = new HashMap<>();
                                    params.put("sensorId", proximityNum);
                                    params.put("sensorType", "proximity");

/**
 N：待判断的二进制数
 B：待判断的位（右往左）

 结果：(（N>>（B-1）)&1
 */


                                 /*   if ((statusValue & 0b0001) == 0) {
                                        unit = "open";
                                        value = 0;

                                    } else {
                                        value = 1;
                                        unit = "close";
                                    }*/

                                    StringBuilder unit = null;
                                    StringBuilder value = null;

                                    for (int i = 0; i < proximityCount; i++) {
//                                        System.out.println("第" + (i + 1) + "位：" + (statusValue >> i & 0x01));

                                        if (i == 0) {
                                            if ((statusValue >> i & 0x01) == 0) {
                                                unit = new StringBuilder("close");
                                                value = new StringBuilder("0");


                                            } else {
                                                unit = new StringBuilder("open");
                                                value = new StringBuilder("1");
                                            }
                                        } else {
                                            if ((statusValue >> i & 0x01) == 0) {
                                                unit.append(",close");
                                                value.append(",0");

                                            } else {
                                                unit.append(",open");
                                                value.append(",1");
                                            }
                                        }

                                    }

                                    params.put("unit", unit.toString());
                                    params.put("num", value.toString());


                                    HttpResult result = apiService.doPost(Constant.SERVER_IP_PORT_CONTEXT + "/api/saveProximity.action", params);
                                    if (result.getCode() == ResponseCodeConst.ERROR_PARAM.getCode()) {
                                        LOGGER.error("接近传感器数据保存数据库失败" + new Date().toLocaleString());
                                    }
                                }
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
            serialPort = SerialPortManager.openPort(proximityPort, baudRate);
            SerialPortManager.addEventListener(serialPort, this);

            // 红外读指令 读取接近传感器数值
            byte[] command = ("@01" + Constant.LINE_SEPARATOR).getBytes();

            while (true) {
                SerialPortManager.sendToPort(serialPort, command);
                Thread.sleep(internal);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
