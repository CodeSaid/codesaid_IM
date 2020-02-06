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
    private WindowManager.LayoutParams lp;

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
        lp = createLayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
    }

    /**
     * 创建 WindowManager.LayoutParams
     *
     * @param width   宽度
     * @param height  高度
     * @param gravity 位置
     */
    public WindowManager.LayoutParams createLayoutParams(int width, int height, int gravity) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        // 设置宽高
        layoutParams.width = width;
        layoutParams.height = height;

        // 设置标志位
        layoutParams.flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;

        // 设置格式
        layoutParams.format = PixelFormat.TRANSLUCENT;

        // 设置位置
        layoutParams.gravity = gravity;

        // 设置类型
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        return layoutParams;
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
                windowManager.addView(view, lp);
            }
        }
    }

    /**
     * 自定义 WindowManager.LayoutParams
     * 显示 View
     *
     * @param view view
     */
    public void showView(View view, WindowManager.LayoutParams layoutParams) {
        if (view != null) {
            if (view.getParent() == null) {
                windowManager.addView(view, layoutParams);
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

    /**
     * 更新 View
     *
     * @param view         view
     * @param layoutParams layoutParams
     */
    public void updateView(View view, WindowManager.LayoutParams layoutParams) {
        if (view != null && layoutParams != null) {
            windowManager.updateViewLayout(view, layoutParams);
        }
    }
}
