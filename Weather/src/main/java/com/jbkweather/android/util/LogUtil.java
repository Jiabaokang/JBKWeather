package com.jbkweather.android.util;

import android.util.Log;

/**
 * Created by sunny on 2017/7/9.
 * 日志工具类
 */

public class LogUtil {

    private static final int VERBOSE = 1;

    private static final int DEBUG = 2;

    private static final int INFO = 3;

    private static final int WARN = 4;

    private static final int ERROR = 5;

    //项目发布的时候取消到所有日志  public static int level = NOTHING;
    public static final int NOTHING = 6;

    public static int level = VERBOSE;

    public static void v(String tag,String mag){
        if(level <= VERBOSE){
            Log.v(tag,mag);
        }
    }

    public static void d(String tag,String mag){
        if(level <= DEBUG){
            Log.v(tag,mag);
        }
    }

    public static void i(String tag,String mag){
        if(level <= INFO){
            Log.v(tag,mag);
        }
    }

    public static void w(String tag,String mag){
        if(level <= WARN){
            Log.v(tag,mag);
        }
    }

    public static void e(String tag,String mag){
        if(level <= ERROR){
            Log.v(tag,mag);
        }
    }

}
