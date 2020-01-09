package com.im.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codesaid.lib_framework.base.BaseUIActivity;
import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.bmob.IMUser;
import com.im.R;
import com.im.adapter.AddFriendAdapter;
import com.im.model.AddFriendModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;


/**
 * Created By codesaid
 * On :2020-01-08
 * Package Name: com.im.ui
 * desc : 从通讯录导入好友
 */
public class ContactFriendActivity extends BaseUIActivity {

    private RecyclerView mContactView;
    private Map<String, String> mContactMap = new HashMap<>();

    private AddFriendAdapter mContactAdapter;
    private List<AddFriendModel> mList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_friend);

        initView();
    }

    private void initView() {
        mContactView = findViewById(R.id.mContactView);
        mContactView.setLayoutManager(new LinearLayoutManager(this));
        mContactView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mContactAdapter = new AddFriendAdapter(this, mList);

        mContactView.setAdapter(mContactAdapter);

        mContactAdapter.setOnClickListener(new AddFriendAdapter.onClickListener() {
            @Override
            public void onClick(int position) {

            }
        });

        loadContact();

        loadUser();
    }

    /**
     * 加载用户
     */
    private void loadUser() {
        if (mContactMap.size() > 0) {
            for (final Map.Entry<String, String> entry : mContactMap.entrySet()) {
                BmobManager.getInstance().queryPhoneUser(entry.getValue(), new FindListener<IMUser>() {
                    @Override
                    public void done(List<IMUser> list, BmobException e) {
                        if (e == null) {
                            if (!list.isEmpty()) {
                                IMUser user = list.get(0);

                                addContent(user, entry.getKey(), entry.getValue());
                            }
                        }
                    }
                });
            }
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
            while (cursor.moveToNext()) {
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                mContactMap.put(name, phone);
            }
            // 释放
            cursor.close();
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
}
