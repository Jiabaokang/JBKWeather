package com.jbkweather.android.appbase;

import android.app.Application;

import org.litepal.LitePal;

/**
 * Created by sunny on 2017/7/9.
 */

public class WeatherApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
    }
}
