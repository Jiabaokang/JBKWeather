package com.jbkweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by sunny on 2017/7/13.
 *  @SerializedName() 让json字段和Java字段之间建立映射关系
 */

public class Basic {

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
