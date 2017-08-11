package com.jbkweather.android;

import android.Manifest;
import android.os.Bundle;
import android.widget.Toast;

import com.jbkweather.android.appbase.BaseActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

import java.util.List;

@EActivity(R.layout.activity_main)
public class MainActivity extends BaseActivity implements BaseActivity.IRequestPermissionsListener{

    @AfterViews
    void initView(){
        super.requestMustPermission(this, new String[]{Manifest.permission.CALL_PHONE,Manifest.permission.WRITE_EXTERNAL_STORAGE});
    }


    @Override
    public void onPermissionSuccess() {
        Toast.makeText(this, "权限申请成功了", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionFailed(List<String> failedPermissionList) {
        for (int i = 0; i < failedPermissionList.size(); i++) {
            Toast.makeText(this, failedPermissionList.get(i), Toast.LENGTH_SHORT).show();
           // requestMustPermission(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
        }
    }
}
