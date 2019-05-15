package com.chenxf.app;

import android.app.Application;
import android.content.Context;

import com.chenxf.router.ActivityRouter;
import com.chenxf.router.RouterTableInitializerapp;

public class MyApplication extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        //初始化列表
        ActivityRouter.getInstance().init(this);
        ActivityRouter.getInstance().initActivityRouterTable(new RouterTableInitializerapp());//RouterTableInitializerapp是编译期间生成
    }
}