package cn.ccf;

import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.TooManyListenersException;

/**
 * 串口参数的配置 串口一般有如下参数可以在该串口打开以前进行配置：
 * 包括波特率，输入/输出流控制，数据位数，停止位和奇偶校验。
 */
// 注：串口操作类一定要继承SerialPortEventListener 
public class SerialPortTest1 implements SerialPortEventListener {
    // 检测系统中可用的通讯端口类 
    private CommPortIdentifier portId;
    // 枚举类型
    private Enumeration<CommPortIdentifier> portList;

    // RS232串口
    private SerialPort serialPort;

    // 输入输出流
    private InputStream inputStream;
    private OutputStream outputStream;

    // 保存串口返回信息
    private String test = "";

    // 单例创建
    private static SerialPortTest1 uniqueInstance = new SerialPortTest1();

    // 初始化串口
    @SuppressWarnings("unchecked")
    public void init() {

        portList = CommPortIdentifier.getPortIdentifiers();
        // 循环通讯端口
        while (portList.hasMoreElements()) {
            portId = portList.nextElement();
            // 判断是否是串口
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                // 比较串口名称是否是"COM1"
                if ("COM2".equals(portId.getName())) {
                    System.out.println("找到串口COM2");
                    // 打开串口
                    try {
                        // open:（应用程序名【随意命名】，阻塞时等待的毫秒数）
                        serialPort = (SerialPort) portId.open(Object.class.getSimpleName(), 2000);
                        System.out.println("获取到串口对象,COM2");
                        // 设置串口监听
                        serialPort.addEventListener(this);
                        // 设置串口数据时间有效(可监听)
                        serialPort.notifyOnDataAvailable(true);
                        // 设置串口通讯参数 
                        // 波特率，数据位，停止位和校验方式
                        // 波特率2400,偶校验 
                        serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8,//
                                SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                        test = "";
                        outputStream = serialPort.getOutputStream();

                    } catch (PortInUseException e) {
                        e.printStackTrace();
                    } catch (TooManyListenersException e) {
                        e.printStackTrace();
                    } catch (UnsupportedCommOperationException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    // 实现接口SerialPortEventListener中的方法 读取从串口中接收的数据
    @Override
    public void serialEvent(SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPortEvent.BI:	//通讯中断
            case SerialPortEvent.OE:	//溢位错误
            case SerialPortEvent.FE:	//帧错误
            case SerialPortEvent.PE:	//奇偶校验错误
            case SerialPortEvent.CD:	//载波检测
            case SerialPortEvent.CTS:	//清除发送
            case SerialPortEvent.DSR:	//数据设备准备好
            case SerialPortEvent.RI:	//响铃侦测
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:	//输出缓冲区已清空
                break;
            case SerialPortEvent.DATA_AVAILABLE:	//有数据到达
                readComm();
                break;
            default:
                break;
        }
    }

    // 读取串口返回信息
    public void readComm() {
        byte[] readBuffer = new byte[1024];
        try {
            inputStream = serialPort.getInputStream();
            // 从线路上读取数据流
            int len = 0;
            while ((len = inputStream.read(readBuffer)) != -1) {
//                System.out.println("实时反馈：" + new String(readBuffer, 0, len).trim() + new Date());
//                test += new String(readBuffer, 0, len).trim();
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 关闭串口
    public void closeSerialPort() {
        if (serialPort != null) {
            serialPort.notifyOnDataAvailable(false);
            serialPort.removeEventListener();
            if (inputStream != null) {
                try {
                    inputStream.close();
                    inputStream = null;
                }
                catch (IOException e) {}
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                    outputStream = null;
                }
                catch (IOException e) {}
            }
            serialPort.close();
            serialPort = null;
        }
    }

    public static void main(String[] args) {
        SerialPortTest1 sp = new SerialPortTest1();
        sp.init();
        System.out.println("输出" + sp.test);
        sp.readComm();
        sp.closeSerialPort();
    }
}

