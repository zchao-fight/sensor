package cn.ccf.exception;

public class NotASerialPort extends Exception {

    public NotASerialPort() {
    }

    public NotASerialPort(String message) {
        super(message);
    }
}
