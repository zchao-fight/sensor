package cn.ccf.swipe;

import cn.ccf.alarm.AlarmSound;
import cn.ccf.alarm.Broadcast;
import cn.ccf.alarm.IOModule;
import cn.ccf.bean.Person;
import cn.ccf.common.*;
import cn.ccf.exception.*;
import cn.ccf.httpclient.ApiService;
import cn.ccf.httpclient.HttpResult;
import cn.ccf.utils.Conversion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gnu.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TooManyListenersException;

public class SwipeCardIn implements SerialPortEventListener, Runnable {


    private Thread closeDoorThread;

    private static final Logger LOGGER = LoggerFactory.getLogger(SwipeCardOut.class);

    private Person person;

    private SerialPort serialPort;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public SwipeCardIn(Person person) {
        this.person = person;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPortEvent.BI:    //通讯中断
                System.out.print("--");
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
                    byte[] bytes = SerialPortManager.readFromPort(serialPort, 12);
                    if (bytes == null) {
                        break;
                    }
                    String hexString = Conversion.bytes2HexString(bytes);
                    if (!hexString.startsWith("02") || !hexString.endsWith("0D0A03")) {
                        LOGGER.error("卡号错误");
                        Broadcast.play(SystemConf.get("alarm.sound.uselessCard"));
                        break;
                    }

                    String hexWorkId = hexString.substring(2, 18);
                    String workId = Conversion.hexStr2Str(hexWorkId);
                    ApiService apiService = new ApiService();
                    Map<String, String> params = new HashMap<>();
                    params.put("workId", String.valueOf(Long.parseLong(workId, 16)));
                    params.put("workshopId", SystemConf.get("workshop.id"));
                    params.put("num", String.valueOf(person.getCount() + 1));

                    // 刷卡进入人数超过最大值 > 目的：防止更改定员人数错误
                    if ((person.getCount() + 1) > person.getQuota()) {
                        AlarmSound alarmSound = new AlarmSound(SystemConf.get("alarm.sound.OutOfLimit"));
                        Thread thread = new Thread(alarmSound);
                        thread.start();
                        break;
                    }

                    HttpResult result = apiService.doPost(Constant.SERVER_IP_PORT_CONTEXT + "/api/checkSwipeIn.action", params);

                    if (result.getCode() == 200) {
                        JsonNode jsonNode = MAPPER.readTree(result.getBody());
                        if (jsonNode.get("code").asInt() == 1) {
                            Integer count = person.incr();

                            DiskSerialization diskSerialization = new DiskSerialization(person.getQuota(), count);
                            diskSerialization.start();

                            // 刷卡总人数
                            person.incrTotal();

                            if (count == person.getQuota()) {
                                // 黄灯亮 绿灯灭 门磁开
                                String command = "#010A0A" + Constant.LINE_SEPARATOR; // 控制低8位数字信号输出
                                SerialPortManager.sendToPort(IOModule.getSerialPort(), command.getBytes());
                            } else {
                                // 绿灯亮 门磁开
                                String command = "#010A09" + Constant.LINE_SEPARATOR; // 控制低8位数字信号输出
                                SerialPortManager.sendToPort(IOModule.getSerialPort(), command.getBytes());
                            }

                            if (closeDoorThread != null) {
                                closeDoorThread.interrupt();
                            }
                            closeDoorThread = new Thread(() -> {
                                try {
                                    Thread.sleep(Integer.parseInt(SystemConf.get("close.door.internal")));
                                    String command = "#011300" + Constant.LINE_SEPARATOR; // 控制单个数字信号输出
                                    SerialPortManager.sendToPort(IOModule.getSerialPort(), command.getBytes());
                                } catch (InterruptedException | SendDataToSerialPortFailure | SerialPortOutputStreamCloseFailure e) {
                                    LOGGER.info(closeDoorThread + "进入：十秒关门线程中断，从新开始计算十秒");
                                }
                            });
                            closeDoorThread.setName("closeDoorThread");
                            closeDoorThread.start();

                        } else {
                            ErrorExecution.handle(jsonNode.get("code").asInt());
                        }

                        break;
                    }
                } catch (ReadDataFromSerialPortFailure | SerialPortInputStreamCloseFailure | SendDataToSerialPortFailure | IOException | SerialPortOutputStreamCloseFailure readDataFromSerialPortFailure) {
                    readDataFromSerialPortFailure.printStackTrace();
                }
            default:
                break;
        }
    }

    @Override
    public void run() {
        // 创建串口， COM2位串口名称， 9600波特率
        try {
            serialPort = SerialPortManager.openPort(SystemConf.get("swipe.card.in.port"), 9600);
            SerialPortManager.addEventListener(serialPort, this);
        } catch (NoSuchPortException | PortInUseException | NotASerialPort | UnsupportedCommOperationException | TooManyListenersException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String cardId = "0012387932";
        System.out.println(Integer.valueOf(cardId));

//        Hex.encodeHexString()
        int temp = Integer.parseInt("00BD065C", 16);
        System.out.println(temp);

    }
}
