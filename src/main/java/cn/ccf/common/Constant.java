package cn.ccf.common;

public class Constant {

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public static final String FILE_SEPARATOR = System.getProperty("file.separator");

    public static final String SWIPE_NUMBER_DIR = "c:" + FILE_SEPARATOR + "swipe-number";

    public static final String SWIPE_NUMBER_PATH = "c:" + FILE_SEPARATOR + "swipe-number" + FILE_SEPARATOR + "perNum.txt";

    public static final String SERVER_IP_PORT_CONTEXT = "http://" + SystemConf.get("server.ip") + ":" + SystemConf.get("server.port") + "/"
            + SystemConf.get("server.context.name");

    public static final String USER_DIR = System.getProperty("user.dir");

}
