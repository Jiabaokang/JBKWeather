package com.jbkweather.android.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by sunny on 2017/7/9.
 * 网络请求工具类
 */

public class HttpUtil {
    /**
     * 发送网络请求
     * @param address 请求地址
     * @param callback 结果回掉
     */
    public static void sendOkHttpRequest(String address ,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
