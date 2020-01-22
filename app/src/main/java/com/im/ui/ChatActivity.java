package com.im.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codesaid.lib_framework.adapter.CommonAdapter;
import com.codesaid.lib_framework.adapter.CommonViewHolder;
import com.codesaid.lib_framework.base.BaseBackActivity;
import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.cloud.CloudManager;
import com.codesaid.lib_framework.entity.Constants;
import com.codesaid.lib_framework.utils.log.LogUtils;
import com.im.R;
import com.im.model.ChatModel;

import java.util.ArrayList;
import java.util.List;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;

/**
 * Created By codesaid
 * On :2020-01-21
 * Package Name: com.im.ui
 */
public class ChatActivity extends BaseBackActivity implements View.OnClickListener {

    //左边
    public static final int TYPE_LEFT_TEXT = 0;
    public static final int TYPE_LEFT_IMAGE = 1;
    public static final int TYPE_LEFT_LOCATION = 2;

    //右边
    public static final int TYPE_RIGHT_TEXT = 3;
    public static final int TYPE_RIGHT_IMAGE = 4;
    public static final int TYPE_RIGHT_LOCATION = 5;

    // 对方 id
    private String mYourId;
    // 对方 昵称
    private String mYourName;
    // 对方头像
    private String mYourPhoto;

    // 我的  头像
    private String mMePhoto;

    private CommonAdapter<ChatModel> mAdapter;
    private List<ChatModel> mList = new ArrayList<>();

    /**
     * 跳转到当前页面
     *
     * @param context   context
     * @param userId    user id
     * @param userName  user name
     * @param userPhoto user photo
     */
    public static void startActivity(Context context, String userId, String userName, String userPhoto) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(Constants.INTENT_USER_ID, userId);
        intent.putExtra(Constants.INTENT_USER_NAME, userName);
        intent.putExtra(Constants.INTENT_USER_PHOTO, userPhoto);
        context.startActivity(intent);
    }

    //聊天列表
    private RecyclerView mChatView;
    //输入框
    private EditText et_input_msg;
    //发送按钮
    private Button btn_send_msg;
    //语音输入
    private LinearLayout ll_voice;
    //相机
    private LinearLayout ll_camera;
    //图片
    private LinearLayout ll_pic;
    //位置
    private LinearLayout ll_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initView();
    }

    private void initView() {
        mChatView = findViewById(R.id.mChatView);
        et_input_msg = findViewById(R.id.et_input_msg);
        btn_send_msg = findViewById(R.id.btn_send_msg);

        ll_voice = findViewById(R.id.ll_voice);
        ll_camera = findViewById(R.id.ll_camera);
        ll_pic = findViewById(R.id.ll_pic);
        ll_location = findViewById(R.id.ll_location);

        btn_send_msg.setOnClickListener(this);
        ll_voice.setOnClickListener(this);
        ll_camera.setOnClickListener(this);
        ll_pic.setOnClickListener(this);
        ll_location.setOnClickListener(this);

        mChatView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new CommonAdapter<>(mList, new CommonAdapter.onBindMoreDataListener<ChatModel>() {
            @Override
            public int getItemType(int position) {
                return mList.get(position).getType();
            }

            @Override
            public void onBindViewHolder(ChatModel model, CommonViewHolder holder, int type, int position) {
                switch (model.getType()) {
                    case TYPE_LEFT_TEXT:
                        holder.setText(R.id.tv_left_text, model.getText());
                        holder.setImgUrl(ChatActivity.this, R.id.iv_left_photo, mYourPhoto);
                        break;
                    case TYPE_LEFT_IMAGE:
                        break;
                    case TYPE_LEFT_LOCATION:
                        break;
                    case TYPE_RIGHT_TEXT:
                        holder.setText(R.id.tv_right_text, model.getText());
                        holder.setImgUrl(ChatActivity.this, R.id.iv_right_photo, mMePhoto);
                        break;
                    case TYPE_RIGHT_IMAGE:
                        break;
                    case TYPE_RIGHT_LOCATION:
                        break;
                }
            }

            @Override
            public int getLayoutId(int type) {
                if (type == TYPE_LEFT_TEXT) {
                    return R.layout.layout_chat_left_text;
                } else if (type == TYPE_RIGHT_TEXT) {
                    return R.layout.layout_chat_right_text;
                } else if (type == TYPE_LEFT_IMAGE) {
                    return R.layout.layout_chat_left_img;
                } else if (type == TYPE_RIGHT_IMAGE) {
                    return R.layout.layout_chat_right_img;
                } else if (type == TYPE_LEFT_LOCATION) {
                    return R.layout.layout_chat_left_location;
                } else if (type == TYPE_RIGHT_LOCATION) {
                    return R.layout.layout_chat_right_location;
                }
                return 0;
            }
        });
        mChatView.setAdapter(mAdapter);

        loadMeInfo();

        queryMessage();

        addLeftText("hello");
        addRightText("nice to meet you!");
    }

    /**
     * 查询聊天记录
     */
    private void queryMessage() {
        CloudManager.getInstance().getHistoryMessage(mYourId, new RongIMClient.ResultCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> messages) {
                if (messages != null && messages.size() > 0) {

                } else {
                    queryRemoteMessage();
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtils.e("errorCode: " + errorCode);
            }
        });
    }

    /**
     * 查询服务器聊天记录
     */
    private void queryRemoteMessage() {
        CloudManager.getInstance().getRemoteHistoryMessages(mYourId, new RongIMClient.ResultCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> messages) {
                if (messages != null && messages.size() > 0) {

                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtils.e("errorCode: " + errorCode);
            }
        });
    }

    /**
     * 加载自我 info
     */
    private void loadMeInfo() {
        Intent intent = getIntent();
        mYourId = intent.getStringExtra(Constants.INTENT_USER_ID);
        mYourName = intent.getStringExtra(Constants.INTENT_USER_NAME);
        mYourPhoto = intent.getStringExtra(Constants.INTENT_USER_PHOTO);

        mMePhoto = BmobManager.getInstance().getUser().getPhoto();

        // 设置标题
        if (!TextUtils.isEmpty(mYourName)) {
            getSupportActionBar().setTitle(mYourName);
        }
    }

    @Override
    public void onClick(View view) {

    }

    private void baseAddItem(ChatModel model) {
        mList.add(model);
        mAdapter.notifyDataSetChanged();
        // 滑动到底部
        mChatView.scrollToPosition(mList.size() - 1);
    }

    /**
     * 添加左边文字
     *
     * @param text 文字
     */
    private void addLeftText(String text) {
        ChatModel model = new ChatModel();
        model.setType(TYPE_LEFT_TEXT);
        model.setText(text);
        baseAddItem(model);
    }

    /**
     * 添加右  边文字
     *
     * @param text 文字
     */
    private void addRightText(String text) {
        ChatModel model = new ChatModel();
        model.setType(TYPE_RIGHT_TEXT);
        model.setText(text);
        baseAddItem(model);
    }
}
