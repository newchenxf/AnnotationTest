package com.chenxf.router;

import android.app.Activity;

import java.util.Map;

/**
 * Author:yuanzeyao<br/>
 * Date:16/11/17 下午4:31
 * Email:yuanzeyao@qiyi.com
 */
public interface IRouterTableInitializer {
    void initRouterTable(Map<String, Class<? extends Activity>> router);

    void initMappingTable(Map<String, String> mapping);
}