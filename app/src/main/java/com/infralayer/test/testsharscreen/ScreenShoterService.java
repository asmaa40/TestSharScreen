package com.infralayer.test.testsharscreen;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ScreenShoterService extends Service {



    private boolean isRunning  = false;

    @Override
    public void onCreate() {
        Log.i("**************", "Service onCreate");

        isRunning = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {


                //Your logic that service will perform will be placed here
                //In this example we are just looping and waits for 1000 milliseconds in each loop.
                for (int i = 0; i < 5; i++) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }

                    if(isRunning){
                        Log.i("************8", "Service running");
                    }
                }

                //Stop service once it finishes its task
                stopSelf();
            }
        }).start();
        return Service.START_STICKY;

    }
}
