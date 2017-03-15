package com.example.aa.loginterceptordemo;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.SocketHandler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*try {
            Logger.getGlobal().addHandler(new SocketHandler("10.0.2.2", 6969));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("Main Activity", "Debug Log");*/
        //TODO uncomment this for testing
        // new LogHandlerTask().execute();

        startService(new Intent(this, LoggingService.class));
    }

    public static class LogHandlerTask extends AsyncTask<Void, Void, Void> {

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

            /*LogRecord logRec = new LogRecord(Level.INFO, "Log will be recorded");
            socketHandler.publish(logRec);
            LOGGER.info("socket handler info message");*/

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d("Main Activity", "Log Handler Added");
            /*LOGGER.config("TEST LOG MESSAGE");
            socketHandler.flush();*/
        }
    }
}
