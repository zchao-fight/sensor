package cn.ccf.quartz;

import cn.ccf.httpclient.ApiService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.IOException;
import java.util.Date;

public class LaserJob implements Job {

    private String name;

    public LaserJob(String name) {
        this.name = name;
    }

    public LaserJob() {
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        ApiService apiService = new ApiService();
        try {
            System.out.println(apiService.doGet("http://www.baidu.com"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("激光" + new Date().toLocaleString());
    }
}
