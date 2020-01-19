package com.im.fragment.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.codesaid.lib_framework.adapter.CommonAdapter;
import com.codesaid.lib_framework.adapter.CommonViewHolder;
import com.codesaid.lib_framework.base.BaseFragment;
import com.codesaid.lib_framework.cloud.CloudManager;
import com.codesaid.lib_framework.utils.log.LogUtils;
import com.im.R;
import com.im.model.ChatRecordModel;

import java.util.ArrayList;
import java.util.List;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

/**
 * Created By codesaid
 * On :2020-01-18
 * Package Name: com.im.fragment.chat
 * desc : 聊天记录
 */
public class CallRecordFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView mChatRecordView;
    private SwipeRefreshLayout mChatRecordRefreshLayout;

    private CommonAdapter<ChatRecordModel> mAdapter;
    private List<ChatRecordModel> mList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_record, null);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mChatRecordRefreshLayout = view.findViewById(R.id.mChatRecordRefreshLayout);
        mChatRecordView = view.findViewById(R.id.mChatRecordView);

        // 设置刷新
        mChatRecordRefreshLayout.setOnRefreshListener(this);

        mChatRecordView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mChatRecordView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));

        mAdapter = new CommonAdapter<>(mList, new CommonAdapter.onBindDataListener<ChatRecordModel>() {
            @Override
            public void onBindViewHolder(ChatRecordModel model, CommonViewHolder holder, int type, int position) {

            }

            @Override
            public int getLayoutId(int type) {
                return 0;
            }
        });

        mChatRecordView.setAdapter(mAdapter);

        queryChatRecord();
    }

    /**
     * 查询聊天记录
     */
    private void queryChatRecord() {
        mChatRecordRefreshLayout.setRefreshing(true);
        CloudManager.getInstance().queryChatRecord(new RongIMClient.ResultCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                mChatRecordRefreshLayout.setRefreshing(false);
                if (conversations != null && conversations.size() > 0) {
                    for (int i = 0; i < conversations.size(); i++) {
                        LogUtils.i("conversations: " + conversations.get(i).toString());
                    }
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                mChatRecordRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onRefresh() {
        if (!mChatRecordRefreshLayout.isRefreshing()) {
            // 查询聊天记录
            queryChatRecord();
        }
    }
}
