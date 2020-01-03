package com.codesaid.lib_framework.utils.toast;

import android.content.Context;

import com.maning.mndialoglibrary.MToast;
import com.maning.mndialoglibrary.config.MToastConfig;

/**
 * Created By codesaid
 * On :2019-12-29
 * Package Name: com.codesaid.lib_framework.utils
 * desc ： Toast 工具类
 */
public class ToastUtils {


    public static void show(Context context, String desc) {
        MToast.makeTextShort(context, desc,
                new MToastConfig
                        .Builder()
                        .setGravity(MToastConfig.MToastGravity.CENTRE)
                        .setTextSize(26)
                        .build());
    }
}
