package com.example.sisucon.homework.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class Utils {
    public static void seedMessage(String url,okhttp3.Callback callback)
    {
        OkHttpClient connection = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        connection.newCall(request).enqueue(callback);
    }
}
