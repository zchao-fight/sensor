package cn.ccf.alarm;

public class AlarmSound implements Runnable {

    private String filePath;

    public AlarmSound(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void run() {
        Broadcast.play(filePath);
    }

}
