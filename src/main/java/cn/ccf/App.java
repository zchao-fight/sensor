package cn.ccf;

import cn.ccf.common.SerialPortManager;
import cn.ccf.exception.*;
import gnu.io.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.TooManyListenersException;


/**
 * Hello world!
 */
public class App implements SerialPortEventListener {

    private SerialPort serialPort;

    private byte[] data;

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");


    public static void main(String[] args) throws SerialPortOutputStreamCloseFailure, SendDataToSerialPortFailure, NotASerialPort, ReadDataFromSerialPortFailure, NoSuchPortException, PortInUseException, UnsupportedCommOperationException, SerialPortInputStreamCloseFailure, TooManyListenersException, IOException {

        App app = new App();
        app.test2();


    }

    public void test2() {
        SerialPort serialPort = null;
        try {
            serialPort = SerialPortManager.openPort("COM2", 9600);
            SerialPortManager.addEventListener(serialPort, this);
            OutputStream outputStream = serialPort.getOutputStream();
            String command = "#011301" + LINE_SEPARATOR; // 控制单个数字信号输出

            outputStream.write(command.getBytes());
            outputStream.flush();


        } catch (NoSuchPortException e) {
            e.printStackTrace();
        } catch (PortInUseException e) {
            e.printStackTrace();
        } catch (UnsupportedCommOperationException e) {
            e.printStackTrace();
        } catch (NotASerialPort notASerialPort) {
            notASerialPort.printStackTrace();
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void test() throws PortInUseException, NoSuchPortException, NotASerialPort, UnsupportedCommOperationException, SerialPortOutputStreamCloseFailure, SendDataToSerialPortFailure, ReadDataFromSerialPortFailure, SerialPortInputStreamCloseFailure, TooManyListenersException, IOException {
        // 创建串口， COM1位串口名称， 9600波特率
        serialPort = SerialPortManager.openPort("COM2", 9600);
        SerialPortManager.addEventListener(serialPort, this);
    }

    // 实现接口SerialPortEventListener中的方法 读取从串口中接收的数据
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
                    byte[] bytes = SerialPortManager.readFromPort(serialPort, 13);
                } catch (ReadDataFromSerialPortFailure | SerialPortInputStreamCloseFailure readDataFromSerialPortFailure) {
                    readDataFromSerialPortFailure.printStackTrace();
                }
                break;
            default:
                break;
        }
    }
}
