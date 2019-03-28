package cn.ccf.httpclient;

import java.io.IOException;
import java.util.Map;

public class HttpClient extends Thread {

    private String url;

    private Map<String, String> map;

    public HttpClient(String url, Map<String, String> map) {
        this.url = url;
        this.map = map;
    }



    @Override

    public void run() {
        ApiService apiService = new ApiService();
        try {
            HttpResult result = apiService.doPost(url, map);
            System.out.println(result.getCode());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
