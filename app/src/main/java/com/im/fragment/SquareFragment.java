package com.im.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.codesaid.lib_framework.adapter.CommonAdapter;
import com.codesaid.lib_framework.adapter.CommonViewHolder;
import com.codesaid.lib_framework.base.BaseFragment;
import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.bmob.SquareSet;
import com.im.R;
import com.im.ui.PushSquareActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * Created By codesaid
 * On :2020-01-02
 * Package Name: com.im.fragment
 */
public class SquareFragment extends BaseFragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    /**
     * 1.设计并且实现云数据库 SquareSet
     * 2.实现我们的媒体发送 PsuhSquareActivity
     * 3.实现列表 并且实现我们的文本和图片的发送
     */

    private static final int REQUEST_CODE = 1024;

    private ImageView iv_push;
    private RecyclerView mSquareView;
    private SwipeRefreshLayout mSquareSwipeLayout;
    private View item_empty_view;

    private List<SquareSet> mList = new ArrayList<>();
    private CommonAdapter<SquareSet> mSquareAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_square, null);
        initView(view);
        return view;
    }

    private void initView(View view) {
        iv_push = view.findViewById(R.id.iv_push);
        mSquareView = view.findViewById(R.id.mSquareView);
        mSquareSwipeLayout = view.findViewById(R.id.mSquareSwipeLayout);
        item_empty_view = view.findViewById(R.id.item_empty_view);

        iv_push.setOnClickListener(this);
        mSquareSwipeLayout.setOnRefreshListener(this);

        mSquareAdapter = new CommonAdapter<>(mList, new CommonAdapter.onBindMoreDataListener<SquareSet>() {
            @Override
            public int getItemType(int position) {
                return 0;
            }

            @Override
            public void onBindViewHolder(SquareSet model, CommonViewHolder holder, int type, int position) {

            }

            @Override
            public int getLayoutId(int type) {
                return 0;
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_push:
                Intent intent = new Intent(getActivity(), PushSquareActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                break;
        }
    }

    /**
     * 加载 圈子数据
     */
    public void loadSquare(){
        mSquareSwipeLayout.setRefreshing(true);
        BmobManager.getInstance().querySquareSet(new FindListener<SquareSet>() {
            @Override
            public void done(List<SquareSet> list, BmobException e) {
                mSquareSwipeLayout.setRefreshing(true);
                if (e == null) {
                    if (list != null && list.size() > 0) {
                        //倒序
                        Collections.reverse(list);
                        mSquareView.setVisibility(View.VISIBLE);
                        item_empty_view.setVisibility(View.GONE);

                        if (mList.size() > 0) {
                            mList.clear();
                        }
                        mList.addAll(list);
                        mSquareAdapter.notifyDataSetChanged();
                    } else {
                        mSquareView.setVisibility(View.GONE);
                        item_empty_view.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        loadSquare();
    }
}
