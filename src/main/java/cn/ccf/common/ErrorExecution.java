package cn.ccf.common;

import cn.ccf.alarm.AlarmSound;

public class ErrorExecution {
    public static void handle(int code) {
        switch (code) {
            case 101:
                AlarmSound uselessCardAlarm = new AlarmSound(SystemConf.get("alarm.sound.uselessCard"));
                Thread uselessCardthread = new Thread(uselessCardAlarm);
                uselessCardthread.start();
                break;
            case 102:
                AlarmSound haveBeenInFactoryAlarm = new AlarmSound(SystemConf.get("haveBeenInFactoryalarm.sound.haveBeenInFactory"));
                Thread haveBeenInFactoryThread = new Thread(haveBeenInFactoryAlarm);
                haveBeenInFactoryThread.start();
                break;
            case 103:
                AlarmSound haveBeenOutOfFactoryAlarm = new AlarmSound(SystemConf.get("alarm.sound.haveBeenOutOfFactory"));
                Thread haveBeenOutOfFactoryThread = new Thread(haveBeenOutOfFactoryAlarm);
                haveBeenOutOfFactoryThread.start();
                break;
            case 104 :
                AlarmSound swipeCardPreInAlarm = new AlarmSound(SystemConf.get("alarm.sound.swipeCardPreIn"));
                Thread swipeCardPreInThread = new Thread(swipeCardPreInAlarm);
                swipeCardPreInThread.start();
            default:
                break;
        }

    }
}
