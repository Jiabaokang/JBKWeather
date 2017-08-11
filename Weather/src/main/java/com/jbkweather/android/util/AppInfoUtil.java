package com.jbkweather.android.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Created by sunny on 2017/8/10.
 */

public class AppInfoUtil {

    private static final String TAG = "AppInfoUtil";



    /**
     * 获取报名信息
     *
     * @param context 上下文对象
     * @return 包名信息
     */
    public static PackageInfo getPackageInfo(Context context) throws Exception {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        return packageInfo;
    }

    /**
     * 获取当前应用的版本名称
     *
     * @param context 上下文
     * @return 版本名称
     */
    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            versionName = getPackageInfo(context).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return versionName;
    }

    /**
     * 获取当前应用的版本号
     *
     * @param context 上下文
     * @return 当前应用的版本号
     */
    public static String getAppVersionCode(Context context) {
        int  versionCode = 0;
        try {
            versionCode = getPackageInfo(context).versionCode;
        } catch (Exception e) {
            LogUtil.e(TAG, "packageName is null,please checked");
            e.printStackTrace();
        }
        return String.valueOf(versionCode);
    }


}
