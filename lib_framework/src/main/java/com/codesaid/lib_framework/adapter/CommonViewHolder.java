package com.codesaid.lib_framework.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codesaid.lib_framework.helper.GlideHelper;

/**
 * Created By codesaid
 * On :2020-01-10
 * Package Name: com.codesaid.lib_framework.adapter
 * desc : 万能的 RecyclerView ViewHolder
 */
public class CommonViewHolder extends RecyclerView.ViewHolder {

    private SparseArray<View> mViews;
    private View mContentView;


    public CommonViewHolder(@NonNull View itemView) {
        super(itemView);
        mViews = new SparseArray<>();
        mContentView = itemView;
    }

    /**
     * 获取 CommonViewHolder 实体类
     *
     * @param parent   父布局
     * @param layoutId 布局 id
     * @return CommonViewHolder
     */
    public static CommonViewHolder getViewHolder(ViewGroup parent, int layoutId) {
        return new CommonViewHolder(View.inflate(parent.getContext(), layoutId, null));
    }

    /**
     * 提供给外部 访问 view 的方法
     *
     * @param viewId
     * @param <T>
     * @return
     */
    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = mContentView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    /**
     * 设置文本数据
     *
     * @param viewId 控件 id
     * @param text   文本内容
     * @return CommonViewHolder
     */
    public CommonViewHolder setText(int viewId, String text) {
        TextView tv = getView(viewId);
        tv.setText(text);
        return this;
    }

    /**
     * 设置 url 图片
     *
     * @param viewId 控件 id
     * @param url    图片路径
     * @return CommonViewHolder
     */
    public CommonViewHolder setImgUrl(Context context, int viewId, String url) {
        ImageView iv = getView(viewId);
        GlideHelper.loadUrl(context, url, iv);
        return this;
    }

    /**
     * 设置图片背景
     *
     * @param viewId 控件 id
     * @param resId  背景 id
     * @return CommonViewHolder
     */
    public CommonViewHolder setImgResource(int viewId, int resId) {
        ImageView iv = getView(viewId);
        iv.setImageResource(resId);
        return this;
    }

    /**
     * 设置图片背景
     *
     * @param viewId 控件 id
     * @param color  背景 颜色
     * @return CommonViewHolder
     */
    public CommonViewHolder setBackgroundColor(int viewId, int color) {
        ImageView iv = getView(viewId);
        iv.setBackgroundColor(color);
        return this;
    }
}
