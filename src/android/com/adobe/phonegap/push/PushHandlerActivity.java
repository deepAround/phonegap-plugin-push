package com.adobe.phonegap.push;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

public class PushHandlerActivity extends Activity implements PushConstants {
    private static String LOG_TAG = "PushPlugin_PushHandlerActivity";

    /*
     * this activity will be started if the user touches a notification that we own.
     * We send it's data off to the push plugin for processing.
     * If needed, we boot up the main activity to kickstart the application.
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        GCMIntentService gcm = new GCMIntentService();
        gcm.setNotification(getIntent().getIntExtra(NOT_ID, 0), "");
        clearBadge();
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "onCreate");

        boolean isPushPluginActive = PushPlugin.isActive();
        processPushBundle(isPushPluginActive);

        finish();

        if (!isPushPluginActive) {
            forceMainActivityReload();
        }
    }

    /**
     * Takes the pushBundle extras from the intent,
     * and sends it through to the PushPlugin for processing.
     */
    private void processPushBundle(boolean isPushPluginActive) {
        Bundle extras = getIntent().getExtras();

        if (extras != null)	{
            Bundle originalExtras = extras.getBundle(PUSH_BUNDLE);

            originalExtras.putBoolean(FOREGROUND, false);
            originalExtras.putBoolean(COLDSTART, !isPushPluginActive);
            originalExtras.putString(CALLBACK, extras.getString("callback"));

            PushPlugin.sendExtras(originalExtras);
        }
    }

    /**
     * Forces the main activity to re-launch if it's unloaded.
     */
    private void forceMainActivityReload() {
        PackageManager pm = getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(getApplicationContext().getPackageName());
        startActivity(launchIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    /**
     * Persist the badge of the app icon so that `getBadge` is able to return
     * the badge number back to the client.
     *
     * @param badge
     *      The badge of the app icon
     */
    public void saveBadge (int badge) {
        String packageName = getApplicationContext().getPackageName();
        Intent launchIntent = getApplicationContext().getPackageManager().getLaunchIntentForPackage(packageName);
        String className = launchIntent.getComponent().getClassName();

        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", badge);

        Log.d(LOG_TAG, "saveBadge: " + badge + ", "+packageName+", "+className);
        // 메인 메뉴에 나타나는 어플의  패키지 명
        intent.putExtra("badge_count_package_name", packageName);
        // 메인메뉴에 나타나는 어플의 클래스 명
        intent.putExtra("badge_count_class_name", className);
        sendBroadcast(intent);
    }

    /**
     * Clears the badge of the app icon.
     */
    public void clearBadge() {
        GCMIntentService.badgeCount = 0;
        saveBadge(0);
    }
}
