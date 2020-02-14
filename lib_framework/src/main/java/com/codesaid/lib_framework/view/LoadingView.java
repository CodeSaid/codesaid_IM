package com.codesaid.lib_framework.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.codesaid.lib_framework.R;
import com.codesaid.lib_framework.utils.anim.AnimUtils;

/**
 * Created By codesaid
 * On :2019-12-30
 * Package Name: com.codesaid.lib_framework.view
 * desc : 加载等待 提示框
 */
public class LoadingView {

    private DialogView mLoadingView;

    private ImageView mIvLoading;
    private TextView mTvLoadingText;

    private ObjectAnimator mAnimator;

    public LoadingView(Context context) {
        mLoadingView = DialogManager.getInstance().initView(context, R.layout.dialog_loding);

        mIvLoading = mLoadingView.findViewById(R.id.iv_loding);
        mTvLoadingText = mLoadingView.findViewById(R.id.tv_loding_text);

        mAnimator = AnimUtils.rotation(mIvLoading);
    }

    /**
     * 设置加载的提示文本
     *
     * @param desc text
     */
    public void setLoadingText(String desc) {
        if (!TextUtils.isEmpty(desc)) {
            mTvLoadingText.setText(desc);
        }
    }

    /**
     * show view
     */
    public void show() {
        mAnimator.start();
        DialogManager.getInstance().show(mLoadingView);
    }

    public void show(String desc) {
        mAnimator.start();
        setLoadingText(desc);
        DialogManager.getInstance().show(mLoadingView);
    }

    /**
     * hide view
     */
    public void hide() {
        mAnimator.pause();
        DialogManager.getInstance().hide(mLoadingView);
    }

    /**
     * 设置外部是否可以点击
     *
     * @param flag true or false
     */
    public void setCancelable(boolean flag) {
        mLoadingView.setCancelable(flag);

    }
}
