package cn.ccf.common;

import cn.ccf.exception.*;
import gnu.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TooManyListenersException;

/**
 * @author charles zhang
 * open:（应用程序名【随意命名】，阻塞时等待的毫秒数）
 * open方法打开通讯端口，获得一个CommPort对象，它使程序独占端口。
 * 如果端口正被其他应用程序占用，将使用CommPortOwnershipListener事件机制
 * 传递一个PORT_OWNERSHIP_REQUESTED事件。
 * 每个端口都关联一个InputStream和一个OutputStream,如果端口是用
 * open方法打开的，那么任何的getInputStream都将返回相同的数据流对象，除非有close被调用。
 */
public class SerialPortManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SerialPortManager.class);

    /**
     * 查找所有可用端口
     *
     * @return portNameList 可用端口名称列表
     * @author charles zhang
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<String> findPort() {
        // 获得当前所有可用串口 获取系统中所有的通讯端口
        Enumeration<CommPortIdentifier> portList = CommPortIdentifier
                .getPortIdentifiers();
        ArrayList<String> portNameList = new ArrayList<>();
        // 将可用串口名添加到List并返回该List
        while (portList.hasMoreElements()) {
            String portName = portList.nextElement().getName();
            portNameList.add(portName);
        }
        return portNameList;
    }



    public static SerialPort openPortWithParity(String portName, int baudRate, int parity) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, NotASerialPort {

        try {
            // 通过端口名识别端口
            CommPortIdentifier portIdentifier = CommPortIdentifier
                    .getPortIdentifier(portName);
            // 打开端口，设置端口名与timeout（打开操作的超时时间）
            CommPort commPort = portIdentifier.open(portName, 2000);
            // 判断是不是串口
            if (commPort instanceof SerialPort) {
                SerialPort serialPort = (SerialPort) commPort;
                try {
                    // 设置串口的波特率等参数 波特率,数据位,停止位,奇偶检验
                    serialPort.setSerialPortParams(baudRate,
                            SerialPort.DATABITS_8,
                            SerialPort.STOPBITS_1,
                            SerialPort.PARITY_EVEN);
                } catch (UnsupportedCommOperationException e) {
                    LOGGER.error("设置串口参数失败", e);
                    throw new UnsupportedCommOperationException();
                }
                return serialPort;
            } else {
                // 不是串口
                LOGGER.error(portName + "端口指向设备不是串口类型");
                throw new NotASerialPort("端口指向设备不是串口类型");
            }
        } catch (NoSuchPortException e1) {
            LOGGER.error("没有该端口对应的串口设备 ： %s", e1.getMessage());
            throw new PortInUseException();
        } catch (PortInUseException e2) {
            LOGGER.error(portName + "端口正在使用中 ： %s", e2.getMessage());
            throw new PortInUseException();
        }
    }




    /**
     * 打开串口
     *
     * @param portName 端口名称
     * @param baudRate 波特率
     * @return 串口对象
     * @throws NoSuchPortException               没有该端口对应的串口设备
     * @throws PortInUseException                端口已被占用
     * @throws NotASerialPort                    端口指向设备不是串口类型
     * @throws UnsupportedCommOperationException 设置串口参数失败
     */
    public static SerialPort openPort(String portName, int baudRate) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, NotASerialPort {

        try {
            // 通过端口名识别端口
            CommPortIdentifier portIdentifier = CommPortIdentifier
                    .getPortIdentifier(portName);
            // 打开端口，设置端口名与timeout（打开操作的超时时间）
            CommPort commPort = portIdentifier.open(portName, 2000);
            // 判断是不是串口
            if (commPort instanceof SerialPort) {
                SerialPort serialPort = (SerialPort) commPort;
                try {
                    // 设置串口的波特率等参数 波特率,数据位,停止位,奇偶检验
                    serialPort.setSerialPortParams(baudRate,
                            SerialPort.DATABITS_8,
                            SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);
                } catch (UnsupportedCommOperationException e) {
                    LOGGER.error("设置串口参数失败", e);
                    throw new UnsupportedCommOperationException();
                }
                return serialPort;
            } else {
                // 不是串口
                LOGGER.error(portName + "端口指向设备不是串口类型");
                throw new NotASerialPort("端口指向设备不是串口类型");
            }
        } catch (NoSuchPortException e1) {
            LOGGER.error("没有该端口对应的串口设备 ： %s", e1.getMessage());
            throw new PortInUseException();
        } catch (PortInUseException e2) {
            LOGGER.error(portName + "端口正在使用中 ： %s", e2.getMessage());
            throw new PortInUseException();
        }
    }

    /**
     * 关闭串口
     * <p>
     * 待关闭的串口对象
     */
    public static void closePort(SerialPort serialPort) {
        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
        }
    }


    /**
     * 向串口发送数据
     *
     * @param serialPort 串口对象
     * @param order      待发送数据
     * @throws SendDataToSerialPortFailure        向串口发送数据失败
     * @throws SerialPortOutputStreamCloseFailure 关闭串口对象的输出流出错
     */
    public static void sendToPort(SerialPort serialPort, byte[] order) throws SendDataToSerialPortFailure, SerialPortOutputStreamCloseFailure {
        OutputStream out = null;

        try {
            out = serialPort.getOutputStream();
            out.write(order);
            out.flush();
        } catch (IOException e) {
            LOGGER.error("向串口发送数据失败");
            throw new SendDataToSerialPortFailure();
        } finally {
            if (null != out) {
                try {
                    out.close();
                    out = null;
                } catch (IOException e) {
                    LOGGER.error("关闭串口对象的输出流出错");
                    throw new SerialPortOutputStreamCloseFailure();
                }
            }
        }

    }

    /**
     * 从串口读取数据， 错误数据和正确数据长度不一致
     *
     * @param serialPort 当前已建立连接的SerialPort对象
     * @return 读取到的数据
     * @throws ReadDataFromSerialPortFailure     从串口读取数据时出错
     * @throws SerialPortInputStreamCloseFailure 关闭串口对象输入流出错
     */
    public static byte[] readFromPortWithVariation(SerialPort serialPort, int bufferLen) throws ReadDataFromSerialPortFailure, SerialPortInputStreamCloseFailure {
        InputStream in = null;
        try {
            // 获取串口里的数据长度
            in = serialPort.getInputStream();

            if (bufferLen <= in.available()) {
                // 初始化byte数组为协议中中数据的长度
                byte[] bytes = new byte[bufferLen];
                // 读取协议指定字节
                in.read(bytes,0, bufferLen);
                // 清空多余字节
                in.skip(in.available());
                return bytes;
            } else if (in.available() < bufferLen) {
                in.skip(in.available());
            }
        } catch (IOException e) {
            throw new ReadDataFromSerialPortFailure();
        } finally {
            try {
                if (in != null) {
                    in.close();
                    in = null;
                }
            } catch (Exception e) {
                throw new SerialPortInputStreamCloseFailure();
            }
        }
        return null;
    }

    /**
     * 从串口读取数据
     *
     * @param serialPort 当前已建立连接的SerialPort对象
     * @return 读取到的数据
     * @throws ReadDataFromSerialPortFailure     从串口读取数据时出错
     * @throws SerialPortInputStreamCloseFailure 关闭串口对象输入流出错
     */
    public static byte[] readFromPort(SerialPort serialPort, int bufferLen) throws ReadDataFromSerialPortFailure, SerialPortInputStreamCloseFailure {
        InputStream in = null;
        try {
            // 获取串口里的数据长度
            in = serialPort.getInputStream();

            if (bufferLen <= in.available()) {
                // 初始化byte数组为协议中中数据的长度
                byte[] bytes = new byte[bufferLen];
                // 读取协议指定字节
                in.read(bytes,0, bufferLen);
                // 清空多余字节
                in.skip(in.available());
                return bytes;
            }
        } catch (IOException e) {
            throw new ReadDataFromSerialPortFailure();
        } finally {
            try {
                if (in != null) {
                    in.close();
                    in = null;
                }
            } catch (IOException e) {
                throw new SerialPortInputStreamCloseFailure();
            }
        }
        return null;
    }


    /**
     * 添加监听器
     *
     * @param port     串口对象
     * @param listener 串口监听器 SerialPortEventListener传递串行端口事件
     * @throws TooManyListenersException 监听类对象过多
     * @author charles zhang
     */
    public static void addEventListener(SerialPort port,
                                        SerialPortEventListener listener) throws TooManyListenersException {
        try {
            // 给串口添加监听器
            port.addEventListener(listener);
            // 设置当有数据到达时唤醒监听接收线程
            port.notifyOnDataAvailable(true);
            // 设置当通信中断时唤醒中断线程
//            port.notifyOnBreakInterrupt(true);
        } catch (TooManyListenersException e) {
            LOGGER.error("监听类对象过多 : %s", e.getMessage());
            throw new TooManyListenersException();
        }
    }


}
