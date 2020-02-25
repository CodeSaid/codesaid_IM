package com.codesaid.lib_framework.base;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.codesaid.lib_framework.event.EventManager;
import com.codesaid.lib_framework.event.MessageEvent;
import com.codesaid.lib_framework.utils.LanguaueUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created By codesaid
 * On :2019-12-25
 * Package Name: com.codesaid.lib_framework.base
 * desc : Activity 基类
 */
public class BaseActivity extends AppCompatActivity {

    //申请运行时权限的Code
    private static final int PERMISSION_REQUEST_CODE = 1000;
    //申请窗口权限的Code
    public static final int PERMISSION_WINDOW_REQUEST_CODE = 1001;

    //申明所需权限
    private String[] mStrPermission = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.VIBRATE
    };

    //保存没有同意的权限
    private List<String> mPerList = new ArrayList<>();
    //保存没有同意的失败权限
    private List<String> mPerNoList = new ArrayList<>();

    private OnPermissionsResult permissionsResult;

    /**
     * 一个方法请求权限
     *
     * @param permissionsResult
     */
    protected void request(OnPermissionsResult permissionsResult) {
        if (!checkPermissionsAll()) {
            requestPermissionAll(permissionsResult);
        }
    }

    /**
     * 判断单个权限
     *
     * @param permissions
     * @return
     */
    protected boolean checkPermissions(String permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int check = checkSelfPermission(permissions);
            return check == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    /**
     * 判断是否需要申请权限
     *
     * @return
     */
    protected boolean checkPermissionsAll() {
        mPerList.clear();
        for (int i = 0; i < mStrPermission.length; i++) {
            boolean check = checkPermissions(mStrPermission[i]);
            //如果不同意则请求
            if (!check) {
                mPerList.add(mStrPermission[i]);
            }
        }
        return mPerList.size() > 0 ? false : true;
    }

    /**
     * 请求权限
     *
     * @param mPermissions
     */
    protected void requestPermission(String[] mPermissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(mPermissions, PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * 申请所有权限
     *
     * @param permissionsResult
     */
    protected void requestPermissionAll(OnPermissionsResult permissionsResult) {
        this.permissionsResult = permissionsResult;
        requestPermission(mPerList.toArray(new String[mPerList.size()]));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        mPerNoList.clear();
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        //你有失败的权限
                        mPerNoList.add(permissions[i]);
                    }
                }
                if (permissionsResult != null) {
                    if (mPerNoList.size() == 0) {
                        permissionsResult.OnSuccess();
                    } else {
                        permissionsResult.OnFail(mPerNoList);
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected interface OnPermissionsResult {
        void OnSuccess();

        void OnFail(List<String> noPermissions);
    }

    /**
     * 判断应用是否有窗口权限
     */
    protected boolean checkWindowPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    /**
     * 请求 窗口 权限
     */
    protected void requestWindowPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                , Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, PERMISSION_WINDOW_REQUEST_CODE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getType()) {
            case EventManager.EVENT_RUPDATE_LANGUAUE:
                LanguaueUtils.updateLanguaue(this);
                recreate();
                break;
        }
    }
}
