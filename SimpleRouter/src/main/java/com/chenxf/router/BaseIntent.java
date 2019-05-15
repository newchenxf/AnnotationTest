package com.chenxf.router;


public abstract class BaseIntent{
    String mUrl;

    public BaseIntent(String url) {
        this.mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }

}
