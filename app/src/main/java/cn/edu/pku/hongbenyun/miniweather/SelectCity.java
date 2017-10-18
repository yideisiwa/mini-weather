package cn.edu.pku.hongbenyun.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Mike_Hong on 2017/10/18.
 */

public class SelectCity extends Activity implements View.OnClickListener
{
    private ImageView mBackBtn;
    private TextView city_name_Tv;

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
