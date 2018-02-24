package com.jbkweather.android;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.jbkweather.android.appbase.BaseActivity;

import java.util.List;


public class MainActivity extends BaseActivity implements BaseActivity.IRequestPermissionsListener{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

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
