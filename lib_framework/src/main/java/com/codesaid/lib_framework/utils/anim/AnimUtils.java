package com.codesaid.lib_framework.utils.anim;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created By codesaid
 * On :2019-12-26
 * Package Name: com.codesaid.lib_framework.utils.anim
 * desc : 动画工具类
 */
public class AnimUtils {

    /**
     * 旋转动画
     *
     * @param view
     * @return
     */
    public static ObjectAnimator rotation(View view) {
        // 旋转动画
        ObjectAnimator mAnimator = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
        mAnimator.setDuration(3 * 1000);
        mAnimator.setRepeatMode(ValueAnimator.RESTART);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.setInterpolator(new LinearInterpolator());
        return mAnimator;
    }
}
