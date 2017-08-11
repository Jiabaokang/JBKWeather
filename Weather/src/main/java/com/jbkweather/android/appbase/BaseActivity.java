package com.jbkweather.android.appbase;

import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.jbkweather.android.util.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sunny on 2017/8/10.
 * BaseActivity加入权限申请功能
 */

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    private static final int PERMISSION_REQUEST_CODE = 1;

    //权限申请成功失败的回调接口
    private IRequestPermissionsListener mPermissionsListener;


    private List<String> permissionList = new ArrayList<>();

    /**
     * 申请权限的方法
     * @param requestPermissionsListener 回调监听
     * @param permissions 需要申请的权限
     */
    public void requestMustPermission(IRequestPermissionsListener requestPermissionsListener, String[] permissions) {
        this.mPermissionsListener = requestPermissionsListener;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(permissions[i]);
                }
            }
            if (permissionList.isEmpty()) {

                LogUtil.i(TAG, "Has applied for all permissions");
            } else {
                ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]),
                        PERMISSION_REQUEST_CODE);
            }
        } else {
            LogUtil.i(TAG, "Build.VERSION < 23版本");
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LogUtil.i(TAG, "onRequestPermissionsResult" + Arrays.toString(permissions));

        //清空集合后放置用户未授权的权限名称
        permissionList.clear();
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length < 0) {
                mPermissionsListener.onPermissionSuccess();
            } else {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        permissionList.add(permissions[i]);
                    }
                }
                mPermissionsListener.onPermissionFailed(permissionList);
            }
        }
    }

    //将权限申请的结果返回
    public interface IRequestPermissionsListener {
        void onPermissionSuccess();

        void onPermissionFailed(List<String> failedPermissionList);
    }
}
