package cn.ccf.alarm;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * 播放语音提示
 *
 * @author Hao
 *
 */
public class Broadcast {

    public static void play(String filePath) {

        try {
            //获取音频输入流
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath));
            //获取音频编码对象
            AudioFormat audioFormat = audioInputStream.getFormat();
            //设置数据输入
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat, AudioSystem.NOT_SPECIFIED);
            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(audioFormat);
            sourceDataLine.start();

            //声卡输出
            int count;
            byte tempBuffer[] = new byte[1024];
            while((count = audioInputStream.read(tempBuffer,0,tempBuffer.length))!=-1){
                if(count > 0){
                    sourceDataLine.write(tempBuffer, 0, count);
                }
            }

            sourceDataLine.drain();
            sourceDataLine.close();


        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }
}
