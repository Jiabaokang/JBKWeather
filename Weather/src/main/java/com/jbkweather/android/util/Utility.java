package com.jbkweather.android.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.jbkweather.android.database.City;
import com.jbkweather.android.database.County;
import com.jbkweather.android.database.Province;
import com.jbkweather.android.gson.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sunny on 2017/7/9.
 * 解析省市县的数据
 */

public class Utility {
    /**
     * 解析处理服务器返回的省级数据
     * @param response
     * @return
     */
    public static boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvince = new JSONArray(response);
                for (int i = 0; i < allProvince.length(); i++) {
                    JSONObject provinceJSONObject = allProvince.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceJSONObject.getString("name"));
                    province.setProvinceCode(provinceJSONObject.getInt("id"));

                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析处理服务器返回的市级数据
     * @param response
     * @return
     */
    public static boolean handleCityResponse(String response,int provinceId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allityCitys = new JSONArray(response);
                for (int i = 0; i < allityCitys.length(); i++) {
                    JSONObject cityJSONObject = allityCitys.getJSONObject(i);
                    City city = new City();
                    city.setCityCode(cityJSONObject.getInt("id"));
                    city.setCityName(cityJSONObject.getString("name"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析处理服务器返回的县级数据
     * @param response
     * @return
     */
    public static boolean handleCountyResponse(String response,int cityId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCountys = new JSONArray(response);
                for (int i = 0; i < allCountys.length(); i++) {
                    JSONObject countyJSONObject = allCountys.getJSONObject(i);
                    County county = new County();
                    county.setWeathreId(countyJSONObject.getString("weather_id"));
                    county.setCountyName(countyJSONObject.getString("name"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析Weather实体类
     * @param response
     * @return
     */
    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
