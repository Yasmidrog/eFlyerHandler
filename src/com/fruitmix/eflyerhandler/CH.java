package com.fruitmix.eflyerhandler;

import org.sparkle.janette.client.ClientHandler;
import org.sparkle.jbind.JBinD;

/**
 * Created by yasmidrog on 18.10.15.
 */
public class CH extends ClientHandler {
    public CH(){

    }
    public void in(JBinD data){
        MainActivity.gps=data.getPart("GPS").getDataAsString();
    }
    public JBinD out(){return MainActivity.sensor.getBind();}
}
