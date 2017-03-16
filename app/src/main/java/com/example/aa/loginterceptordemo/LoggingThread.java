package com.example.aa.loginterceptordemo;

import android.os.Process;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Created by aa on 16/03/17.
 */

public class LoggingThread extends Thread {

    private String mProcessId;
    private boolean mFinish;
    private static final String TAG = LoggingThread.class.getSimpleName();

    public LoggingThread(String name, int processId) {
        super("[" + name + "]");
        mProcessId = String.valueOf(processId);
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        Socket socket;
        try {
            socket = IO.socket("http://10.0.2.2:3000");
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        try {
            String[] command = new String[]{"logcat", "-v", "threadtime"};
            java.lang.Process process = Runtime.getRuntime().exec(command);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while (!mFinish && (line = bufferedReader.readLine()) != null) {
                if (line.contains(mProcessId)) {
                    socket.emit("new message", line);
                }
            }
        } catch (IOException ex) {
            Log.e(TAG, "start failed", ex);
        }

        if (socket.connected()) {
            socket.disconnect();
            socket.close();
            socket = null;
        }
    }

    public void finish() {
        mFinish = true;
    }
}
