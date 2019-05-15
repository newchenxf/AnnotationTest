package com.chenxf.router;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Map;

public class ActivityRouter {
    private static final String TAG = "ActivityRouter";

    /**
     * 是否初始化
     */
    private boolean isInit = false;

    /**
     * 全局Context
     */
    private Context mBaseContext;

    public static final String DEFAULT_SCHEME = "chenxf";
    /**
     * 单例
     */
    private static class ActivityRouterHolder {
        private static final ActivityRouter INSTANCE = new ActivityRouter();
    }

    public static ActivityRouter getInstance() {
        return ActivityRouterHolder.INSTANCE;
    }

    private ActivityRouter() {
    }

    /**
     * 初始化路由表，此方法适用于通过注解方式生成路由表
     *
     * @param context 全局Context
     */
    public void init(Context context) {
        if (context == null) {
            return;
        }
        if (!isInit) {
            mBaseContext = context.getApplicationContext();
            isInit = true;
        }
    }



    /**
     * 设置映射表
     */
    public void addMappingTable(Map<String, String> table) {
        if (table != null) {
            RouteControlCenter.getInstance().getMappingTable().putAll(table);
        }
    }

    @Deprecated
    public void addMapingTable(Map<String, String> table) {
        addMappingTable(table);
    }

    public Map<String, String> getMappingTable() {
        return RouteControlCenter.getInstance().getMappingTable();
    }

    public Map<String, Class<? extends Activity>> getRouteTable() {
        return RouteControlCenter.getInstance().getRouteTable();
    }

    public Context getContext() {
        return mBaseContext;
    }


    /**
     * 使用指定初始化接口初始化路由表
     *
     * @param routerInitializer
     */
    public void initActivityRouterTable(IRouterTableInitializer routerInitializer) {
        if (routerInitializer != null) {
            Map<String, Class<? extends Activity>> routeTable = getRouteTable();
            routerInitializer.initRouterTable(routeTable);
        }
    }


    /**
     * 通过{@link QYIntent}启动Activity
     *
     * @param fromContext 当前Activity，如果需要从Context跳转，则传入相应的context或者置Null
     * @param qyIntent    qyIntent类似系统Intent,用来选择需要跳转的Activity
     */
    public void start(Context fromContext, QYIntent qyIntent) {
        start(fromContext, qyIntent, null, null);
    }

    /**
     * 通过{@link QYIntent}启动Activity,并设置跳转回调
     *
     * @param fromContext   当前Activity，如果需要从Context跳转，则传入相应的context或者置Null
     * @param qyIntent      qyIntent类似系统Intent,用来选择需要跳转的Activity
     * @param routeCallBack 跳转回调
     */
    public void start(Context fromContext, QYIntent qyIntent, @Nullable IRouteCallBack routeCallBack) {
        start(fromContext, qyIntent, routeCallBack, null);
    }

    public void start(Context fromContext, QYIntent qyIntent, @Nullable IRouteCallBack routeCallBack, @Nullable Bundle options) {
//        RouterLazyInitializer.init();
        if (qyIntent == null) {
            Log.e(TAG, "start failed, qyIntent is null !");
            if (routeCallBack != null) {
                routeCallBack.error(mBaseContext, "", new NullPointerException("qyIntent is null"));
            }
            return;
        }

        try {
            Activity fromActivity = null;
            if (fromContext instanceof Activity) {
                fromActivity = (Activity) fromContext;
            }
            enterActivity(fromActivity, qyIntent, routeCallBack, options);
        } catch (Exception e) {
            Log.e(TAG, "start error, exception=", e);
            if (routeCallBack != null) {
                routeCallBack.error(mBaseContext, qyIntent.getUrl(), e);
            }
        }
    }


    /**
     * 对应startActivity
     */
    private void enterActivity(Activity fromActivity, QYIntent qyIntent,
                               @Nullable IRouteCallBack routeCallBack, @Nullable Bundle options) {
        //QYIntent转换为Intent
        Intent intent = RouteControlCenter.getInstance().queryIntent(mBaseContext, qyIntent);
        if (intent == null) {
            Log.e(TAG, "Route Not Found ! " + qyIntent.getUrl());
            if (routeCallBack != null) {
                routeCallBack.notFound(mBaseContext, qyIntent.getUrl());
            }
            return;
        }
        if (routeCallBack != null) {
            routeCallBack.beforeOpen(mBaseContext, qyIntent.getUrl());
        }
        if (fromActivity == null) {
            Log.d(TAG, "startActivity  fromActivity is null !");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | qyIntent.getFlags());
            startActivity(mBaseContext, intent, options);
        } else {
            Log.d(TAG, "startActivity  fromActivity is not null !");
            intent.setFlags(qyIntent.getFlags());
            startActivity(fromActivity, intent, options);
        }
        Log.d(TAG, "startActivity success ! "+ qyIntent.getUrl());
        if (routeCallBack != null) {
            routeCallBack.afterOpen(mBaseContext, qyIntent.getUrl());
        }
    }

    private void startActivity(@NonNull Context context, Intent intent, @Nullable Bundle options) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.startActivity(intent, options);
        } else {
            context.startActivity(intent);
        }
    }

}
