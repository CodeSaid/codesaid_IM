package com.im.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.codesaid.lib_framework.base.BaseActivity;
import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.entity.Constants;
import com.codesaid.lib_framework.utils.sp.SpUtils;
import com.im.MainActivity;
import com.im.R;

/**
 * Created By codesaid
 * On :2019-12-24
 * Package Name: com.im.ui
 * desc : 启动页面
 * <p>
 * A:启动页全屏
 * B:延迟进入主页
 * C:根据具体逻辑是进入引导页 or 主页 or 登录页
 * D:适配刘海屏
 */
public class LauncherActivity extends BaseActivity {

    private static final int SKIP_MAIN = 1000;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case SKIP_MAIN:
                    goToMain();
                    break;
                default:
                    break;
            }
            return false;
        }
    });


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher_layout);

        mHandler.sendEmptyMessageDelayed(SKIP_MAIN, 2 * 1000);
    }

    /**
     * 跳转到主页
     */
    private void goToMain() {
        boolean isFirstApp = SpUtils.getInstance().getBoolean(Constants.SP_IS_FIRST_APP, true);
        Intent intent = new Intent();
        // 判断是否是 第一次启动 APP
        if (isFirstApp) {
            // 跳转到 ----> 引导页
            intent.setClass(this, GuideActivity.class);
            SpUtils.getInstance().putBoolean(Constants.SP_IS_FIRST_APP, false);
        } else {
            // 判断是否登录过
            String token = SpUtils.getInstance().getString(Constants.SP_TOKEN, "");
            if (TextUtils.isEmpty(token)) {
                // 判断 Bmob 是否登录过
                if (BmobManager.getInstance().isLogin()) {
                    // 登录过 ----> 跳转到主页
                    intent.setClass(this, MainActivity.class);
                } else {
                    // 未登录过 ----> 跳转到登录页面
                    intent.setClass(this, LoginActivity.class);
                }

            } else {
                // 登录过 ----> 跳转到主页
                intent.setClass(this, MainActivity.class);
            }
        }

        startActivity(intent);
        finish();
    }
}
