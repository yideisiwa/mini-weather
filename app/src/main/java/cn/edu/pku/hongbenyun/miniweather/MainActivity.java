package cn.edu.pku.hongbenyun.miniweather;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import cn.edu.pku.hongbenyun.bean.City;
import cn.edu.pku.hongbenyun.bean.TodayWeather;
import cn.edu.pku.hongbenyun.bean.Weather;
import cn.edu.pku.hongbenyun.util.NetUtil;

public class MainActivity extends Activity implements View.OnClickListener, ViewPager.OnPageChangeListener
{
    private static final int UPDATE_TODAY_WEATHER = 1;

    private ProgressBar progressBar;
    private ImageView mUpdateBtn;
    private ImageView mCitySelect;
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv,
            temperatureTv, climateTv, windTv, city_name_Tv;
    private ImageView weatherImg, pmImg;

    private LinearLayout linearLayout1;
    private LinearLayout linearLayout2;

    private  ViewPagerAdapter vpAdapter;
    private ViewPager vp;
    private List<View> views;

    private ImageView[] dots;
    private int[] ids = {R.id.iv1,R.id.iv2};

    private String cityCode;

    //实现天气文字与图片的对应
    private final  static Map<String, Integer> weatherMap = new HashMap<String, Integer>();
    static {
        weatherMap.put("暴雪", R.drawable.biz_plugin_weather_baoxue);
        weatherMap.put("暴雨", R.drawable.biz_plugin_weather_baoyu);
        weatherMap.put("大暴雨", R.drawable.biz_plugin_weather_dabaoyu);
        weatherMap.put("大雪", R.drawable.biz_plugin_weather_daxue);
        weatherMap.put("大雨", R.drawable.biz_plugin_weather_dayu);
        weatherMap.put("多云", R.drawable.biz_plugin_weather_duoyun);
        weatherMap.put("雷阵雨", R.drawable.biz_plugin_weather_leizhenyu);
        weatherMap.put("雷阵雨冰雹", R.drawable.biz_plugin_weather_leizhenyubingbao);
        weatherMap.put("晴", R.drawable.biz_plugin_weather_qing);
        weatherMap.put("沙尘暴", R.drawable.biz_plugin_weather_shachenbao);
        weatherMap.put("特大暴雨", R.drawable.biz_plugin_weather_tedabaoyu);
        weatherMap.put("雾", R.drawable.biz_plugin_weather_wu);
        weatherMap.put("小雪", R.drawable.biz_plugin_weather_xiaoxue);
        weatherMap.put("小雨", R.drawable.biz_plugin_weather_xiaoyu);
        weatherMap.put("阴", R.drawable.biz_plugin_weather_yin);
        weatherMap.put("雨夹雪", R.drawable.biz_plugin_weather_yujiaxue);
        weatherMap.put("阵雪", R.drawable.biz_plugin_weather_zhenxue);
        weatherMap.put("阵雨", R.drawable.biz_plugin_weather_zhenyu);
        weatherMap.put("中雪", R.drawable.biz_plugin_weather_zhongxue);
        weatherMap.put("中雨", R.drawable.biz_plugin_weather_zhongyu);
    }

    //处理其他线程返回的数据
    public static MyHandler mHandler;


    class MyHandler extends Handler{

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    mUpdateBtn.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    break;
                case 2:
                    queryWeatherCode(cityCode);
                    break;
                default:
                    break;
            }

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);

        progressBar =(ProgressBar)findViewById(R.id.title_update_progress);
        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);

        mCitySelect = (ImageView)findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);

        mHandler = new MyHandler();

        //初始化各类控件
        initView();
        initViews();
        initDots();
        //从sharedPreference中读取保存的城市，更新当前城市天气
        getAndUpdateTodayWeather();

        Intent i = new Intent(MainActivity.this, MyService.class);
        bindService(i, connection, Context.BIND_AUTO_CREATE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String newCityCode= data.getStringExtra("cityCode");
            cityCode = newCityCode;
            Log.d("myWeather", "选择的城市代码为"+newCityCode);
            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                //选择城市后返回到显示天气界面，请求网络数据更新界面，并更新sharePreference
                Log.d("myWeather", "网络OK");
                queryWeatherCode(newCityCode);
                SharedPreferences settings
                        = (SharedPreferences)getSharedPreferences("config", MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("main_city_code", newCityCode);
                editor.commit();
            } else {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.title_city_manager)
        {
            //点击选择城市按钮，跳转到选择城市界面
            Intent i = new Intent(this, SelectCity.class);
            i.putExtra("cityName",cityTv.getText());
            startActivityForResult(i,1);
        }
        if (view.getId() == R.id.title_update_btn){
            //点击更新城市按钮，更新城市天气
            getAndUpdateTodayWeather();
        }
    }

    private void getAndUpdateTodayWeather()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        String cityCode = sharedPreferences.getString("main_city_code","101010100");
        this.cityCode = cityCode;
        Log.d("myWeather",cityCode);


        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
            Log.d("myWeather", "网络OK");
            queryWeatherCode(cityCode);
        }else
        {
            Log.d("myWeather", "网络挂了");
            Toast.makeText(MainActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
        }
    }

    /**
     *
     * @param cityCode
     */
    private void queryWeatherCode(String cityCode)  {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather", address);
        mUpdateBtn.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
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
                        mHandler.sendMessageDelayed(msg,2000);
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

    void initView(){
        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality);
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView) findViewById(R.id.temperature);
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        weatherImg = (ImageView) findViewById(R.id.weather_img);

        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");
    }

    void updateTodayWeather(TodayWeather todayWeather){
        int pm25 = 0;

        city_name_Tv.setText(todayWeather.getCity()+"天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime()+ "发布");
        humidityTv.setText("湿度："+todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25()==null?"N/A":todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality()==null?"N/A":todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh()+"~"+todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力:"+todayWeather.getFengli());

        if(todayWeather.getPm25()!=null)
            pm25 = Integer.parseInt(todayWeather.getPm25());
        if (pm25 >= 0)
        {
            if (pm25 < 51)
                pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
            else if (pm25 < 101)
                pmImg.setImageResource(R.drawable.biz_plugin_weather_51_100);
            else if (pm25 < 151)
                pmImg.setImageResource(R.drawable.biz_plugin_weather_101_150);
            else if (pm25 < 201)
                pmImg.setImageResource(R.drawable.biz_plugin_weather_151_200);
            else if (pm25 < 301)
                pmImg.setImageResource(R.drawable.biz_plugin_weather_201_300);
            else if (pm25 > 300)
                pmImg.setImageResource(R.drawable.biz_plugin_weather_greater_300);
        }
        if(todayWeather.getType()!=null)
            weatherImg.setImageResource(weatherMap.get(todayWeather.getType()));

        //更新未来六天天气界面
        linearLayout1.removeAllViews();
        linearLayout2.removeAllViews();
        for(int i=0;i<todayWeather.getWeathers().size() && i<2;i++)
        {
            Weather weather=todayWeather.getWeathers().get(i);
            View view= LayoutInflater.from(this).inflate(R.layout.weather_page_item, null);

            TextView dateTv = (TextView)view.findViewById(R.id.date);
            TextView temperatureTv = (TextView)view.findViewById(R.id.temperature);
            TextView climateTv = (TextView)view.findViewById(R.id.climate);
            TextView windTv = (TextView)view.findViewById(R.id.wind);
            ImageView weatherImg = (ImageView) view.findViewById(R.id.weather_img);

            dateTv.setText(weather.getDate()==null?"null":weather.getDate());
            temperatureTv.setText((weather.getHigh()==null?"null":weather.getHigh())+"~"+(weather.getLow()==null?"null":weather.getLow()));
            climateTv.setText(weather.getType()==null?"null":weather.getType());
            windTv.setText(weather.getFengli()==null?"null":weather.getFengli());
            if(weather.getType()!=null)
                weatherImg.setImageResource(weatherMap.get(weather.getType()));
            else
                weatherImg.setImageResource(weatherMap.get(weather.getType()));
            linearLayout1.addView(view);
        }

        for(int i=2;i<todayWeather.getWeathers().size() && i<4;i++)
        {
            Weather weather=todayWeather.getWeathers().get(i);
            View view= LayoutInflater.from(this).inflate(R.layout.weather_page_item, null);

            TextView dateTv = (TextView)view.findViewById(R.id.date);
            TextView temperatureTv = (TextView)view.findViewById(R.id.temperature);
            TextView climateTv = (TextView)view.findViewById(R.id.climate);
            TextView windTv = (TextView)view.findViewById(R.id.wind);
            ImageView weatherImg = (ImageView) view.findViewById(R.id.weather_img);

            dateTv.setText(weather.getDate());
            temperatureTv.setText(weather.getHigh()+"~"+weather.getLow());
            climateTv.setText(weather.getType());
            windTv.setText(weather.getFengli());
            Log.d("qqq",weather.getFengli());
            if(weather.getType()!=null)
                weatherImg.setImageResource(weatherMap.get("晴"));

            linearLayout2.addView(view);
        }



        Toast.makeText(MainActivity.this,"更新成功！",Toast.LENGTH_SHORT).show();

    }

    public void initDots()
    {
        dots = new ImageView[views.size()];
        for(int i=0;i<dots.length;i++)
        {
            dots[i]=(ImageView)findViewById(ids[i]);
        }
    }
    public void initViews()
    {
        LayoutInflater inflater = LayoutInflater.from(this);
        views = new ArrayList<View>();
        View view1= inflater.inflate(R.layout.weather_page1, null);
        View view2= inflater.inflate(R.layout.weather_page2, null);
        linearLayout1 = (LinearLayout) view1.findViewById(R.id.linear_layout1);
        linearLayout2 = (LinearLayout) view2.findViewById(R.id.linear_layout2);
        views.add(view1);
        views.add(view2);
        vpAdapter = new ViewPagerAdapter(views,this);
        vp =(ViewPager)findViewById(R.id.viewpager);
        vp.setAdapter(vpAdapter);
        vp.addOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for(int i =0;i<ids.length;i++)
        {
            if(i==position)
            {
                dots[i].setImageResource(R.drawable.page_indicator_focused);
            }
            else
                dots[i].setImageResource(R.drawable.page_indicator_unfocused);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private MyService mBoundService;

    private ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((MyService.MyBinder) service).getService();

            // Tell the user about this for our demo.
            Toast.makeText(MainActivity.this, "connected",
                    Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
            Toast.makeText(MainActivity.this, "connected",
                    Toast.LENGTH_SHORT).show();
        }
    };
}
