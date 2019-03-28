package cn.ccf.alarm;

import cn.ccf.common.SerialPortManager;
import cn.ccf.common.*;
import cn.ccf.exception.NotASerialPort;
import cn.ccf.exception.SendDataToSerialPortFailure;
import cn.ccf.exception.SerialPortOutputStreamCloseFailure;
import gnu.io.*;

import java.util.TooManyListenersException;

public class AlarmLight implements Runnable {
    private SerialPort serialPort;

    public AlarmLight(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    @Override
    public void run() {

        try {
            // 启动程序 显示绿灯
            String command = "#011001" + Constant.LINE_SEPARATOR; // 控制单个数字信号输出
            SerialPortManager.sendToPort(serialPort, command.getBytes());
        } catch (SendDataToSerialPortFailure | SerialPortOutputStreamCloseFailure sendDataToSerialPortFailure) {
            sendDataToSerialPortFailure.printStackTrace();
        }
    }
}
