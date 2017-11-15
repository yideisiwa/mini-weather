package cn.edu.pku.hongbenyun.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.hongbenyun.app.MyApplication;
import cn.edu.pku.hongbenyun.bean.City;
import cn.edu.pku.hongbenyun.util.ChineseToPinyin;
import cn.edu.pku.hongbenyun.util.MyAdapter;

/**
 * Created by Mike_Hong on 2017/10/18.
 * 选择城市界面
 */

public class SelectCity extends Activity implements View.OnClickListener
{
    private EditText city_name_Et;
    private ImageView mBackBtn;
    private TextView city_name_Tv;
    private ListView city_list_Lv;
    private MyAdapter myAdapter;
    private List<City> mCityList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.select_city);

        //接收来自主界面的intent信息
        Intent i =this.getIntent();
        String cityName=i.getStringExtra("cityName");

        city_name_Et = (EditText)findViewById(R.id.city_name);
        city_name_Et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<City> mSearchCityList = new ArrayList<City>();
                String search_city_name = city_name_Et.getText().toString();
                for (City city : mCityList) {
                    String cityName = city.getCity();
                    if(cityName.contains(search_city_name) || ChineseToPinyin.toPinyin(cityName).contains(ChineseToPinyin.toPinyin(search_city_name)))
                        mSearchCityList.add(city);
                }
                myAdapter.updateCityList(mSearchCityList);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mBackBtn = (ImageView)findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);
        city_name_Tv = (TextView)findViewById(R.id.title_name);
        city_name_Tv.setText("当前城市："+cityName);
        city_list_Lv = (ListView)findViewById(R.id.city_list);

        //设置自定义ListView Adapter实现对ListView自定义功能
        mCityList = MyApplication.getInstance().getCityList();
        myAdapter = new MyAdapter(mCityList,this);
        city_list_Lv.setAdapter(myAdapter);
        city_list_Lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(SelectCity.this,myAdapter.getItem(position).getNumber() , Toast.LENGTH_LONG).show();
                //当选择了某一个城市时，返回给主界面城市的code
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
            /*
            case R.id.search:
                List<City> mSearchCityList = new ArrayList<City>();
                String search_city_name = city_name_Et.getText().toString();
                for (City city : mCityList) {
                    String cityName = city.getCity();
                    if(cityName.contains(search_city_name))
                        mSearchCityList.add(city);
                }
                myAdapter.updateCityList(mSearchCityList);
                break;*/
            case R.id.title_back:
                onBackPressed();
                break;
            default:
                break;
        }

    }
}
