package com.im.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codesaid.lib_framework.adapter.CommonAdapter;
import com.codesaid.lib_framework.adapter.CommonViewHolder;
import com.codesaid.lib_framework.base.BaseUIActivity;
import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.bmob.Friend;
import com.codesaid.lib_framework.bmob.IMUser;
import com.codesaid.lib_framework.cloud.CloudManager;
import com.codesaid.lib_framework.entity.Constants;
import com.codesaid.lib_framework.helper.GlideHelper;
import com.codesaid.lib_framework.utils.toast.ToastUtils;
import com.codesaid.lib_framework.view.DialogManager;
import com.codesaid.lib_framework.view.DialogView;
import com.im.R;
import com.im.model.UserInfoModel;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created By codesaid
 * On :2020-01-13
 * Package Name: com.im.ui
 * desc : 用户信息 Activity
 */
public class UserInfoActivity extends BaseUIActivity implements View.OnClickListener {

    private DialogView mAddFriendDialog;
    private EditText et_msg;
    private TextView tv_cancel;
    private TextView tv_add_friend;

    /**
     * 跳转到 UserInfoActivity
     *
     * @param context context
     * @param userId  用户 id
     */
    public static void startActivity(Context context, String userId) {
        Intent intent = new Intent(context, UserInfoActivity.class);
        intent.putExtra(Constants.INTENT_USER_ID, userId);
        context.startActivity(intent);
    }

    private RelativeLayout ll_back;

    private CircleImageView iv_user_photo;
    private TextView tv_nickname;
    private TextView tv_desc;

    private RecyclerView mUserInfoView;
    private CommonAdapter<UserInfoModel> mUserInfoAdapter;
    private List<UserInfoModel> mList = new ArrayList<>();

    private Button btn_add_friend;
    private Button btn_chat;
    private Button btn_audio_chat;
    private Button btn_video_chat;

    private LinearLayout ll_is_friend;

    // 用户 id
    private String userId = "";

    //个人信息颜色
    private int[] mColor = {0x881E90FF, 0x8800FF7F, 0x88FFD700, 0x88FF6347, 0x88F08080, 0x8840E0D0};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        initView();
    }

    private void initView() {

        initAddFriendDialog();

        userId = getIntent().getStringExtra(Constants.INTENT_USER_ID);

        ll_back = findViewById(R.id.ll_back);
        iv_user_photo = findViewById(R.id.iv_user_photo);
        tv_nickname = findViewById(R.id.tv_nickname);
        tv_desc = findViewById(R.id.tv_desc);
        mUserInfoView = findViewById(R.id.mUserInfoView);
        btn_add_friend = findViewById(R.id.btn_add_friend);
        btn_chat = findViewById(R.id.btn_chat);
        btn_audio_chat = findViewById(R.id.btn_audio_chat);
        btn_video_chat = findViewById(R.id.btn_video_chat);
        ll_is_friend = findViewById(R.id.ll_is_friend);

        ll_back.setOnClickListener(this);
        btn_add_friend.setOnClickListener(this);
        btn_chat.setOnClickListener(this);
        btn_audio_chat.setOnClickListener(this);
        btn_video_chat.setOnClickListener(this);
        iv_user_photo.setOnClickListener(this);

        mUserInfoAdapter = new CommonAdapter<>(mList, new CommonAdapter.onBindDataListener<UserInfoModel>() {
            @Override
            public void onBindViewHolder(UserInfoModel model, CommonViewHolder holder, int type, int position) {
                //holder.setBackgroundColor(R.id.ll_bg, model.getBgColor());
                holder.getView(R.id.ll_bg).setBackgroundColor(model.getBgColor());
                holder.setText(R.id.tv_type, model.getTitle());
                holder.setText(R.id.tv_content, model.getContent());
            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layout_user_info_item;
            }
        });

        mUserInfoView.setLayoutManager(new GridLayoutManager(this, 3));
        mUserInfoView.setAdapter(mUserInfoAdapter);

        queryUserInfo();
    }

    /**
     * 添加好友提示框
     */
    @SuppressLint("SetTextI18n")
    private void initAddFriendDialog() {
        mAddFriendDialog = DialogManager.getInstance().initView(this, R.layout.dialog_send_friend);

        et_msg = mAddFriendDialog.findViewById(R.id.et_msg);
        tv_cancel = mAddFriendDialog.findViewById(R.id.tv_cancel);
        tv_add_friend = mAddFriendDialog.findViewById(R.id.tv_add_friend);

        et_msg.setText(getString(R.string.text_me_info_tips)
                + BmobManager.getInstance().getUser().getNickName());

        tv_cancel.setOnClickListener(this);
        tv_add_friend.setOnClickListener(this);
    }

    /**
     * 查询用户 信息
     */
    private void queryUserInfo() {
        if (TextUtils.isEmpty(userId)) {
            return;
        }
        BmobManager.getInstance().queryObjectIdUser(userId, new FindListener<IMUser>() {
            @Override
            public void done(List<IMUser> list, BmobException e) {
                if (e == null) {
                    if (list != null && list.size() > 0) {
                        IMUser user = list.get(0);

                        updateUserInfo(user);
                    }
                }
            }
        });

        // 判断是否是好友
        BmobManager.getInstance().queryFriends(new FindListener<Friend>() {
            @Override
            public void done(List<Friend> list, BmobException e) {
                if (e == null) {
                    if (list != null && list.size() > 0) {
                        for (int i = 0; i < list.size(); i++) {
                            Friend friend = list.get(i);
                            // 判断这个对象中的 id 是否  跟目前的 userId 相同
                            if (friend.getFriendUser().getObjectId().equals(userId)) {
                                // 是好友关系
                                btn_add_friend.setVisibility(View.GONE);
                                ll_is_friend.setVisibility(View.VISIBLE);
                            }
                        }
                    } else {
                        // 非好友
                    }
                }
            }
        });
    }

    /**
     * 更新用户信息
     *
     * @param user user
     */
    private void updateUserInfo(IMUser user) {
        // 设置用户信息
        GlideHelper.loadUrl(UserInfoActivity.this, user.getPhoto(),
                iv_user_photo);
        tv_nickname.setText(user.getNickName());
        tv_desc.setText(user.getDesc());

        //性别 年龄 生日 星座 爱好 单身状态
        addUserInfoModel(mColor[0], getString(R.string.text_me_info_sex),
                user.isSex() ? getString(R.string.text_me_info_boy)
                        : getString(R.string.text_me_info_girl));
        addUserInfoModel(mColor[1], getString(R.string.text_me_info_age),
                user.getAge() + getString(R.string.text_search_age));
        addUserInfoModel(mColor[2], getString(R.string.text_me_info_birthday), user.getBirthday());
        addUserInfoModel(mColor[3], getString(R.string.text_me_info_constellation), user.getConstellation());
        addUserInfoModel(mColor[4], getString(R.string.text_me_info_hobby), user.getHobby());
        addUserInfoModel(mColor[5], getString(R.string.text_me_info_status), user.getStatus());
        //刷新数据
        mUserInfoAdapter.notifyDataSetChanged();
    }

    /**
     * 添加数据
     *
     * @param color   背景颜色
     * @param title   标题
     * @param content 内容
     */
    private void addUserInfoModel(int color, String title, String content) {
        UserInfoModel model = new UserInfoModel();
        model.setTitle(title);
        model.setContent(content);
        model.setBgColor(color);
        mList.add(model);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel: // 添加好友提示框 取消按钮
                DialogManager.getInstance().hide(mAddFriendDialog);
                break;
            case R.id.tv_add_friend: // 添加好友提示框 确定按钮
                DialogManager.getInstance().hide(mAddFriendDialog);
                String msg = et_msg.getText().toString().trim();
                if (TextUtils.isEmpty(msg)) {
                    msg = getString(R.string.text_me_info_tips) + BmobManager.getInstance().getUser().getNickName();
                }
                // 发送添加好友信息
                CloudManager.getInstance().sendTextMessage(msg, CloudManager.TYPE_ADD_FRIEND, userId);
                ToastUtils.show(UserInfoActivity.this, "发送成功");
                break;
            case R.id.ll_back:
                finish();
                break;
            case R.id.iv_user_photo:

                break;
            case R.id.btn_add_friend:
                DialogManager.getInstance().show(mAddFriendDialog);
                break;
            case R.id.btn_chat:

                break;
            case R.id.btn_audio_chat:

                break;
            case R.id.btn_video_chat:

                break;
        }
    }
}
