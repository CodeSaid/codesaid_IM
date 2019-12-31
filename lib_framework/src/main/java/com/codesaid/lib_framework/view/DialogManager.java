package com.codesaid.lib_framework.view;

import android.content.Context;
import android.view.Gravity;

import com.codesaid.lib_framework.R;

/**
 * Created By codesaid
 * On :2019-12-30
 * Package Name: com.codesaid.lib_framework.view
 */
public class DialogManager {

    private static volatile DialogManager mInstance = null;

    private DialogManager() {

    }

    public static DialogManager getInstance() {
        if (mInstance == null) {
            synchronized (DialogManager.class) {
                if (mInstance == null) {
                    mInstance = new DialogManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化 Dialog
     *
     * @param context context
     * @param layout  layout id
     * @return DialogView
     */
    public DialogView initView(Context context, int layout) {
        return new DialogView(context, layout, R.style.Theme_Dialog, Gravity.CENTER);
    }

    /**
     * 初始化 Dialog
     *
     * @param context context
     * @param layout  layout id
     * @param gravity 位置
     * @return DialogView
     */
    public DialogView initView(Context context, int layout, int gravity) {
        return new DialogView(context, layout, R.style.Theme_Dialog, gravity);
    }

    /**
     * 显示 Dialog
     *
     * @param view dialog
     */
    public void show(DialogView view) {
        if (view != null) {
            if (!view.isShowing()) {
                view.show();
            }
        }
    }

    /**
     * 隐藏L Dialog
     *
     * @param view dialog
     */
    public void hide(DialogView view) {
        if (view != null) {
            if (view.isShowing()) {
                view.hide();
            }
        }
    }
}
