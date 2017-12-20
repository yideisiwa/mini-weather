package cn.edu.pku.hongbenyun.miniweather;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of App Widget functionality.
 */
public class NewAppWidget extends AppWidgetProvider {

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

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        String type= "晴";
        String temperature = "-2℃~7℃";

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
        views.setTextViewText(R.id.temperature, temperature);
        if(type!=null)
            views.setImageViewResource(R.id.weather_img,weatherMap.get(type));


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String type=intent.getStringExtra("type");
        String temperature = intent.getStringExtra("temperature");

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
        views.setTextViewText(R.id.temperature, temperature);
        if(type!=null)
           views.setImageViewResource(R.id.weather_img,weatherMap.get(type));


        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        // 相当于获得所有本程序创建的appwidget
        ComponentName thisName = new ComponentName(context,NewAppWidget.class);
        //更新widget
        appWidgetManager.updateAppWidget(thisName,views);

    }
}

