package com.codesaid.lib_framework.helper;

import android.app.Activity;

import java.util.HashSet;

/**
 * Created By codesaid
 * On :2020-02-29 21:06
 * Package Name: com.codesaid.lib_framework.helper
 * desc: Activity 帮助类
 */
public class ActivityHelper {

    private static ActivityHelper instance = new ActivityHelper();

    private static HashSet<Activity> hashSet = new HashSet<>();

    private ActivityHelper() {

    }

    public static ActivityHelper getInstance() {
        return instance;
    }

    /**
     * 填充
     *
     * @param activity activity
     */
    public void addActivity(Activity activity) {
        try {
            hashSet.add(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 退出
     */
    public void exit() {
        try {
            for (Activity activity : hashSet) {
                if (activity != null)
                    activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
