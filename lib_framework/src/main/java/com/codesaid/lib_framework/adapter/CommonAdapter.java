package com.codesaid.lib_framework.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Created By codesaid
 * On :2020-01-10
 * Package Name: com.codesaid.lib_framework.adapter
 * desc : 万能的 RecyclerView Adapter
 */
public class CommonAdapter<T> extends RecyclerView.Adapter<CommonViewHolder> {

    private List<T> mList;

    private onBindDataListener<T> mOnBindDataListener;
    private onBindMoreDataListener<T> mOnBindMoreDataListener;

    public CommonAdapter(List<T> list, onBindDataListener<T> onBindDataListener) {
        mList = list;
        mOnBindDataListener = onBindDataListener;
    }

    public CommonAdapter(List<T> list, onBindMoreDataListener<T> onBindMoreDataListener) {
        mList = list;
        mOnBindDataListener = onBindMoreDataListener;
        mOnBindMoreDataListener = onBindMoreDataListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (mOnBindMoreDataListener != null) {
            return mOnBindMoreDataListener.getItemType(position);
        }
        return 0;
    }

    /**
     * 绑定 单 type 数据
     *
     * @param <T>
     */
    public interface onBindDataListener<T> {
        void onBindViewHolder(T model, CommonViewHolder holder, int type, int position);

        int getLayoutId(int type);
    }

    /**
     * 绑定 多 type 数据
     *
     * @param <T>
     */
    public interface onBindMoreDataListener<T> extends onBindDataListener<T> {
        int getItemType(int position);
    }

    @NonNull
    @Override
    public CommonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mOnBindDataListener != null) {
            int layoutId = mOnBindDataListener.getLayoutId(viewType);
            CommonViewHolder holder = CommonViewHolder.getViewHolder(parent, layoutId);
            return holder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull CommonViewHolder holder, int position) {
        mOnBindDataListener.onBindViewHolder(mList.get(position),
                holder, getItemViewType(position), position);
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }
}
