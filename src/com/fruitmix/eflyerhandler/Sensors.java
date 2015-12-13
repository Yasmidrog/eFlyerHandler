package com.fruitmix.eflyerhandler;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.sparkle.jbind.JBinD;
import org.sparkle.jbind.Part;
import org.sparkle.jcfg.JCFG;
import org.sparkle.jcfg.Writer;

/**
 * Created by yasmidrog on 12.10.15.
 */
public class Sensors {
    /*
    обновление данных о датчиках
     */
    private JCFG config=new JCFG();
    String curentData; //текущая информация в виде строки
    private  SensorManager mSensorManager;
    private  Sensor mAccelerometer;
    private Context context;
    public Sensors(Context c,SensorEventListener listener){
        context=c;
        onCreate(listener);
    }
    protected void onCreate(SensorEventListener listener) {
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(listener, mAccelerometer, 3);
    }
    public void onSensorChanged(SensorEvent event) {
        //обновить данные о каждлм датчике
    	try{
        config.set(event.sensor.getName().split("	")[2]+"X",event.values[0]);
        config.set(event.sensor.getName().split("	")[2]+"Y",event.values[1]);
        config.set(event.sensor.getName().split("	")[2]+"Z",event.values[2]);
        curentData =Writer.writeToString(config);
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }

    public JCFG getConfig(){
        return config;
    }

   public JBinD getBind(){
       //сформировать JBind для отправки
       JBinD b=new JBinD();
       try {
           b.addPart(new Part("sensors", config));

       } catch (Exception e){
           e.printStackTrace();
       }
       return b;
   }
    public String getConfigAsString(){
        return Writer.writeToString(config);
    }
}
