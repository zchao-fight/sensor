package cn.ccf.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SystemConf {

    private static Properties properties = new Properties();

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemConf.class);

    static {
        try {
            InputStream in = SystemConf.class.getClassLoader().getResourceAsStream("env.properties");
            properties.load(in);
            in.close();
        } catch (IOException e) {
            LOGGER.error("读取配置文件错误");
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static void set(String key, String value) {
        properties.setProperty(key, value);
    }
}
