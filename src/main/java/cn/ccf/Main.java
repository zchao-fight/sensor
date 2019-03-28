package cn.ccf;

import cn.ccf.alarm.AlarmLight;
import cn.ccf.alarm.IOModule;
import cn.ccf.bean.Person;
import cn.ccf.common.SystemConf;
import cn.ccf.swipe.SwipeCardIn;
import cn.ccf.swipe.SwipeCardOut;
import cn.ccf.utils.TCPServer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {


        Person person = new Person();

        startSwipeCardModule(person); // 启动刷卡模块

        startSensor(Gas.class, "gas");
        startSensor(Resistance.class, "resistance");
        startSensor(Dust.class, "dust");
        startSensor(LaserRange.class, "laser");
        startSensor(Humiture.class, "humiture");


        TCPServer tcpServer = new TCPServer(person, Integer.parseInt(SystemConf.get("swipe.tcp.port")));
        tcpServer.startServer();





      /*
        ArrayList<String> port = SerialPortManager.findPort();
        port.forEach(System.out::print);
      */
    }

    private static Boolean isMathced(String ports, String nums) {
        return StringUtils.isNotEmpty(ports) && StringUtils.isNotEmpty(nums)
                && ports.split(",").length == nums.split(",").length;
    }

    private static <T extends Runnable> void startSensor(Class<T> clazz, String type) {

        if (StringUtils.equals(type, "humiture")) {
            //-------温湿度---------------------
            String humiturePorts = SystemConf.get("humiture.port");
            String channelCounts = SystemConf.get("channel.count");
            String humitureNums = SystemConf.get("humiture.number");

            if (isMathced(humiturePorts, channelCounts)) {
                String[] humiturePortArray = humiturePorts.split(",");
                String[] channelCountArray = channelCounts.split(",");
                String[] humitureNumArray = humitureNums.split("#");
                for (int i = 0; i < humiturePortArray.length; i++) {
                    Humiture humiture = new Humiture(humiturePortArray[i], channelCountArray[i], humitureNumArray[i]);
                    Thread humitureThread = new Thread(humiture);
                    humitureThread.start();
                }
            }
            return;
        }


        if (StringUtils.equals(type, "laser")) {
            //-------激光---------------------
            String laserPorts = SystemConf.get("laser.port");
            String laserNums = SystemConf.get("laser.number");
            if (isMathced(laserPorts, laserNums)) {
                String[] laserPortArray = laserPorts.split(",");
                String[] laserNumArray = laserNums.split(",");
                for (int i = 0; i < laserPortArray.length; i++) {
                    LaserRange laserRange = new LaserRange(laserPortArray[i], laserNumArray[i]);
                    Thread laserThread = new Thread(laserRange);
                    laserThread.start();
                }
            }
            return;
        }


        String ports = SystemConf.get(type + ".port");
        String nums = SystemConf.get(type + ".number");
        String addrs = SystemConf.get(type + ".addr");

        if (isMathced(ports, nums)) {
            String[] PortArray = ports.split(",");
            String[] NumArray = nums.split(",");
            String[] AddrArray = addrs.split(",");
            try {
                Constructor<T> constructor = clazz.getConstructor(String.class, String.class, String.class);
                for (int i = 0; i < PortArray.length; i++) {
                    T t = constructor.newInstance(PortArray[i], NumArray[i], AddrArray[i]);
                    Thread sensorThread = new Thread(t);
                    sensorThread.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            LOGGER.info("未配置" + type);
        }
    }

    public static void startSwipeCardModule(Person person) {
        AlarmLight alarmLight = new AlarmLight(IOModule.getSerialPort());
        SwipeCardIn swipeCardIn = new SwipeCardIn(person);
        SwipeCardOut swipeCardOut = new SwipeCardOut(person);

        Thread alarmLightThread = new Thread(alarmLight);
        Thread swipeInThread = new Thread(swipeCardIn);
        Thread swipeOutThread = new Thread(swipeCardOut);

        alarmLightThread.start();
        swipeInThread.start();
        swipeOutThread.start();

        //-------红外---------------------
        Infrared infrared = new Infrared(person, IOModule.getSerialPort());
        Thread infraredThread = new Thread(infrared);
        infraredThread.start();
    }
}



/*
        //-------激光---------------------
        String laserPorts = SystemConf.get("laser.port");
        String laserNums = SystemConf.get("laser.number");
        if (isMathced(laserPorts, laserNums)) {
            String[] laserPortArray = laserPorts.split(",");
            String[] laserNumArray = laserNums.split(",");
            for (int i = 0; i < laserPortArray.length; i++) {
                LaserRange laserRange = new LaserRange(laserPortArray[i], laserNumArray[i]);
                Thread laserThread = new Thread(laserRange);
                laserThread.start();
            }
        }*/



/*
         //-------接地电阻---------------------
         String resistancePorts = SystemConf.get("resistance.port");
         String resistanceNums = SystemConf.get("resistance.number");
         String resistanceAddrs = SystemConf.get("resistance.addr");
         if (isMathced(resistancePorts, resistanceNums)) {
         String[] resistancePortArray = resistancePorts.split(",");
         String[] resistanceNumArray = resistanceNums.split(",");
         String[] resistanceAddrArray = resistanceAddrs.split(",");
         for (int i = 0; i < resistancePortArray.length; i++) {
         Resistance resistance = new Resistance(resistancePortArray[i], resistanceNumArray[i], resistanceAddrArray[i]);
         Thread resistanceThread = new Thread(resistance);
         resistanceThread.start();
         }
         } else {
         LOGGER.info("未配置接地电阻");
         }

 //-------粉尘---------------------
 String dustPorts = SystemConf.get("dust.port");
 String dustNums = SystemConf.get("dust.number");
 String dustAddrs = SystemConf.get("dust.addr");

 if (isMathced(dustPorts, dustNums)) {
 String[] dustPortArray = dustPorts.split(",");
 String[] dustNumArray = dustNums.split(",");
 String[] dustAddrArray = dustAddrs.split(",");
 for (int i = 0; i < dustPortArray.length; i++) {
 Dust dust = new Dust(dustPortArray[i], dustNumArray[i], dustAddrArray[i]);
 Thread dustThread = new Thread(dust);
 dustThread.start();
 }
 } else {
 LOGGER.info("未配置粉尘传感器");
 }

  //-------气体---------------------
 String gasPorts = SystemConf.get("gas.port");
 String gasNums = SystemConf.get("gas.number");
 String gasAddrs = SystemConf.get("gas.addr");
 if (isMathced(gasPorts, gasNums)) {
 String[] gasPortArray = gasPorts.split(",");
 String[] gasNumArray = gasNums.split(",");
 String[] gasAddrArray = gasAddrs.split(",");
 for (int i = 0; i < gasPortArray.length; i++) {
 Gas gas = new Gas(gasPortArray[i], gasNumArray[i], gasAddrArray[i]);
 Thread gasThread = new Thread(gas);
 gasThread.start();
 }
 }

 */
