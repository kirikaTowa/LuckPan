package com.itfitness.luckpan;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.itfitness.luckpan.widget.LuckPan;
import com.itfitness.luckpan.widget.LuckPanAnimEndCallBack;

public class MainActivity extends AppCompatActivity {
    private LuckPan pan;
    private ImageView imgStart;
    private String[] mItemStrs = {"活动1","活动2","活动3","活动4","活动5","活动6","活动7","活动8"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pan = (LuckPan) findViewById(R.id.pan);
        imgStart = (ImageView) findViewById(R.id.img_start);
        pan.setItems(mItemStrs);
        pan.setLuckNumber(2);
        pan.setLuckPanAnimEndCallBack(new LuckPanAnimEndCallBack() {
            @Override
            public void onAnimEnd(String str) {
                Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
            }
        });
        imgStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pan.startAnim();
            }
        });
    }
}
