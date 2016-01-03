package com.adobe.phonegap.push;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by madosaja on 16. 1. 4..
 */
public class MaintenanceService extends Service {
    private static final String LOG_TAG = "MaintenanceService";
    private static int REBOOT_DELAY_TIMER = 10 * 1000;
    private static int CHECK_DELAY_TIMER = 30 * 60 * 1000;

    public class LocalBinder extends Binder {
        public MaintenanceService getService() {
            return MaintenanceService.this;
        }
    }
    private final IBinder mBinder = new LocalBinder();

    private static final Map<String, String> draftMap = new HashMap<String, String>();
    static {
        draftMap.put("draft10", "org.java_websocket.drafts.Draft_10");
        draftMap.put("draft17", "org.java_websocket.drafts.Draft_17");
        draftMap.put("draft75", "org.java_websocket.drafts.Draft_75");
        draftMap.put("draft76", "org.java_websocket.drafts.Draft_76");
    }

    /**
     *
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return mBinder;
    }

    /**
     * 서비스가 만들어졌을 때
     */
    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate");
        unregisterRestartAlarm();
        super.onCreate();
    }

    /**
     * 서비스가 종료될 때
     */
    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        registerRestartAlarm();
        stopForeground(true);
        super.onDestroy();
    }

    /**
     * 서비스를 시작한다면
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId){
        Log.d(LOG_TAG, "onStart");
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        startForeground(0, new Notification());
        /*
        if(!checkThread.isAlive()) {
            checkThread.start();
        }
        //*/
        return START_STICKY;
    }

    /**
     * 서비스가 죽었을 때 다시 살리도록 알람설정
     */
    private void registerRestartAlarm(){
        Intent intent = new Intent(MaintenanceService.this, RestartService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        }
        intent.setAction(RestartService.ACTION_RESTART_MAINTENANCESERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(MaintenanceService.this, 0, intent, 0);

        long firstTime = SystemClock.elapsedRealtime();
        firstTime += REBOOT_DELAY_TIMER;

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, REBOOT_DELAY_TIMER, sender);
    }

    /**
     * 알람설정 해제
     */
    private void unregisterRestartAlarm(){
        Intent intent = new Intent(MaintenanceService.this, RestartService.class);
        intent.setAction(RestartService.ACTION_RESTART_MAINTENANCESERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(MaintenanceService.this, 0, intent, 0);

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(sender);
    }

    private int counter;

    private Thread checkThread = new Thread(){
        public void run(){
            counter = 0;
            while(true) {
                Log.d(LOG_TAG, "checkThread - "+Integer.toString(counter));
                counter++;
                //unregisterRestartAlarm();
                //registerRestartAlarm();
                SystemClock.sleep(CHECK_DELAY_TIMER);
            }
        }
    };

}
