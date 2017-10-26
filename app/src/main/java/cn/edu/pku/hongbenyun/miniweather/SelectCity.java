package cn.edu.pku.hongbenyun.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cn.edu.pku.hongbenyun.app.MyApplication;
import cn.edu.pku.hongbenyun.bean.City;
import cn.edu.pku.hongbenyun.util.MyAdapter;

/**
 * Created by Mike_Hong on 2017/10/18.
 */

public class SelectCity extends Activity implements View.OnClickListener
{
    private ImageView mBackBtn;
    private TextView city_name_Tv;
    private ListView city_list_Lv;
    private MyAdapter myAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.select_city);

        Intent i =this.getIntent();
        String cityName=i.getStringExtra("cityName");

        mBackBtn = (ImageView)findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);
        city_name_Tv = (TextView)findViewById(R.id.title_name);
        city_name_Tv.setText("当前城市："+cityName);
        city_list_Lv = (ListView)findViewById(R.id.city_list);

        myAdapter = new MyAdapter(MyApplication.getInstance().getCityList(),this);
        city_list_Lv.setAdapter(myAdapter);
        city_list_Lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(SelectCity.this,myAdapter.getItem(position).getNumber() , Toast.LENGTH_LONG).show();
                Intent i = new Intent();
                i.putExtra("cityCode", myAdapter.getItem(position).getNumber());
                setResult(RESULT_OK, i);
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.title_back:
                Intent i = new Intent();
                i.putExtra("cityCode", "101160101");
                setResult(RESULT_OK, i);
                finish();
                break;
            default:
                break;
        }

    }
}
