package cn.ccf.quartz;

import cn.ccf.common.SystemConf;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.DateBuilder.evenMinuteDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public class CronExecute {
    public void execute() throws Exception {
        Logger log = LoggerFactory.getLogger(CronExecute.class);

        log.info("------- Initializing ----------------------");

        // 定义调度器
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler sched = sf.getScheduler();
        Scheduler sched1 = sf.getScheduler();

        log.info("------- Initialization Complete -----------");

        // 获取当前时间的下一分钟
        Date runTime = evenMinuteDate(new Date());

        log.info("------- Scheduling Job  -------------------");

        // 定义laserJob
        JobDetail laserJob = newJob(LaserJob.class).withIdentity("laserJob", "sensor").build();

        JobDetail temperatureJob = newJob(TemperatureJob.class).withIdentity("temperatureJob", "sensor").build();

        // 定义触发器，每5秒执行一次
        Trigger laserTrigger = newTrigger().withIdentity("laserTrigger", "laser")
                .withSchedule(cronSchedule("0/"+ Integer.parseInt(SystemConf.get("laser.internal"))+" * * * * ?")).build();

        Trigger temperatureTrigger = newTrigger().withIdentity("temperatureTrigger", "temperature")
                .withSchedule(cronSchedule("0/"+ Integer.parseInt(SystemConf.get("temperature.internal"))+" * * * * ?")).build();

        // 将job注册到调度器
        sched.scheduleJob(laserJob, laserTrigger);
        sched.scheduleJob(temperatureJob, temperatureTrigger);
        log.info(laserJob.getKey() + " will run at: " + runTime);

        // 启动调度器
        sched.start();

        log.info("------- Started Scheduler -----------------");

        // 等待1分钟
        log.info("------- Waiting 60 seconds... -------------");
        try {
            Thread.sleep(60L * 1000L);
        } catch (Exception e) {
            //
        }

        // 关闭调度器
        log.info("------- Shutting Down ---------------------");
        sched.shutdown(true);
        log.info("------- Shutdown Complete -----------------");
    }


    public static void main(String[] args) throws Exception {

        CronExecute cronExecute = new CronExecute();
        cronExecute.execute();

    }

}
