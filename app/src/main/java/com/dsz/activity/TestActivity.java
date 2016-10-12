package com.dsz.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.dsz.threads.MyThreads;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test);
        MyThreads myThreads = new MyThreads(this);
        myThreads.start();
    }
}
