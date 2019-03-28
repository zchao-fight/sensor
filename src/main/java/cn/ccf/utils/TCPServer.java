package cn.ccf.utils;

import cn.ccf.bean.Person;
import cn.ccf.common.DiskSerialization;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
    private ServerSocket serverSocket;

    private Socket socket;

    private static final Logger LOGGER = LoggerFactory.getLogger(TCPServer.class);

    private int port;

    private Person person;

    public TCPServer(Person person, int port) {
        this.person = person;
        this.port = port;
    }

    public void startServer() {
        if (serverSocket == null) {
            try {
                serverSocket = new ServerSocket(port);
                LOGGER.info("服务器等待连接中。。。。");
                while (true) {
                    socket = serverSocket.accept();
                    LOGGER.info("服务器连接客户端成功。。。。");
                    new Thread() {
                        private Socket t_socket;

                        {
                            t_socket = socket;
                        }

                        @Override
                        public void run() {
                            BufferedReader br = null;
                            try {
                                InputStream in = t_socket.getInputStream();
                                InputStreamReader isr = new InputStreamReader(in, "utf-8");
                                br = new BufferedReader(isr);
                                String msg = br.readLine();
                                // todo while循环出差
                                if (StringUtils.startsWith(msg, "clear")) {
                                    // 清零
                                    person.setCount(0);
                                    int quota = person.getQuota();
                                    DiskSerialization diskSerialization = new DiskSerialization(quota, 0);
                                    diskSerialization.start();
                                    // led读取文件
                                } else if (StringUtils.startsWith(msg, "quota")) {
                                    //更改定员
                                    String[] splits = msg.split(":");
                                    int quota = Integer.parseInt(splits[1]);
                                    person.setQuota(quota);
                                    DiskSerialization diskSerialization = new DiskSerialization(quota, person.getCount());
                                    diskSerialization.start();
                                }

                                System.out.println(msg);

                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    if (br != null) {
                                        br.close();
                                    }
                                    t_socket.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
