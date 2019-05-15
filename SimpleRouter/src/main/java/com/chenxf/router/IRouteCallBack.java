package com.chenxf.router;

import android.content.Context;

public interface IRouteCallBack {

    void notFound(Context context, String url);

    void beforeOpen(Context context, String url);

    void afterOpen(Context context, String url);

    void error(Context context, String url, Throwable e);
}