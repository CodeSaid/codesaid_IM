package com.codesaid.lib_framework.window;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

/**
 * Created By codesaid
 * On :2020-02-04
 * Package Name: com.codesaid.lib_framework.window
 * desc : window  的 辅助类
 */
public class WindowHelper {
    private static volatile WindowHelper mInstance = null;

    private Context mContext;

    private WindowManager windowManager;
    private WindowManager.LayoutParams mLayoutParams;

    private WindowHelper() {

    }

    public static WindowHelper getInstance() {
        if (mInstance == null) {
            synchronized (WindowHelper.class) {
                if (mInstance == null) {
                    mInstance = new WindowHelper();
                }
            }
        }
        return mInstance;
    }

    /**
     * init window
     *
     * @param context context
     */
    public void initWindow(Context context) {
        this.mContext = context;
        windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();

        // 设置宽高
        mLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        mLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;

        // 设置标志位
        mLayoutParams.flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;

        // 设置格式
        mLayoutParams.format = PixelFormat.TRANSLUCENT;

        // 设置位置
        mLayoutParams.gravity = Gravity.CENTER;

        // 设置类型
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
    }

    /**
     * 创建 View
     *
     * @param layoutId layoutId
     * @return View
     */
    public View getView(int layoutId) {
        return View.inflate(mContext, layoutId, null);

    }

    /**
     * 显示 View
     *
     * @param view view
     */
    public void showView(View view) {
        if (view != null) {
            if (view.getParent() == null) {
                windowManager.addView(view, mLayoutParams);
            }
        }
    }

    /**
     * 隐藏 View
     *
     * @param view view
     */
    public void hideView(View view) {
        if (view != null) {
            if (view.getParent() != null) {
                windowManager.removeView(view);
            }
        }
    }
}
