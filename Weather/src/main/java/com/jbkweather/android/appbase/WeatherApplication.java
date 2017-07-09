package com.jbkweather.android.appbase;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePal;

/**
 * Created by sunny on 2017/7/9.
 */

public class WeatherApplication extends Application{

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        LitePal.initialize(context);
    }

    public static Context getContext(){
        return context;
    }


}
