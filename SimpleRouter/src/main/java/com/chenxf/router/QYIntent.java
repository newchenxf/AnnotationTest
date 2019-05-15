package com.chenxf.router;

import android.os.Bundle;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Author:yuanzeyao<br/>
 * Date:16/11/17 下午5:29
 * Email:yuanzeyao@qiyi.com
 */
public class QYIntent extends BaseIntent {

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_EXTEND = 1;

    private Bundle mExtras;
    private int mRequestCode = 0;
    private int mFlags = 0;
    private int mType;

    public QYIntent(String url) {
        super(url);
        mExtras = new Bundle();
        mType = TYPE_NORMAL;
    }

    public Bundle getExtras() {
        return mExtras;
    }

    public void setExtras(Bundle extras) {
        this.mExtras = extras;
    }

    public QYIntent addExtras(Bundle extras) {
        mExtras.putAll(extras);
        return this;
    }

    public int getRequestCode() {
        return mRequestCode;
    }

    public void setRequestCode(int requestCode) {
        this.mRequestCode = requestCode;
    }

    public int getFlags() {
        return mFlags;
    }

    public QYIntent withParams(String key, Serializable value) {
        mExtras.putSerializable(key, value);
        return this;
    }

    public QYIntent withParams(String key, Parcelable value) {
        mExtras.putParcelable(key, value);
        return this;
    }

    public QYIntent withParams(String key, int value) {
        mExtras.putInt(key, value);
        return this;
    }

    public QYIntent withParams(String key, double value) {
        mExtras.putDouble(key, value);
        return this;
    }

    public QYIntent withParams(String key, float value) {
        mExtras.putFloat(key, value);
        return this;
    }

    public QYIntent withParams(String key, char value) {
        mExtras.putChar(key, value);
        return this;
    }

    public QYIntent withParams(String key, CharSequence value) {
        mExtras.putCharSequence(key, value);
        return this;
    }

    public QYIntent withParams(String key, String value) {
        mExtras.putString(key, value);
        return this;
    }

    public QYIntent withParams(String key, long value) {
        mExtras.putLong(key, value);
        return this;
    }

    public QYIntent withFlags(int flags) {
        this.mFlags = flags;
        return this;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }
}
