package com.codesaid.lib_framework.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

/**
 * Created By codesaid
 * On :2019-12-30
 * Package Name: com.codesaid.lib_framework.view
 * desc 自定义 Dialog
 */
public class DialogView extends Dialog {

    public DialogView(@NonNull Context context, int layout, int themeResId, int gravity) {
        super(context, themeResId);
        setContentView(layout);
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = gravity;
        window.setAttributes(layoutParams);

    }
}
