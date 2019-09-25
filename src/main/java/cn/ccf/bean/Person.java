package cn.ccf.bean;

import cn.ccf.common.Constant;
import cn.ccf.common.SystemConf;
import cn.ccf.httpclient.ApiService;
import cn.ccf.httpclient.HttpResult;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

public class Person {

    private String name;
    private Integer count = 0; // 厂房进入人数
    private boolean flag = false;
    private Integer total = 0; // 总刷卡次数

    private Integer infraredCount = 0; //红外计数

    public int getQuota() {
        return quota;
    }

    public synchronized void setQuota(int quota) {
        this.quota = quota;
    }

    private int quota; // 定员

      {
        ApiService apiService = new ApiService();
        Map<String, String> map = new HashMap<>();
        map.put("workshopNumber", SystemConf.get("workshop.id"));
        try {
            HttpResult result = apiService.doPost(Constant.SERVER_IP_PORT_CONTEXT + "/workshop/getQuota.action", map);
            if (result.getCode() == 200) {
                this.setQuota( Integer.parseInt(result.getBody()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取厂房定员数量出错");
        }

          try {
              RandomAccessFile raf = new RandomAccessFile("c:\\swipe-number\\perNum.txt", "rw");
              String msg = raf.readLine();
              raf.seek(0);
              if (msg != null) {
                  String[] contents = msg.split(",");
                  count = Integer.parseInt((contents[1]));
              }
          } catch (Exception e) {
              e.printStackTrace();
          }
      }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public synchronized Integer incr() {
        // 刷卡进入人数加1
        count++;
        return count;
    }

    public synchronized void incrTotal() {
        total++;
    }

    public synchronized void decTotal() {
        total--;
    }

    public synchronized Integer dec() {
        // 刷卡出 人数减1
        count--;
        return count;
    }

    public Integer getTotal() {
        return total;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Integer getInfraredCount() {
        return infraredCount;
    }

    public void setInfraredCount(Integer infraredCount) {
        this.infraredCount = infraredCount;
    }
}

