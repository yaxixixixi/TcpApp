package com.yaxi.tcphttp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void goTcp(View view){
        Intent it = new Intent(MainActivity.this,TcpActivity.class);
        startActivity(it);
    }
    public void goHttp(View view){
        Intent it = new Intent(MainActivity.this,HttpActivity.class);
        startActivity(it);
    }
}
