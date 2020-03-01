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
import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.bmob.Friend;
import com.codesaid.lib_framework.bmob.IMUser;
import com.codesaid.lib_framework.event.EventManager;
import com.codesaid.lib_framework.event.MessageEvent;
import com.im.R;
import com.im.model.AllFriendModel;
import com.im.ui.UserInfoActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * Created By codesaid
 * On :2020-01-18
 * Package Name: com.im.fragment.chat
 * desc : 全部好友
 */
public class AllFriendFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private View item_empty_view;
    private RecyclerView mAllFriendView;
    private SwipeRefreshLayout mAllFriendRefreshLayout;

    private CommonAdapter<AllFriendModel> mAdapter;
    private List<AllFriendModel> mList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_record, null);
        initView(view);
        return view;
    }

    private void initView(View view) {
        item_empty_view = view.findViewById(R.id.item_empty_view);
        mAllFriendView = view.findViewById(R.id.mAllFriendView);
        mAllFriendRefreshLayout = view.findViewById(R.id.mAllFriendRefreshLayout);

        // 设置下拉刷新
        mAllFriendRefreshLayout.setOnRefreshListener(this);

        mAllFriendView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAllFriendView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));

        mAdapter = new CommonAdapter<>(mList, new CommonAdapter.onBindDataListener<AllFriendModel>() {
            @Override
            public void onBindViewHolder(final AllFriendModel model, CommonViewHolder holder, int type, int position) {
                holder.setImgUrl(getActivity(), R.id.iv_photo, model.getUrl());
                holder.setText(R.id.tv_nickname, model.getNickName());
                holder.setImgResource(R.id.iv_sex, model.isSex()
                        ? R.drawable.img_boy_icon : R.drawable.img_girl_icon);
                holder.setText(R.id.tv_desc, model.getDesc());

                // 跳转到好友信息
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        UserInfoActivity.startActivity(getActivity(), model.getUserId());
                    }
                });
            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layout_all_friend_item;
            }
        });

        mAllFriendView.setAdapter(mAdapter);

        queryMyFriends();
    }

    /**
     * 查询全部好友
     */
    private void queryMyFriends() {
        mAllFriendRefreshLayout.setRefreshing(true);
        BmobManager.getInstance().queryFriends(new FindListener<Friend>() {
            @Override
            public void done(List<Friend> list, BmobException e) {
                mAllFriendRefreshLayout.setRefreshing(false);
                if (e == null) {
                    if (list != null && list.size() > 0) {

                        item_empty_view.setVisibility(View.GONE);
                        mAllFriendView.setVisibility(View.VISIBLE);

                        if (mList.size() > 0)
                            mList.clear();

                        for (int i = 0; i < list.size(); i++) {
                            Friend friend = list.get(i);
                            String id = friend.getFriendUser().getObjectId();
                            BmobManager.getInstance().queryObjectIdUser(id, new FindListener<IMUser>() {
                                @Override
                                public void done(List<IMUser> list, BmobException e) {
                                    if (e == null) {
                                        if (list != null && list.size() > 0) {
                                            IMUser user = list.get(0);
                                            AllFriendModel model = new AllFriendModel();
                                            model.setUserId(user.getObjectId());
                                            model.setUrl(user.getPhoto());
                                            model.setNickName(user.getNickName());
                                            model.setSex(user.isSex());
                                            model.setDesc(getString(R.string.text_all_friend_desc) + user.getDesc());
                                            mList.add(model);
                                            mAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            });

                        }
                    } else {
                        item_empty_view.setVisibility(View.VISIBLE);
                        mAllFriendView.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        if (mAllFriendRefreshLayout.isRefreshing()) {
            queryMyFriends();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getType()) {
            case EventManager.FLAG_UPDATE_FRIEND:
                if (!mAllFriendRefreshLayout.isRefreshing()) {
                    queryMyFriends();
                }
                break;
        }
    }
}
