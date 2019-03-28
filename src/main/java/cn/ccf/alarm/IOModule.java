package cn.ccf.alarm;

import cn.ccf.common.Constant;
import cn.ccf.common.SerialPortManager;
import cn.ccf.common.SystemConf;
import gnu.io.SerialPort;

public class IOModule {

    private static SerialPort serialPort;

    static {
        try {
            serialPort = SerialPortManager.openPort(SystemConf.get("io.module.port"), 9600);
            byte[] clearCommand = ("$01C0" + Constant.LINE_SEPARATOR).getBytes();
            // 清空红外计数
            SerialPortManager.sendToPort(serialPort, clearCommand);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SerialPort getSerialPort() {
        return serialPort;
    }

}
