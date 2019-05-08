package cn.ccf.alarm;

import cn.ccf.common.Constant;

public class AlarmSound implements Runnable{

    private String filePath;

    public AlarmSound(String filePath) {
        this.filePath = Constant.USER_DIR + Constant.FILE_SEPARATOR + filePath;
    }

    @Override
    public void run() {
        Broadcast.play(filePath);
    }

}
