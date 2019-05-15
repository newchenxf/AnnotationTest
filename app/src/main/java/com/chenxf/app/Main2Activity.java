package com.chenxf.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.chenxf.router.ActivityRouter;
import com.chenxf.router.QYIntent;
import com.chenxf.simplebutterknife.BindView;
import com.chenxf.simplebutterknife.ButterKnife;
import com.example.annotationtest.R;

public class Main2Activity extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.router_btn)
    Button mRouterBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ButterKnife.bind(this);
        mRouterBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.router_btn) {
            QYIntent intent = new QYIntent("chenxf://router/test");
            ActivityRouter.getInstance().start(this, intent);
        }
    }

}
