package com.jbkweather.android.database;

import org.litepal.crud.DataSupport;

/**
 * Created by sunny on 2017/7/9.
 * 市县实体类
 */

public class County extends DataSupport {

    //ID是实体类中都应该有的字段看，映射到数据库表的时候需要，应该是继承了DataSupport这库的原因
    private int id;

    //县或者市的名称
    private String countyName;

    //天气ID
    private String weathreId;

    //城市ID
    private int cityId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String getWeathreId() {
        return weathreId;
    }

    public void setWeathreId(String weathreId) {
        this.weathreId = weathreId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
