package com.codesaid.lib_framework.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

/**
 * Created By codesaid
 * On :2020-01-10
 * Package Name: com.codesaid.lib_framework.view
 * desc : 个人页面 ----> 头部拉伸 View
 */
public class HeadZoomScrollView extends ScrollView {

    //头部View
    private View mZoomView;
    private int mZoomViewWidth;
    private int mZoomViewHeight;

    //是否在滑动
    private boolean isScrolling = false;
    //第一次按下的坐标
    private float firstPosition;
    //滑动系数
    private float mScrollRate = 0.3f;
    //回弹系数
    private float mReplyRate = 0.5f;

    public HeadZoomScrollView(Context context) {
        super(context);
    }

    public HeadZoomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeadZoomScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 1.获取头部的View(操控的View)
     * 2.滑动事件的处理
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildAt(0) != null) {
            ViewGroup vg = (ViewGroup) getChildAt(0);
            if (vg.getChildAt(0) != null) {
                mZoomView = vg.getChildAt(0);
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //获取View的宽高
        if (mZoomViewWidth <= 0 || mZoomViewHeight <= 0) {
            mZoomViewWidth = mZoomView.getMeasuredWidth();
            mZoomViewHeight = mZoomView.getMeasuredHeight();
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (!isScrolling) {
                    //说明没有滑动
                    if (getScrollY() == 0) {
                        //没有滑动说明是第一次滑动 记录
                        firstPosition = ev.getY();
                    } else {
                        break;
                    }
                }
                //计算缩放值
                //公式：(当前的位置 - 第一次按下的位置) * 缩放系数
                int distance = (int) ((ev.getY() - firstPosition) * mScrollRate);
                if (distance < 0) {
                    break;
                }
                isScrolling = true;
                setZoomView(distance);
                break;
            case MotionEvent.ACTION_UP:
                isScrolling = false;
                replyZoomView();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 回调 View 动画
     */
    private void replyZoomView() {
        //计算下拉的缩放值再让属性动画根据这个值复原
        int distance = mZoomView.getMeasuredWidth() - mZoomViewWidth;
        ValueAnimator animator = ValueAnimator.ofFloat(distance, 0)
                .setDuration((long) (distance * mReplyRate));
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setZoomView((Float) animation.getAnimatedValue());
            }
        });
        animator.start();
    }

    /**
     * 缩放View
     *
     * @param distance 缩放比例
     */
    private void setZoomView(float distance) {
        if (mZoomViewWidth <= 0 || mZoomViewHeight <= 0) {
            return;
        }
        ViewGroup.LayoutParams layoutParams = mZoomView.getLayoutParams();
        layoutParams.width = (int) (mZoomViewWidth + distance);
        // 现在的宽/原本的宽 得到 缩放比例 * 原本的高 得到缩放的高
        layoutParams.height = (int) (mZoomViewHeight * ((mZoomViewWidth + distance) / mZoomViewWidth));
        //设置间距
        //公式：- (layoutParams.width - mZoomViewWidth) / 2
        ((MarginLayoutParams) layoutParams).setMargins(-(layoutParams.width - mZoomViewWidth) / 2, 0, 0, 0);
        mZoomView.setLayoutParams(layoutParams);
    }
}
