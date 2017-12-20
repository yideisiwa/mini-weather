package cn.edu.pku.hongbenyun.miniweather;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.edu.pku.hongbenyun.bean.TodayWeather;
import cn.edu.pku.hongbenyun.bean.Weather;

/**
 * Created by Mike_Hong on 2017/12/6.
 */

public class MyService extends Service {

    private static final int UPDATE_TODAY_WEATHER = 1;
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

            Log.d("Threading", "starting...");
            new Thread(new DownThread()).start();//启动线程
            return START_STICKY;
    }

    class DownThread implements Runnable{

        public void run() {
            while(true){
                try {
                    Thread.sleep(5000);
                    queryWeatherCode(cityCode);
//                    Message msg = new Message();
//                    msg.what = 2;
//                    String sd = cityCode;
//                    Log.d("Threading", "update");
//                    MainActivity.mHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     *
     * @param cityCode
     */
    private void queryWeatherCode(String cityCode)  {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather", address);

        //创建新线程进行网络操作，防止阻塞主线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con=null;
                TodayWeather todayWeather = null;
                try{
                    //发起请求，获取网络数据保存到response中
                    URL url = new URL(address);
                    con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while((str=reader.readLine()) != null){
                        response.append(str);
                        Log.d("myWeather", str);
                    }

                    String responseStr=response.toString();
                    Log.d("myWeather", responseStr);

                    //解析从网络获取的response字符串，获取自己想要的天气数据保存。
                    todayWeather = parseXML(responseStr);
                    if (todayWeather != null) {
                        //向主线程发送信息
                        Log.d("myWeather", todayWeather.toString());
                        Message msg =new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj=todayWeather;
                        //mHandler.sendMessage(msg);
                        MainActivity.mHandler.sendMessageDelayed(msg,2000);
                        Intent intent =new Intent();
                        intent.putExtra("type",todayWeather.getType());
                        intent.putExtra("temperature",todayWeather.getHigh()+"~"+todayWeather.getLow());
                        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
                        sendBroadcast(intent);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(con != null){
                        con.disconnect();
                    }
                }
            }
        }).start();
    }


    private TodayWeather parseXML(String xmldata){
        TodayWeather todayWeather = null;
        int fengxiangCount=0;
        int fengliCount =0;
        int dateCount=0;
        int highCount =0;
        int lowCount=0;
        int typeCount =0;
        int weatherCount =0;
        int forecast_typeCount=0;
        int forecast_fengliCount=0;
        Weather weather =null;
        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather", "parseXML");
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    // 判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    // 判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        Log.d("qqq",xmlPullParser.getName());
                        if(xmlPullParser.getName().equals("resp")){
                            todayWeather= new TodayWeather();
                        }
                        if (todayWeather != null) {

                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                typeCount++;
                            }else if(xmlPullParser.getName().equals("weather")){
                                if(weatherCount ==0)
                                    weatherCount++;
                                else {
                                    weather = new Weather();
                                    todayWeather.getWeathers().add(weather);
                                    forecast_typeCount=0;
                                    forecast_fengliCount=0;
                                }
                            }else if(weather !=null) {

                                if(xmlPullParser.getName().equals("date"))
                                {
                                    eventType = xmlPullParser.next();
                                    weather.setDate(xmlPullParser.getText());
                                }
                                else if(xmlPullParser.getName().equals("type") && forecast_typeCount==0)
                                {
                                    eventType = xmlPullParser.next();
                                    weather.setType(xmlPullParser.getText());
                                    forecast_typeCount++;
                                }
                                else if(xmlPullParser.getName().equals("high"))
                                {
                                    eventType = xmlPullParser.next();
                                    weather.setHigh(xmlPullParser.getText().substring(2).trim());
                                }
                                else if(xmlPullParser.getName().equals("low"))
                                {
                                    eventType = xmlPullParser.next();
                                    weather.setLow(xmlPullParser.getText().substring(2).trim());
                                }
                                else if(xmlPullParser.getName().equals("fengli") && forecast_fengliCount ==0)
                                {
                                    eventType = xmlPullParser.next();
                                    weather.setFengli(xmlPullParser.getText());
                                    forecast_fengliCount++;
                                }
                            }

                        }

                        break;


                    // 判断当前事件是否为标签元素结束事件
                    case XmlPullParser.END_TAG:
                        break;
                }
                // 进入下一个元素并触发相应事件
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return todayWeather;
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

