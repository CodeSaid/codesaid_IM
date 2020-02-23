package com.im.ui;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codesaid.lib_framework.adapter.CommonAdapter;
import com.codesaid.lib_framework.adapter.CommonViewHolder;
import com.codesaid.lib_framework.base.BaseBackActivity;
import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.bmob.IMUser;
import com.codesaid.lib_framework.cloud.CloudManager;
import com.codesaid.lib_framework.db.LitePalHelper;
import com.codesaid.lib_framework.db.NewFriend;
import com.codesaid.lib_framework.event.EventManager;
import com.im.R;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created By codesaid
 * On :2020-01-17
 * Package Name: com.im.ui
 * desc : 新朋友
 */
public class NewFriendActivity extends BaseBackActivity {

    private View item_empty_view;
    private RecyclerView mNewFriendView;
    private Disposable mDisposable;

    private CommonAdapter<NewFriend> mAdapter;
    private List<NewFriend> mList = new ArrayList<>();
    private IMUser mUser;

    private List<IMUser> mUserList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friend);

        initView();

        queryNewFriend();
    }

    private void initView() {
        mNewFriendView = findViewById(R.id.mNewFriendView);
        item_empty_view = findViewById(R.id.item_empty_view);

        mNewFriendView.setLayoutManager(new LinearLayoutManager(this));
        mNewFriendView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        mAdapter = new CommonAdapter<>(mList, new CommonAdapter.onBindDataListener<NewFriend>() {
            @Override
            public void onBindViewHolder(final NewFriend model, final CommonViewHolder holder, int type, final int position) {
                // 查询用户信息
                BmobManager.getInstance().queryObjectIdUser(model.getId(), new FindListener<IMUser>() {
                    @Override
                    public void done(List<IMUser> list, BmobException e) {
                        if (e == null) {
                            mUser = list.get(0);
                            mUserList.add(mUser);
                            holder.setImgUrl(NewFriendActivity.this, R.id.iv_photo,
                                    mUser.getPhoto());
                            holder.setImgResource(R.id.iv_sex, mUser.isSex() ?
                                    R.drawable.img_boy_icon : R.drawable.img_girl_icon);
                            holder.setText(R.id.tv_nickname, mUser.getNickName());
                            holder.setText(R.id.tv_age, mUser.getAge()
                                    + getString(R.string.text_search_age));
                            holder.setText(R.id.tv_desc, mUser.getDesc());
                            holder.setText(R.id.tv_msg, model.getMsg());

                            if (0 == model.getIsAgree()) {
                                holder.getView(R.id.ll_agree).setVisibility(View.GONE);
                                holder.getView(R.id.tv_result).setVisibility(View.VISIBLE);
                                holder.setText(R.id.tv_result, getString(R.string.text_new_friend_agree));
                            } else if (1 == model.getIsAgree()) {
                                holder.getView(R.id.ll_agree).setVisibility(View.VISIBLE);
                                holder.getView(R.id.tv_result).setVisibility(View.GONE);
                                holder.setText(R.id.tv_result, getString(R.string.text_new_friend_no_agree));
                            }
                        }
                    }
                });

                // 同意添加
                holder.getView(R.id.ll_yes).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        updateItem(position, 0);
                        BmobManager.getInstance().addFriend(mUserList.get(position), new SaveListener<String>() {
                            @Override
                            public void done(String s, BmobException e) {
                                if (e == null) {
                                    CloudManager.getInstance().sendTextMessage(
                                            BmobManager.getInstance().getUser().getNickName() + "同意添加你为好友",
                                            CloudManager.TYPE_ARGEED_FRIEND, mUser.getObjectId());
                                    // 刷新好友列表
                                    EventManager.post(EventManager.FLAG_UPDATE_FRIEND);
                                }
                            }
                        });
                    }
                });

                // 拒绝添加
                holder.getView(R.id.ll_no).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        updateItem(position, 1);
                    }
                });
            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layout_new_friend_item;
            }
        });
        mNewFriendView.setAdapter(mAdapter);
    }

    /**
     * 更新 UI
     *
     * @param position user
     * @param i        同意 or  拒绝
     */
    private void updateItem(int position, int i) {
        NewFriend friend = mList.get(position);
        // 更新数据库
        LitePalHelper.getInstance().updateNewFriend(friend.getId(), i);
        // 更新本地数据
        friend.setIsAgree(i);
        mList.set(position, friend);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 查询新的好友申请
     */
    private void queryNewFriend() {
        mDisposable = Observable.create(new ObservableOnSubscribe<List<NewFriend>>() {
            @Override
            public void subscribe(ObservableEmitter<List<NewFriend>> emitter) throws Exception {
                emitter.onNext(LitePalHelper.getInstance().queryNewFriend());
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<NewFriend>>() {
                    @Override
                    public void accept(List<NewFriend> newFriends) throws Exception {
                        // 更新 UI
                        if (newFriends != null && newFriends.size() > 0) {
                            mList.addAll(newFriends);
                            mAdapter.notifyDataSetChanged();
                        } else {
                            item_empty_view.setVisibility(View.VISIBLE);
                            mNewFriendView.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }
}
