package com.jbkweather.android.gson;

/**
 * Created by sunny on 2017/7/15.
 *
 */

public class AQI {
    public AQICity city;

    public class AQICity {
        public String aqi;
        public String pm25;
    }
}
