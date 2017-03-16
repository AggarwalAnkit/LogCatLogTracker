package com.example.aa.loginterceptordemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO uncomment this for using java.util.Logger
        // new LogHandlerTask().execute();

        startService(new Intent(this, LoggingService.class));

        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(5);
        scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Log.d(MainActivity.class.getSimpleName(), "I am a debug test log");
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    /*public static class LogHandlerTask extends AsyncTask<Void, Void, Void> {

        //private static final Logger LOGGER = Logger.getLogger(MainActivity.class.getName());
        private static final Logger LOGGER = Logger.getGlobal();
        private SocketHandler socketHandler;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                socketHandler = new SocketHandler("10.0.2.2", 6969);
                socketHandler.setFormatter(new SimpleFormatter());
                LOGGER.setLevel(Level.ALL);
                LOGGER.addHandler(socketHandler);
            } catch (IOException e) {
                e.printStackTrace();
            }
            LOGGER.log(Level.CONFIG, "TEST");

            *//*LogRecord logRec = new LogRecord(Level.INFO, "Log will be recorded");
            socketHandler.publish(logRec);
            LOGGER.info("socket handler info message");*//*

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d("Main Activity", "Log Handler Added");
            *//*LOGGER.config("TEST LOG MESSAGE");
            socketHandler.flush();*//*
        }
    }*/
}
