package cn.ccf.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class DiskSerialization extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiskSerialization.class);

    private int count;

    private int quota;

    public DiskSerialization(int quota, int count) {
        this.quota = quota;
        this.count = count;
    }

    @Override
    public void run() {
        File file = new File(Constant.SWIPE_NUMBER_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }

        try {
            // 文件不存在 自动创建
            RandomAccessFile raf = new RandomAccessFile(Constant.SWIPE_NUMBER_PATH, "rw");
            FileChannel fc = raf.getChannel();
            //将文件大小截为0
            fc.truncate(0);
            String content = quota + "," + count;
            raf.write(content.getBytes("utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }


       /* FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(count);
        } catch (FileNotFoundException e) {
            LOGGER.error("写入刷卡人数文件不存在，请检查目录c:\\swipe-number\\perNum.txt是否存在");
        } catch (IOException e) {
            LOGGER.error("写入耍人人数IO错误");
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    LOGGER.error("关闭刷卡文件流错误");
                }
            }
        }*/


    }
}
