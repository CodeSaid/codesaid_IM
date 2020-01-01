package com.codesaid.lib_framework.bmob;

import android.content.Context;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.QueryListener;

/**
 * Created By codesaid
 * On :2019-12-29
 * Package Name: com.codesaid.lib_framework.bmob
 * desc : Bmob Manager
 */
public class BmobManager {

    private static final String APP_KEY = "06f10a2dabbb5e3c84121c048e69dd98";

    private volatile static BmobManager mInstance = null;

    private BmobManager() {

    }

    public static BmobManager getInstance() {
        if (mInstance == null) {
            synchronized (BmobManager.class) {
                if (mInstance == null) {
                    mInstance = new BmobManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * init Bmob
     */
    public void init(Context context) {
        Bmob.initialize(context, APP_KEY);
    }

    /**
     * 获取当前登录的 User
     *
     * @return User
     */
    public IMUser getUser() {
        return BmobUser.getCurrentUser(IMUser.class);
    }

    /**
     * 发送短信验证码
     *
     * @param phone    手机号码
     * @param listener listener
     */
    public void requestSMS(String phone, QueryListener<Integer> listener) {
        BmobSMS.requestSMSCode(phone, "", listener);
    }

    /**
     * 通过手机短信验证码注册 or 登录
     *
     * @param phone    手机号
     * @param code     验证码
     * @param listener listener
     */
    public void signOrLoginByMobilePhone(String phone, String code, LogInListener<IMUser> listener) {
        BmobUser.signOrLoginByMobilePhone(phone, code, listener);
    }

    /**
     * 判断是否登录过
     */
    public boolean isLogin() {
        return BmobUser.isLogin();
    }
}
