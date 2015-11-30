package com.fruitmix.eflyerhandler;



import org.sparkle.janette.client.ClientConnection;
import android.os.AsyncTask;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.view.View.OnClickListener;
import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import org.freedesktop.gstreamer.*;


public class MainActivity extends Activity implements SurfaceHolder.Callback,SensorEventListener {
    private native void nativeInit();     // Initialize native code, build pipeline, etc
    private native void nativeFinalize(); // Destroy pipeline and shutdown native code
    private native void nativePlay();     // Set pipeline to PLAYING
    private native void nativePause();    // Set pipeline to PAUSED
    private static native boolean nativeClassInit(); // Initialize native class: cache Method IDs for callbacks
    private native void nativeSurfaceInit(Object surface);
    private native void nativeSurfaceFinalize();
    private long native_custom_data;      // Native code will use this to keep private data

    private boolean is_playing_desired;   // Whether the user asked to go to PLAYING

    static Sensors sensor;
    static String gps="GPS";
    AsyncTask<Void,Void,Void> sendtask;
    ClientConnection cc;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Initialize GStreamer and warn if it fails
        try {
            GStreamer.init(this);
        } catch (Exception e) {
           e.printStackTrace();
            return;
        }

        setContentView(R.layout.activity_main);
        sensor = new Sensors(getBaseContext(), this);  
        Button play = (Button) this.findViewById(R.id.button_play);
        play.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                is_playing_desired = true;
                nativePlay();
            }
        });

        Button pause = (Button) this.findViewById(R.id.button_stop);
        pause.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                is_playing_desired = false;
                nativePause();
            }
        });
        ((Button)findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sendtask != null) {
                    cc.disconnect();
                    sendtask.cancel(true);
                }
                sendtask = createTask();
                sendtask.execute();
            }
        });
        SurfaceView sv = (SurfaceView) this.findViewById(R.id.surfaceView1);
        SurfaceHolder sh = sv.getHolder();
        sh.addCallback(this);

        if (savedInstanceState != null) {
            is_playing_desired = savedInstanceState.getBoolean("playing");
            Log.i ("GStreamer", "Activity created. Saved state is playing:" + is_playing_desired);
        } else {
            is_playing_desired = false;
            Log.i ("GStreamer", "Activity created. There is no saved state, playing: false");
        }

        // Start with disabled buttons, until native code is initialized
        this.findViewById(R.id.button_play).setEnabled(false);
        this.findViewById(R.id.button_stop).setEnabled(false);

        nativeInit();
    }

    protected void onSaveInstanceState (Bundle outState) {
        Log.d ("GStreamer", "Saving state, playing:" + is_playing_desired);
        outState.putBoolean("playing", is_playing_desired);
    }

    protected void onDestroy() {
        nativeFinalize();
        super.onDestroy();
    }

    // Called from native code. This sets the content of the TextView from the UI thread.
    private void setMessage(final String message) {
        final EditText tv = (EditText) this.findViewById(R.id.editText1);
        runOnUiThread (new Runnable() {
          public void run() {
            tv.setText(message);
          }
        });
    }

    // Called from native code. Native code calls this once it has created its pipeline and
    // the main loop is running, so it is ready to accept commands.
    private void onGStreamerInitialized () {
        Log.i ("GStreamer", "Gst initialized. Restoring state, playing:" + is_playing_desired);
        // Restore previous playing state
        if (is_playing_desired) {
            nativePlay();
        } else {
            nativePause();
        }

        // Re-enable buttons, now that GStreamer is initialized
        final Activity activity = this;
        runOnUiThread(new Runnable() {
            public void run() {
                activity.findViewById(R.id.button_play).setEnabled(true);
                activity.findViewById(R.id.button_stop).setEnabled(true);
            }
        });
    }

    static {
        System.loadLibrary("gstreamer_android");
        System.loadLibrary("eFlyerHandler");
        nativeClassInit();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        Log.d("GStreamer", "Surface changed to format " + format + " width "
                + width + " height " + height);
        nativeSurfaceInit (holder.getSurface());
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("GStreamer", "Surface created: " + holder.getSurface());
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("GStreamer", "Surface destroyed");
        nativeSurfaceFinalize ();
    }
    public void onSensorChanged(SensorEvent event){
        sensor.onSensorChanged(event);
        ((TextView)findViewById(R.id.GPS)).setText(gps);
    }
    public void onAccuracyChanged(Sensor a1,int a2){}

    private  AsyncTask<Void,Void,Void> createTask(){
    	
        String ip=((EditText)findViewById(R.id.iptext)).getText().toString();
        int port=Integer.valueOf(((EditText) findViewById(R.id.porttext)).
                getText().toString());
        cc= new ClientConnection(ip, port, CH.class, 200);
    
        return  new AsyncTask<Void,Void,Void>(){
            protected Void doInBackground(Void... pars){
                try {
                    cc.connect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }
}
