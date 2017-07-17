package com.jbkweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by sunny on 2017/7/15.
 * 当前天气
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {
        @SerializedName("txt")
        public String info;
    }


}
