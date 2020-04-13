package cn.android.sample;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;


import cn.android.security.APISecurity;

public class MainActivity extends AppCompatActivity {

    TextView tv;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.sample_text);

        if(APISecurity.init(this)){
            tv.setText("初始化ok");
        }

        findViewById(R.id.btnTest).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                //API签名字符串
                String val = "POST https://www.xxx.com/login?id=1&pwd=xxx......";
                //计算签名
                String aptStr="123456";
                tv.setText("Sign:" + APISecurity.sign(aptStr));
            }
        });

    }

}