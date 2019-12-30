package com.im.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.codesaid.lib_framework.base.BaseUIActivity;
import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.bmob.IMUser;
import com.codesaid.lib_framework.entity.Constants;
import com.codesaid.lib_framework.utils.sp.SpUtils;
import com.codesaid.lib_framework.utils.toast.ToastUtils;
import com.codesaid.lib_framework.view.DialogManager;
import com.codesaid.lib_framework.view.DialogView;
import com.codesaid.lib_framework.view.LoadingView;
import com.codesaid.lib_framework.view.TouchPictureView;
import com.im.MainActivity;
import com.im.R;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.QueryListener;

/**
 * Created By codesaid
 * On :2019-12-24
 * Package Name: com.im.ui
 * desc : 登录页面
 */
public class LoginActivity extends BaseUIActivity implements View.OnClickListener {

    /**
     * 1.点击发送的按钮，弹出一个提示框，图片验证码，验证通过之后
     * 2.!发送验证码，@同时按钮变成不可点击，@按钮开始倒计时，倒计时结束，@按钮可点击，@文字变成“发送”
     * 3.通过手机号码和验证码进行登录
     * 4.登录成功之后获取本地对象
     */

    private EditText et_phone;
    private EditText et_code;
    private Button btn_send_code;
    private Button btn_login;

    private static final int H_TIME = 1001;

    // 60s 倒计时
    private static int TIME = 60;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @SuppressLint("SetTextI18n")
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case H_TIME:
                    TIME--;
                    btn_send_code.setText(TIME + "s");
                    if (TIME > 0) {
                        mHandler.sendEmptyMessageDelayed(H_TIME, 1000);
                    } else {
                        btn_send_code.setEnabled(true);
                        btn_send_code.setText("发送");
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    });
    private DialogView mDialogView;
    private TouchPictureView mPictureView;

    private LoadingView mLoadingView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_layout);

        initView();
    }

    private void initView() {

        initDialogView();

        et_phone = findViewById(R.id.et_phone);
        et_code = findViewById(R.id.et_code);
        btn_send_code = findViewById(R.id.btn_send_code);
        btn_login = findViewById(R.id.btn_login);

        btn_send_code.setOnClickListener(this);
        btn_login.setOnClickListener(this);

        // 判断用户是否已经登录过
        // 如果登录过，就获取用户之前登录的手机号并且回显到页面上
        String phone = SpUtils.getInstance().getString(Constants.SP_PHONE, "");
        if (!TextUtils.isEmpty(phone)) {
            et_phone.setText(phone);
        }
    }

    private void initDialogView() {
        mLoadingView = new LoadingView(this);

        mDialogView = DialogManager
                .getInstance()
                .initView(this, R.layout.dialog_code_view);

        mPictureView = mDialogView.findViewById(R.id.picture_view);

        mPictureView.setOnViewResultListener(new TouchPictureView.onViewResultListener() {
            @Override
            public void onResult() {
                DialogManager.getInstance().hide(mDialogView);
                sendSMS();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send_code:
                DialogManager
                        .getInstance()
                        .show(mDialogView);
                break;
            case R.id.btn_login:
                login();
                break;
        }
    }

    private void login() {
        // 获取手机号
        final String phone = et_phone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            ToastUtils.show(this, "手机号不能为空");
            return;
        }

        // 获取验证码
        String code = et_code.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            ToastUtils.show(this, "验证码不能为空");
            return;
        }

        // 显示Loading view
        mLoadingView.show("正在登录...");

        BmobManager.getInstance().signOrLoginByMobilePhone(phone, code, new LogInListener<IMUser>() {
            @Override
            public void done(IMUser imUser, BmobException e) {
                if (e == null) {
                    // 登录成功
                    mLoadingView.hide();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    // 把手机号码保存起来
                    SpUtils.getInstance().putString(Constants.SP_PHONE, phone);
                    finish();
                } else {
                    // 登录失败
                    mLoadingView.hide();
                    if (e.getErrorCode() == 207) {
                        ToastUtils.show(LoginActivity.this, getString(R.string.text_login_code_error));
                    } else {
                        ToastUtils.show(LoginActivity.this, "ERROR: " + e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * 发送短信验证码
     */
    private void sendSMS() {
        // 获取手机号
        String phone = et_phone.getText().toString().trim();
        if (!TextUtils.isEmpty(phone)) {
            BmobManager.getInstance().requestSMS(phone, new QueryListener<Integer>() {
                @Override
                public void done(Integer integer, BmobException e) {
                    if (e == null) {
                        btn_send_code.setEnabled(false);
                        mHandler.sendEmptyMessage(H_TIME);
                        ToastUtils.show(LoginActivity.this, "发送成功");
                    } else {
                        Log.e("TAG", e.toString());
                        ToastUtils.show(LoginActivity.this, "发送失败");
                    }
                }
            });
        } else {
            ToastUtils.show(this, "手机号不能为空");
        }
    }
}
