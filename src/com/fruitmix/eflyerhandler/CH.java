package com.fruitmix.eflyerhandler;

import org.sparkle.janette.client.ClientHandler;
import org.sparkle.jbind.JBinD;

/*
 *Обмен данными
 */
public class CH extends ClientHandler {
    public CH(){

    }
    //обновить данные с GPS
    public void in(JBinD data){
        MainActivity.gps=data.getPart("GPS").getDataAsString();
    }
    //послать данные о датчиках
    public JBinD out(){return MainActivity.sensor.getBind();}
}
