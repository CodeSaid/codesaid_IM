package com.codesaid.lib_framework;

import android.content.Context;

import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.cloud.CloudManager;
import com.codesaid.lib_framework.map.MapManager;
import com.codesaid.lib_framework.utils.sp.SpUtils;
import com.codesaid.lib_framework.voice.VoiceManager;

import org.litepal.LitePal;

/**
 * Created By codesaid
 * On :2019-12-21
 * Package Name: com.codesaid.lib_framework
 * desc : Framework 入口
 */
public class Framework {
    private volatile static Framework mInstance;

    private Framework() {

    }

    public static Framework getInstance() {
        if (mInstance == null) {
            synchronized (Framework.class) {
                if (mInstance == null) {
                    mInstance = new Framework();
                }
            }
        }
        return mInstance;
    }

    public void initFramework(Context context) {
        SpUtils.getInstance().initSp(context);
        BmobManager.getInstance().init(context);
        CloudManager.getInstance().initCloud(context);
        LitePal.initialize(context);
        MapManager.getInstance().initMap(context);
        //VoiceManager.getInstance().initVoice(context);
    }
}
