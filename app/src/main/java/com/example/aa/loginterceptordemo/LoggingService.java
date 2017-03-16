package com.example.aa.loginterceptordemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by aa on 15/03/17.
 */

public class LoggingService extends Service {

    private LoggingThread mLoggingThread;

    @Override
    public void onCreate() {
        super.onCreate();
        mLoggingThread = new LoggingThread(LoggingService.class.getSimpleName(), android.os.Process.myPid());
        mLoggingThread.start();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mLoggingThread != null) {
            mLoggingThread.finish();
        }
        mLoggingThread = null;
        super.onDestroy();
    }

    @Override
    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }
}
