package com.example.aa.loginterceptordemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Created by aa on 15/03/17.
 */

public class LoggingService extends Service {

    private Socket mSocket;
    private static final String processId = Integer.toString(android.os.Process.myPid());
    private static final String TAG = LoggingService.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        new LoggingThread().start();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mSocket != null && mSocket.connected()) {
            mSocket.disconnect();
            mSocket.close();
            mSocket = null;
        }
        super.onDestroy();
    }

    @Override
    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class LoggingThread extends Thread {
        public LoggingThread() {
            super("[" + TAG + "]");
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            try {
                mSocket = IO.socket("http://10.0.2.2:3000");
                mSocket.connect();
            } catch (URISyntaxException e) {
                e.printStackTrace();
                stopSelf();
                return;
            }

            try {
                String[] command = new String[]{"logcat", "-v", "threadtime"};
                java.lang.Process process = Runtime.getRuntime().exec(command);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains(processId)) {
                        mSocket.emit("new message", line);
                    }
                }
            } catch (IOException ex) {
                Log.e(TAG, "start failed", ex);
            }
        }
    }
}
