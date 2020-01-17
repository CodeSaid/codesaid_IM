package com.im.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codesaid.lib_framework.adapter.CommonAdapter;
import com.codesaid.lib_framework.adapter.CommonViewHolder;
import com.codesaid.lib_framework.base.BaseBackActivity;
import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.bmob.IMUser;
import com.codesaid.lib_framework.utils.log.LogUtils;
import com.codesaid.lib_framework.utils.toast.ToastUtils;
import com.codesaid.lib_framework.view.LoadingView;
import com.im.R;
import com.im.adapter.AddFriendAdapter;
import com.im.model.AddFriendModel;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * Created By codesaid
 * On :2020-01-08
 * Package Name: com.im.ui
 * desc : 添加好友页面
 */
public class AddFriendActivity extends BaseBackActivity implements View.OnClickListener {

    //标题
    public static final int TYPE_TITLE = 0;
    //内容
    public static final int TYPE_CONTENT = 1;

    /**
     * 1.模拟用户数据
     * 2.根据条件查询
     * 3.推荐好友
     */

    private LinearLayout ll_to_contact;
    private EditText et_phone;
    private ImageView iv_search;
    private RecyclerView mSearchResultView;

    private View include_empty_view;

    private CommonAdapter<AddFriendModel> mAddFriendAdapter;
    private List<AddFriendModel> mList = new ArrayList<>();
    private LoadingView mLoadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        initView();

        initDialog();
    }

    private void initDialog() {
        mLoadingView = new LoadingView(this);
    }

    private void initView() {
        include_empty_view = findViewById(R.id.include_empty_view);

        ll_to_contact = findViewById(R.id.ll_to_contact);

        et_phone = findViewById(R.id.et_phone);
        iv_search = findViewById(R.id.iv_search);

        mSearchResultView = findViewById(R.id.mSearchResultView);

        ll_to_contact.setOnClickListener(this);
        iv_search.setOnClickListener(this);

        //列表的实现
        mSearchResultView.setLayoutManager(new LinearLayoutManager(this));
        mSearchResultView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // mAddFriendAdapter = new AddFriendAdapter(this, mList);


        //        mAddFriendAdapter.setOnClickListener(new AddFriendAdapter.onClickListener() {
        //            @Override
        //            public void onClick(int position) {
        //
        //            }
        //        });

        mAddFriendAdapter = new CommonAdapter<>(mList, new CommonAdapter.onBindMoreDataListener<AddFriendModel>() {
            @Override
            public int getItemType(int position) {
                return mList.get(position).getType();
            }

            @Override
            public void onBindViewHolder(final AddFriendModel model, CommonViewHolder holder, int type, int position) {
                if (type == TYPE_TITLE) {
                    holder.setText(R.id.tv_title, model.getTitle());
                } else if (type == TYPE_CONTENT) {
                    //设置头像
                    holder.setImgUrl(AddFriendActivity.this, R.id.iv_photo, model.getPhoto());
                    //设置性别
                    holder.setImgResource(R.id.iv_sex,
                            model.isSex() ? R.drawable.img_boy_icon : R.drawable.img_girl_icon);
                    //设置昵称
                    holder.setText(R.id.tv_nickname, model.getNickName());
                    //年龄
                    holder.setText(R.id.tv_age, model.getAge() + getString(R.string.text_search_age));
                    //设置描述
                    holder.setText(R.id.tv_desc, model.getDesc());

                    //  点击事件
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            UserInfoActivity.startActivity(AddFriendActivity.this, model.getUserId());
                        }
                    });
                }
            }

            @Override
            public int getLayoutId(int type) {
                if (type == TYPE_TITLE) {
                    return R.layout.layout_search_title_item;
                } else if (type == TYPE_CONTENT) {
                    return R.layout.layout_search_user_item;
                }
                return 0;
            }
        });

        mSearchResultView.setAdapter(mAddFriendAdapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_search: // 搜索好友
                queryPhoneUser();
                break;
            case R.id.ll_to_contact: // 从通讯录导入
                // 处理权限
                if (checkPermissions(Manifest.permission.READ_CONTACTS)) {
                    startActivity(new Intent(AddFriendActivity.this, ContactFriendActivity.class));
                } else {
                    requestPermission(new String[]{Manifest.permission.READ_CONTACTS});
                }
                break;
        }
    }

    /**
     * 查询好友
     */
    private void queryPhoneUser() {
        //1.获取电话号码
        String phone = et_phone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            ToastUtils.show(AddFriendActivity.this,
                    getResources().getString(R.string.text_login_phone_null));
            return;
        }

        //2.过滤自己
        String phoneNumber = BmobManager.getInstance().getUser().getMobilePhoneNumber();
        LogUtils.i("phoneNumber:" + phoneNumber);
        if (phone.equals(phoneNumber)) {
            ToastUtils.show(AddFriendActivity.this,
                    getResources().getString(R.string.text_add_friend_no_me));
            return;
        }

        // 显示Loading view
        mLoadingView.show("正在查询中...");

        BmobManager.getInstance().queryPhoneUser(phone, new FindListener<IMUser>() {
            @Override
            public void done(List<IMUser> list, BmobException e) {
                if (list != null && list.size() > 0) {
                    mLoadingView.hide();
                    IMUser user = list.get(0);
                    include_empty_view.setVisibility(View.GONE);
                    mSearchResultView.setVisibility(View.VISIBLE);

                    // 清空数据
                    mList.clear();
                    addTitle("查询结果");
                    addContent(user);
                    mAddFriendAdapter.notifyDataSetChanged();

                    // 添加推荐好友
                    pushUser();
                } else {
                    mLoadingView.hide();
                    // 显示空 View
                    include_empty_view.setVisibility(View.VISIBLE);
                    mSearchResultView.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * 推荐好友
     */
    private void pushUser() {
        // 查询所有好友
        BmobManager.getInstance().queryAllUser(new FindListener<IMUser>() {
            @Override
            public void done(List<IMUser> list, BmobException e) {
                if (e == null) {
                    if (!mList.isEmpty()) {
                        addTitle("推荐好友");
                        int num = (list.size() < 100) ? list.size() : 100;
                        for (int i = 0; i < num; i++) {
                            // 过滤自己 和 查询的用户
                            String phoneNumber = BmobManager.getInstance().getUser().getMobilePhoneNumber();
                            String searchNumber = et_phone.getText().toString().trim();
                            if (phoneNumber.equals(list.get(i).getMobilePhoneNumber())
                                    || searchNumber.equals(list.get(i).getMobilePhoneNumber())) {
                                continue;
                            }
                            addContent(list.get(i));
                        }
                        mAddFriendAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    /**
     * 添加 标题
     *
     * @param title title
     */
    private void addTitle(String title) {
        AddFriendModel model = new AddFriendModel();
        model.setType(AddFriendAdapter.TYPE_TITLE);
        model.setTitle(title);
        mList.add(model);
    }

    /**
     * 添加内容
     *
     * @param user user
     */
    private void addContent(IMUser user) {
        AddFriendModel model = new AddFriendModel();
        model.setType(TYPE_CONTENT);
        model.setUserId(user.getObjectId());
        model.setPhoto(user.getPhoto());
        model.setSex(user.isSex());
        model.setAge(user.getAge());
        model.setNickName(user.getNickName());
        model.setDesc(user.getDesc());
        mList.add(model);
    }
}
