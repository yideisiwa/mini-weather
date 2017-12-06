package cn.edu.pku.hongbenyun.miniweather;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Mike_Hong on 2017/12/6.
 */

public class MyService extends Service {

    String cityCode;
    private final IBinder mBinder = new MyBinder();


    public class MyBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }
    @Override
    public IBinder onBind(Intent arg0) {
            Log.d("Threading", "bingding...");
            //new DoBackgroundTask().execute();
            new Thread(new DownThread()).start();//启动线程
            return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
            // We want this service to continue running until it is explicitly
            // stopped, so return sticky.

            Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
            Log.d("Threading", "starting...");
            new Thread(new DownThread()).start();//启动线程
            return START_STICKY;
    }

    class DownThread implements Runnable{

        public void run() {
            while(true){
                try {
                    Thread.sleep(5000);
                    Message msg = new Message();
                    msg.what = 2;
                    Log.d("Threading", "update");
                    MainActivity.mHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public class DoBackgroundTask extends AsyncTask<Void, Integer, Void> {
        protected Void doInBackground(Void... params) {
            for (int i = 0; ; i++) {
                //---report its progress---
                publishProgress(i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.d("Threading", e.getLocalizedMessage());
                }
            }
        }
        protected void onProgressUpdate(Integer... progress) {

            Log.d("Threading", "updating...");
        }
    }
}

