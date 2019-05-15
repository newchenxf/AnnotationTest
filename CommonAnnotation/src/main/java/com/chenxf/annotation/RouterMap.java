package com.chenxf.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 路由定义的基本格式：
 * scheme://host/:i{key1}/path1/:f{key2}
 * 整个路由，对应一个activity
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface RouterMap {
    /**
     * 页面对应的scheme
     */
    String value();

    /**
     * 页面对应的注册制id列表，格式为{biz_id}_{biz_sub_id}，例如："100_109"
     * 可以做到数字，来制定某个activity，这样android & ios可以用数字来代表某个页面
     * 不过本例子，不使用该字段，不然套讨论的多了
     */
    String[] registry() default {};
}