package com.dsz.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.dsz.threads.MsgThreads;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test);
        MsgThreads msgThreads = new MsgThreads(this);
        msgThreads.start();
    }
}
