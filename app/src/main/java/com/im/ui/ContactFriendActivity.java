package com.im.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.codesaid.lib_framework.base.BaseBackActivity;
import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.bmob.IMUser;
import com.codesaid.lib_framework.bmob.PrivateSet;
import com.codesaid.lib_framework.utils.log.LogUtils;
import com.im.R;
import com.im.adapter.AddFriendAdapter;
import com.im.model.AddFriendModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


/**
 * Created By codesaid
 * On :2020-01-08
 * Package Name: com.im.ui
 * desc : 从通讯录导入好友
 */
public class ContactFriendActivity extends BaseBackActivity implements SwipeRefreshLayout.OnRefreshListener {

    private View item_empty_view;
    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mContactView;
    private Map<String, String> mContactMap = new HashMap<>();

    private AddFriendAdapter mContactAdapter;
    private List<AddFriendModel> mList = new ArrayList<>();
    private Disposable mDisposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_friend);

        initView();
    }

    private void initView() {

        mRefreshLayout = findViewById(R.id.mRefreshLayout);
        // 设置刷新
        mRefreshLayout.setOnRefreshListener(this);
        mContactView = findViewById(R.id.mContactView);
        item_empty_view = findViewById(R.id.item_empty_view);
        mContactView.setLayoutManager(new LinearLayoutManager(this));
        mContactView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mContactAdapter = new AddFriendAdapter(this, mList);

        mContactView.setAdapter(mContactAdapter);

        mContactAdapter.setOnClickListener(new AddFriendAdapter.onClickListener() {
            @Override
            public void onClick(int position) {
                UserInfoActivity.startActivity(ContactFriendActivity.this,
                        mList.get(position).getUserId());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadUser();
    }

    /**
     * 加载用户
     */
    private void loadUser() {

        /**
         * 1.拿到用户的联系人列表
         * 2.查询我们的PrivateSet
         * 3.过滤一遍联系人列表
         * 4.去显示
         */

        mRefreshLayout.setRefreshing(true);

        if (mList.size() > 0) {
            mList.clear();
        }

        mDisposable = Observable.create(new ObservableOnSubscribe<List<PrivateSet>>() {
            @Override
            public void subscribe(final ObservableEmitter<List<PrivateSet>> emitter) throws Exception {

                loadContact();

                BmobManager.getInstance().queryPrivateSet(new FindListener<PrivateSet>() {
                    @Override
                    public void done(List<PrivateSet> list, BmobException e) {
                        if (e == null) {
                            emitter.onNext(list);
                            emitter.onComplete();
                        }
                    }
                });
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<PrivateSet>>() {
                    @Override
                    public void accept(List<PrivateSet> privateSets) throws Exception {
                        fixPrivateSet(privateSets);
                    }
                });
    }

    /**
     * 过滤 隐私 联系人
     *
     * @param privateSets privateSets
     */
    private void fixPrivateSet(List<PrivateSet> privateSets) {
        List<String> userPhone = new ArrayList<>();
        for (int i = 0; i < privateSets.size(); i++) {
            PrivateSet set = privateSets.get(i);
            userPhone.add(set.getPhone());
        }

        if (mContactMap.size() > 0) {
            mRefreshLayout.setRefreshing(false);
            for (final Map.Entry<String, String> entry : mContactMap.entrySet()) {

                // 过滤 联系人
                // 判断当前的通讯录联系人是否在私有库中
                if (userPhone.contains(entry.getValue())) {
                    return;
                }

                BmobManager.getInstance().queryPhoneUser(entry.getValue(), new FindListener<IMUser>() {
                    @Override
                    public void done(List<IMUser> list, BmobException e) {
                        if (e == null) {
                            if (list != null && list.size() > 0) {
                                item_empty_view.setVisibility(View.GONE);
                                mContactView.setVisibility(View.VISIBLE);

                                IMUser user = list.get(0);
                                addContent(user, entry.getKey(), entry.getValue());
                            } else {
                                item_empty_view.setVisibility(View.VISIBLE);
                                mContactView.setVisibility(View.GONE);
                            }
                        }
                    }
                });
            }
        } else {
            mRefreshLayout.setRefreshing(false);
            item_empty_view.setVisibility(View.VISIBLE);
            mContactView.setVisibility(View.GONE);
        }
    }

    /**
     * 加载联系人
     */
    private void loadContact() {
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                , null, null, null, null);
        String name;
        String phone;
        // 判断是否有联系人
        if (cursor != null) {

            if (mContactMap.size() > 0) {
                mContactMap.clear();
            }

            while (cursor.moveToNext()) {
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                LogUtils.i("name:" + name + " phone:" + phone);

                phone = phone.replace(" ", "").replace("-", "");

                mContactMap.put(name, phone);
            }
            // 释放
            cursor.close();
        } else {
            item_empty_view.setVisibility(View.VISIBLE);
            mContactView.setVisibility(View.GONE);
        }
    }

    /**
     * 添加内容
     *
     * @param user user
     */
    private void addContent(IMUser user, String name, String phone) {
        AddFriendModel model = new AddFriendModel();
        model.setType(AddFriendAdapter.TYPE_CONTENT);
        model.setUserId(user.getObjectId());
        model.setPhoto(user.getPhoto());
        model.setSex(user.isSex());
        model.setAge(user.getAge());
        model.setNickName(user.getNickName());
        model.setDesc(user.getDesc());

        model.setContact(true);
        model.setContactName(name);
        model.setContactPhone(phone);

        mList.add(model);

        mContactAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    @Override
    public void onRefresh() {
        if (mRefreshLayout.isRefreshing()) {
            loadUser();
        }
    }
}
