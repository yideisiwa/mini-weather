package cn.edu.pku.hongbenyun.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mike_Hong on 2017/11/29.
 */

public class Guide extends Activity implements ViewPager.OnPageChangeListener{
    private  ViewPagerAdapter vpAdapter;
    private ViewPager vp;
    private List<View> views;

    private ImageView[] dots;
    private int[] ids = {R.id.iv1,R.id.iv2,R.id.iv3};

    private Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide);

        SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        String introductionPage = sharedPreferences.getString("introductionPage","0");
        if(introductionPage.equals("1"))
        {
            Intent i = new Intent(Guide.this,MainActivity.class);
            startActivity(i);
            finish();
        }
        else
        {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("introductionPage", "1");
            editor.commit();
        }
        initViews();
        initDots();

        btn=(Button)views.get(2).findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Guide.this,MainActivity.class);
                startActivity(i);
                finish();
            }
        });
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
        views.add(inflater.inflate(R.layout.page1,null));
        views.add(inflater.inflate(R.layout.page2,null));
        views.add(inflater.inflate(R.layout.page3,null));
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
}
