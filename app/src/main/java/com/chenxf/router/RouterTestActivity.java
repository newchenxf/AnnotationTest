package com.chenxf.router;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.chenxf.annotation.RouterMap;
import com.example.annotationtest.R;

@RouterMap(value = "chenxf://router/test", registry = "10_1")
public class RouterTestActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_test);
    }

}
