package cn.ccf.common;

import cn.ccf.alarm.AlarmSound;

public class ErrorExecution {
    public static void handle(int code) {
        switch (code) {
            case 101:
                AlarmSound uselessCardAlarm = null;
                uselessCardAlarm = new AlarmSound(Constant.USER_DIR + SystemConf.get("alarm.sound.uselessCard"));
                Thread uselessCardthread = new Thread(uselessCardAlarm);
                uselessCardthread.start();
                break;
            case 102:
                AlarmSound haveBeenInFactoryAlarm = null;
                haveBeenInFactoryAlarm = new AlarmSound(Constant.USER_DIR + SystemConf.get("alarm.sound.haveBeenInFactory"));
                Thread haveBeenInFactoryThread = new Thread(haveBeenInFactoryAlarm);
                haveBeenInFactoryThread.start();
                break;
            case 103:
                AlarmSound haveBeenOutOfFactoryAlarm = null;
                haveBeenOutOfFactoryAlarm = new AlarmSound(Constant.USER_DIR + SystemConf.get("alarm.sound.haveBeenOutOfFactory"));
                Thread haveBeenOutOfFactoryThread = new Thread(haveBeenOutOfFactoryAlarm);
                haveBeenOutOfFactoryThread.start();
                break;
            case 104:
                AlarmSound swipeCardPreInAlarm = null;
                swipeCardPreInAlarm = new AlarmSound(Constant.USER_DIR + SystemConf.get("alarm.sound.swipeCardPreIn"));
                Thread swipeCardPreInThread = new Thread(swipeCardPreInAlarm);
                swipeCardPreInThread.start();
            default:
                break;
        }

    }
}
