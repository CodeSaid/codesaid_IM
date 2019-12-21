package com.codesaid.lib_framework.utils.ui;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;

/**
 * Created By codesaid
 * On :2019-12-22
 * Package Name: com.codesaid.lib_framework.utils.ui
 */
public class SystemUI {

    public static void fixSystemUI(Activity activity) {
        // Android 5.0
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }
}
