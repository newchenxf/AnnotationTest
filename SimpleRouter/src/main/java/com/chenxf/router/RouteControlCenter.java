package com.chenxf.router;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Shen YunLong on 2018/08/13.
 */
public class RouteControlCenter {

    private static final String TAG = "ActivityRouter_RouteCenter";
    /**
     * 本地注册制ID与scheme映射关系
     */
    private Map<String, String> mLocalRegistryMap = new ConcurrentHashMap<>();
    /**
     * 路由表
     */
    private Map<String, Class<? extends Activity>> mLocalSchemeMap = new ConcurrentHashMap<>();


    private LruCache<String, Class<?>> mLruCache = new LruCache<>(20);

    private static volatile RouteControlCenter sInstance;

    private RouteControlCenter() {
    }

    public static RouteControlCenter getInstance() {
        if (null == sInstance) {
            synchronized (RouteControlCenter.class) {
                if (null == sInstance) {
                    sInstance = new RouteControlCenter();
                }
            }
        }
        return sInstance;
    }

    public Map<String, String> getMappingTable() {
        return mLocalRegistryMap;
    }

    public Map<String, Class<? extends Activity>> getRouteTable() {
        return mLocalSchemeMap;
    }




    /**
     * 将QYIntent转换成系统Intent
     */
    public Intent queryIntent(Context context, @NonNull QYIntent qyIntent) {
        if (QYIntent.TYPE_EXTEND == qyIntent.getType()) {
            return null;
        }

        String routeUrl = findMatchedRoute(qyIntent);
        if (!TextUtils.isEmpty(routeUrl)) {
            Class<? extends Activity> matchedActivity = mLocalSchemeMap.get(routeUrl);
            Intent intent = new Intent(context, matchedActivity);
//            intent = IntentUtils.setKeyValueInPath(routeUrl, qyIntent.getUrl(), intent);
//            intent = IntentUtils.setOptionParams(qyIntent.getUrl(), intent);
            intent = intent.putExtras(qyIntent.getExtras());
            return intent;
        }
        return null;
    }


    /**
     * 在路由表中查找指定记录
     */
    private String findMatchedRoute(QYIntent qyIntent) {
        for (String routeUrl : mLocalSchemeMap.keySet()) {
            if(routeUrl.equals(qyIntent.getUrl())) {
                return routeUrl;
            }
        }
        Log.d(TAG, "findMatchedRoute failed !");
        return null;
    }



}
