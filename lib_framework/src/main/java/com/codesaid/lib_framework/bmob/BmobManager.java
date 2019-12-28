package com.codesaid.lib_framework.bmob;

import android.content.Context;

import cn.bmob.v3.Bmob;

/**
 * Created By codesaid
 * On :2019-12-29
 * Package Name: com.codesaid.lib_framework.bmob
 * desc : Bmob Manager
 */
public class BmobManager {

    public static final String APP_KEY = "3191c930892f107b46d1a0feb283ad36";

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
}
