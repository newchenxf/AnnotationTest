package com.chenxf.butterknifetest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.chenxf.simplebutterknife.BindView;
import com.chenxf.simplebutterknife.ButterKnife;
import com.example.annotationtest.R;

public class ButterKnifeActivity extends AppCompatActivity {
    @BindView(R.id.text_view)
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        textView.setText("CHENXF");
    }
}
