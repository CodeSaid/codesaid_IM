package com.codesaid.lib_framework.bmob;

import android.content.Context;

import java.io.File;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

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

    /**
     * 上传头像
     *
     * @param name     名称
     * @param file     头像
     * @param listener listener
     */
    public void uploadFirstPhoto(final String name, File file, final onUploadPhotoListener listener) {
        /**
         * 上传文件   拿到地址
         * 更新用户 信息
         */

        final IMUser user = getUser();
        final BmobFile bmobFile = new BmobFile(file);
        bmobFile.uploadblock(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    // 上传成功
                    user.setNickName(name);
                    user.setPhoto(bmobFile.getFileUrl());

                    user.setTokenNickName(name);
                    user.setTokenPhoto(bmobFile.getFileUrl());

                    // 更新用户信息
                    user.update(new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e == null) {
                                listener.onUploadSuccess();
                            } else {
                                listener.onUploadFail(e);
                            }
                        }
                    });
                } else {
                    listener.onUploadFail(e);
                }
            }
        });
    }

    public interface onUploadPhotoListener {

        void onUploadSuccess();

        void onUploadFail(BmobException e);
    }
}
