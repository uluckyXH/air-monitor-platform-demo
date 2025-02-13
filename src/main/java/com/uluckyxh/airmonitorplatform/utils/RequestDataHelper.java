package com.uluckyxh.airmonitorplatform.utils;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;

import java.util.Map;

/**
 * 请求参数传递辅助类
 * 用于动态表名分表场景下的参数传递
 */
public class RequestDataHelper {

    /**
     * 请求参数存取，使用ThreadLocal确保线程安全
     */
    private static final ThreadLocal<Map<String, Object>> REQUEST_DATA = new ThreadLocal<>();

    /**
     * 设置请求参数
     *
     * @param requestData 请求参数 MAP 对象
     */
    public static void setRequestData(Map<String, Object> requestData) {
        REQUEST_DATA.set(requestData);
    }

    /**
     * 获取指定参数值
     *
     * @param param 参数名
     * @return 参数值
     */
    public static <T> T getRequestData(String param) {
        Map<String, Object> dataMap = getRequestData();
        if (CollectionUtils.isNotEmpty(dataMap)) {
            return (T) dataMap.get(param);
        }
        return null;
    }

    /**
     * 获取所有请求参数
     *
     * @return 请求参数 MAP 对象
     */
    public static Map<String, Object> getRequestData() {
        return REQUEST_DATA.get();
    }

    /**
     * 清理线程变量，防止内存泄漏
     */
    public static void remove() {
        REQUEST_DATA.remove();
    }
}