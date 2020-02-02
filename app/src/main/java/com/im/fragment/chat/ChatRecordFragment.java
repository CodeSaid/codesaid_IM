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
import com.codesaid.lib_framework.bean.TextBean;
import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.bmob.IMUser;
import com.codesaid.lib_framework.cloud.CloudManager;
import com.google.gson.Gson;
import com.im.R;
import com.im.model.ChatRecordModel;
import com.im.ui.ChatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.message.TextMessage;

/**
 * Created By codesaid
 * On :2020-01-18
 * Package Name: com.im.fragment.chat
 * desc : 聊天记录
 */
public class ChatRecordFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private View item_empty_view;
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
        item_empty_view = view.findViewById(R.id.item_empty_view);
        mChatRecordRefreshLayout = view.findViewById(R.id.mChatRecordRefreshLayout);
        mChatRecordView = view.findViewById(R.id.mChatRecordView);

        // 设置刷新
        mChatRecordRefreshLayout.setOnRefreshListener(this);

        mChatRecordView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mChatRecordView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));

        mAdapter = new CommonAdapter<>(mList, new CommonAdapter.onBindDataListener<ChatRecordModel>() {
            @Override
            public void onBindViewHolder(final ChatRecordModel model, CommonViewHolder holder, int type, int position) {
                holder.setImgUrl(getActivity(), R.id.iv_photo, model.getUrl());
                holder.setText(R.id.tv_nickname, model.getNickName());
                holder.setText(R.id.tv_content, model.getEndMsg());
                holder.setText(R.id.tv_time, model.getTime());
                if (model.getUnReadSize() == 0) {
                    holder.getView(R.id.tv_un_read).setVisibility(View.GONE);
                } else {
                    holder.getView(R.id.tv_un_read).setVisibility(View.VISIBLE);
                    holder.setText(R.id.tv_un_read, model.getUnReadSize() + "");
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ChatActivity.startActivity(getActivity(),
                                model.getUserId(), model.getNickName(), model.getUrl());
                    }
                });
            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layout_chat_record_item;
            }
        });

        mChatRecordView.setAdapter(mAdapter);

        // 避免重复加载
        //queryChatRecord();
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

                    if (mList.size() > 0) {
                        mList.clear();
                    }

                    for (int i = 0; i < conversations.size(); i++) {
                        final Conversation conversation = conversations.get(i);
                        String id = conversation.getTargetId();

                        // 查询对象信息
                        BmobManager.getInstance().queryObjectIdUser(id, new FindListener<IMUser>() {
                            @Override
                            public void done(List<IMUser> list, BmobException e) {
                                if (e == null) {
                                    if (list != null && list.size() > 0) {
                                        IMUser user = list.get(0);
                                        ChatRecordModel model = new ChatRecordModel();
                                        model.setUserId(user.getObjectId());
                                        model.setUrl(user.getPhoto());
                                        model.setNickName(user.getNickName());
                                        model.setTime(new SimpleDateFormat("HH:mm:ss", Locale.CHINA)
                                                .format(conversation.getReceivedTime()));
                                        model.setUnReadSize(conversation.getUnreadMessageCount());

                                        String objectName = conversation.getObjectName();
                                        // 判断消息类型
                                        switch (objectName) {
                                            case CloudManager.MSG_TEXT_NAME:
                                                TextMessage message = (TextMessage) conversation.getLatestMessage();
                                                String msg = message.getContent();
                                                TextBean bean = new Gson().fromJson(msg, TextBean.class);
                                                if (bean.getType().equals(CloudManager.TYPE_TEXT)) {
                                                    model.setEndMsg(bean.getMsg());
                                                    mList.add(model);
                                                }
                                                break;
                                            case CloudManager.MSG_IMAGE_NAME:
                                                model.setEndMsg(getString(R.string.text_chat_record_img));
                                                mList.add(model);
                                                break;
                                            case CloudManager.MSG_LOCATION_NAME:
                                                model.setEndMsg(getString(R.string.text_chat_record_location));
                                                mList.add(model);
                                                break;
                                        }
                                        mAdapter.notifyDataSetChanged();

                                        if (mList.size() > 0) {
                                            item_empty_view.setVisibility(View.GONE);
                                            mChatRecordView.setVisibility(View.VISIBLE);
                                        } else {
                                            item_empty_view.setVisibility(View.VISIBLE);
                                            mChatRecordView.setVisibility(View.GONE);
                                        }
                                    }
                                }
                            }
                        });
                    }
                } else {
                    mChatRecordRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                mChatRecordRefreshLayout.setRefreshing(false);
                item_empty_view.setVisibility(View.VISIBLE);
                mChatRecordView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onRefresh() {
        if (mChatRecordRefreshLayout.isRefreshing()) {
            // 查询聊天记录
            queryChatRecord();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 查询聊天记录
        queryChatRecord();

    }
}
