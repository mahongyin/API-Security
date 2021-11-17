package cn.android.sample;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
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

        if(APISecurity.init(getApplicationContext())){
            tv.setText("初始化ok");
        }else {
            tv.setText("初始化fail");
        }

        findViewById(R.id.btnTest).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                //API签名字符串
                String aptStr="123456";
                tv.setText("Sign加盐:" + APISecurity.sign(aptStr));
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        APISecurity.detectedDynamicDebug();
    }
}
