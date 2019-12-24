package com.codesaid.lib_framework.utils.sp;

import android.content.Context;
import android.content.SharedPreferences;

import com.codesaid.lib_framework.BuildConfig;

/**
 * Created By codesaid
 * On :2019-12-24
 * Package Name: com.codesaid.lib_framework.utils.sp
 * desc : SharedPreferences 工具类
 */
public class SpUtils {
    private SharedPreferences sp;
    private SharedPreferences.Editor mEditor;

    /**
     * key - values 存储方式
     * 它的存储路径：data/data/packageName/shared_prefs/sp_name.xml
     * <p>
     * File存储：/sdcard/ 读写方式不一样
     * 数据库：LitePal
     * get/post:数据的读写
     */

    private volatile static SpUtils mInstance = null;

    private SpUtils() {

    }

    public static SpUtils getInstance() {
        if (mInstance == null) {
            synchronized (SpUtils.class) {
                if (mInstance == null) {
                    mInstance = new SpUtils();
                }
            }
        }
        return mInstance;
    }

    public void initSp(Context context) {
        /**
         * MODE_PRIVATE：只限于本应用读写
         * MODE_WORLD_READABLE:支持其他应用读，但是不能写
         * MODE_WORLD_WRITEABLE:其他应用可以写
         */
        sp = context.getSharedPreferences(BuildConfig.SP_NAME, Context.MODE_PRIVATE);
        mEditor = sp.edit();
    }

    public void putInt(String key, int value) {
        mEditor.putInt(key, value);
        mEditor.commit();
    }

    public int getInt(String key, int defaultValue) {
        return sp.getInt(key, defaultValue);
    }

    public void putString(String key, String values) {
        mEditor.putString(key, values);
        mEditor.commit();
    }

    public String getString(String key, String defValues) {
        return sp.getString(key, defValues);
    }

    public void putBoolean(String key, boolean values) {
        mEditor.putBoolean(key, values);
        mEditor.commit();
    }

    public boolean getBoolean(String key, boolean defValues) {
        return sp.getBoolean(key, defValues);
    }

    public void deleteKey(String key) {
        mEditor.remove(key);
        mEditor.commit();
    }
}
