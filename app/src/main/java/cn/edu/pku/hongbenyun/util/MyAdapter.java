package cn.edu.pku.hongbenyun.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import cn.edu.pku.hongbenyun.bean.City;
import cn.edu.pku.hongbenyun.miniweather.R;

/**
 * Created by Mike_Hong on 2017/10/25.
 * 自定义的选择城市界面ListView的适配器
 */

public class MyAdapter extends BaseAdapter {
    //保存城市信息的列表
    private List<City> mCityList;
    //上下文信息
    private Context context;
    public MyAdapter(List<City> mCityList,Context context) {
        this.mCityList = mCityList;
        this.context = context;
    }
    @Override
    public int getCount() {
        return mCityList==null?0:mCityList.size();
    }

    @Override
    public City getItem(int position) {
        return mCityList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view= LayoutInflater.from(context).inflate(R.layout.listview_item, null);
        City city=getItem(position);

        TextView city_name_Tv= (TextView) view.findViewById(R.id.city_name);
        city_name_Tv.setText(city.getCity());
        return view;
    }

    public void updateCityList(List<City> list)
    {
        mCityList = list;
        notifyDataSetChanged();
    }
}
