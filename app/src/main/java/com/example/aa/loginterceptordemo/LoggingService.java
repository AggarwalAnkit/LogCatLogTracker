package com.example.aa.loginterceptordemo;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.SocketHandler;

/**
 * Created by aa on 15/03/17.
 */

public class LoggingService extends Service {

    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;
    private String mName;
    private boolean mRedelivery;
    private Boolean mIsHandlerAdded = false;

    private static final Logger LOGGER = Logger.getGlobal();
    private static final String processId = Integer.toString(android.os.Process.myPid());
    public static final String TAG = LoggingService.class.getSimpleName();

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            onHandleIntent((Intent) msg.obj);
            //don't stop this service. it will get killed with its app's process
            //stopSelf(msg.arg1);
        }
    }

    public LoggingService() {
        this(TAG);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    private LoggingService(String name) {
        super();
        mName = name;
    }

    /**
     * Sets intent redelivery preferences.  Usually called from the constructor
     * with your preferred semantics.
     * <p>
     * <p>If enabled is true,
     * {@link #onStartCommand(Intent, int, int)} will return
     * {@link Service#START_REDELIVER_INTENT}, so if this process dies before
     * {@link #onHandleIntent(Intent)} returns, the process will be restarted
     * and the intent redelivered.  If multiple Intents have been sent, only
     * the most recent one is guaranteed to be redelivered.
     * <p>
     * <p>If enabled is false (the default),
     * {@link #onStartCommand(Intent, int, int)} will return
     * {@link Service#START_NOT_STICKY}, and if the process dies, the Intent
     * dies along with it.
     */
    public void setIntentRedelivery(boolean enabled) {
        mRedelivery = enabled;
    }

    @Override
    public void onCreate() {
        // TODO: It would be nice to have an option to hold a partial wakelock
        // during processing, and to have a static startService(Context, Intent)
        // method that would launch the service & hand off a wakelock.

        super.onCreate();
        HandlerThread thread = new HandlerThread("IntentService[" + mName + "]");
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        LOGGER.setLevel(Level.ALL);
        new AddLogHandlerTask(this).execute();
    }

    /**
     * You should not override this method for your IntentService. Instead,
     * override {@link #onHandleIntent}, which the system calls when the IntentService
     * receives a start request.
     *
     * @see android.app.Service#onStartCommand
     */
    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return mRedelivery ? START_REDELIVER_INTENT : START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        mServiceLooper.quit();
    }

    /**
     * Unless you provide binding for your service, you don't need to implement this
     * method, because the default implementation returns null.
     *
     * @see android.app.Service#onBind
     */
    @Override
    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else.
     * When all requests have been handled, the IntentService stops itself,
     * so you should not call {@link #stopSelf}.
     *
     * @param intent The value passed to {@link
     *               android.content.Context#startService(Intent)}.
     *               This may be null if the service is being restarted after
     *               its process has gone away; see
     *               {@link android.app.Service#onStartCommand}
     *               for details.
     */
    @WorkerThread
    private void onHandleIntent(@Nullable Intent intent) {

        synchronized (this) {
            while (mIsHandlerAdded != null && !mIsHandlerAdded) {
                Log.d(TAG, "Came here.");
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.d(TAG, "wait interrupted.");
                }
            }
        }

        if (mIsHandlerAdded == null) {
            return;
        }

        try {
            String[] command = new String[]{"logcat", "-v", "threadtime"};

            Process process = Runtime.getRuntime().exec(command);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(processId)) {
                    LOGGER.log(Level.ALL, line);
                }
            }
        } catch (IOException ex) {
            Log.e(TAG, "start failed", ex);
        }
    }

    public void setIsHandlerAdded(Boolean isHandlerAdded) {
        mIsHandlerAdded = isHandlerAdded;
    }

    public void startLogging() {
        mServiceHandler.sendEmptyMessage(0);
    }

    public static class AddLogHandlerTask extends AsyncTask<Void, Void, SocketHandler> {

        private WeakReference<LoggingService> mLoggingServiceWeakReference;

        public AddLogHandlerTask(LoggingService loggingService) {
            mLoggingServiceWeakReference = new WeakReference<>(loggingService);
        }

        @Override
        protected SocketHandler doInBackground(Void... params) {
            SocketHandler socketHandler = null;
            try {
                socketHandler = new SocketHandler("10.0.2.2", 6969);
                //socketHandler = new SocketHandler("10.0.2.2", 3000);
                socketHandler.setFormatter(new SimpleFormatter());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return socketHandler;
        }

        @Override
        protected void onPostExecute(SocketHandler socketHandler) {

            if (socketHandler != null) {
                LOGGER.addHandler(socketHandler);
            }

            if (mLoggingServiceWeakReference != null) {
                LoggingService loggingService = mLoggingServiceWeakReference.get();
                if (loggingService != null) {
                    if (socketHandler != null) {
                        loggingService.setIsHandlerAdded(true);
                    } else {
                        loggingService.setIsHandlerAdded(null);
                    }
                    loggingService.startLogging();
                }
            }
        }
    }
}
