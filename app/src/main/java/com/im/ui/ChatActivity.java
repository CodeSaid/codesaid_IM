package com.im.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codesaid.lib_framework.adapter.CommonAdapter;
import com.codesaid.lib_framework.adapter.CommonViewHolder;
import com.codesaid.lib_framework.base.BaseBackActivity;
import com.codesaid.lib_framework.bean.TextBean;
import com.codesaid.lib_framework.bean.VoiceBean;
import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.cloud.CloudManager;
import com.codesaid.lib_framework.entity.Constants;
import com.codesaid.lib_framework.event.EventManager;
import com.codesaid.lib_framework.event.MessageEvent;
import com.codesaid.lib_framework.helper.FileHelper;
import com.codesaid.lib_framework.map.MapManager;
import com.codesaid.lib_framework.utils.log.LogUtils;
import com.codesaid.lib_framework.utils.sp.SpUtils;
import com.codesaid.lib_framework.voice.VoiceManager;
import com.google.gson.Gson;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.im.R;
import com.im.model.ChatModel;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;
import io.rong.message.LocationMessage;
import io.rong.message.TextMessage;

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

    public static final int LOCATION_REQUEST_CODE = 1999;

    private static final int CHAT_INFO_REQUEST_CODE = 1889;

    //背景主题
    private LinearLayout ll_chat_bg;

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

    // 图片文件
    private File uploadFile = null;

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

        ll_chat_bg = (LinearLayout) findViewById(R.id.ll_chat_bg);

        ll_voice = findViewById(R.id.ll_voice);
        ll_camera = findViewById(R.id.ll_camera);
        ll_pic = findViewById(R.id.ll_pic);
        ll_location = findViewById(R.id.ll_location);

        btn_send_msg.setOnClickListener(this);
        ll_voice.setOnClickListener(this);
        ll_camera.setOnClickListener(this);
        ll_pic.setOnClickListener(this);
        ll_location.setOnClickListener(this);

        updateChatTheme();

        mChatView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new CommonAdapter<>(mList, new CommonAdapter.onBindMoreDataListener<ChatModel>() {
            @Override
            public int getItemType(int position) {
                return mList.get(position).getType();
            }

            @Override
            public void onBindViewHolder(final ChatModel model, CommonViewHolder holder, int type, int position) {
                switch (model.getType()) {
                    case TYPE_LEFT_TEXT:
                        holder.setText(R.id.tv_left_text, model.getText());
                        holder.setImgUrl(ChatActivity.this, R.id.iv_left_photo, mYourPhoto);
                        break;
                    case TYPE_LEFT_IMAGE:
                        holder.setImgUrl(ChatActivity.this, R.id.iv_left_img, model.getImgUrl());
                        holder.setImgUrl(ChatActivity.this, R.id.iv_left_photo, mYourPhoto);

                        holder.getView(R.id.iv_left_img).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ImagePreviewActivity.startActivity(ChatActivity.this,
                                        true, model.getImgUrl());
                            }
                        });
                        break;
                    case TYPE_LEFT_LOCATION:
                        holder.setImgUrl(ChatActivity.this, R.id.iv_left_photo, mYourPhoto);
                        holder.setImgUrl(ChatActivity.this, R.id.iv_left_location_img, model.getMapUrl());
                        holder.setText(R.id.tv_left_address, model.getAddress());
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                LocationActivity.startActivity(ChatActivity.this, false,
                                        model.getLa(), model.getLo(), model.getAddress(), LOCATION_REQUEST_CODE);
                            }
                        });
                        break;
                    case TYPE_RIGHT_TEXT:
                        holder.setText(R.id.tv_right_text, model.getText());
                        holder.setImgUrl(ChatActivity.this, R.id.iv_right_photo, mMePhoto);
                        break;
                    case TYPE_RIGHT_IMAGE:
                        if (TextUtils.isEmpty(model.getImgUrl())) {
                            if (model.getLocalFile() != null) {
                                // 加载本地文件
                                holder.setImgFile(ChatActivity.this, R.id.iv_right_img, model.getLocalFile());

                                holder.getView(R.id.iv_right_img).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        ImagePreviewActivity.startActivity(ChatActivity.this,
                                                false, model.getLocalFile().getPath());
                                    }
                                });
                            }
                        } else {
                            holder.setImgUrl(ChatActivity.this, R.id.iv_right_img, model.getImgUrl());

                            holder.getView(R.id.iv_right_img).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ImagePreviewActivity.startActivity(ChatActivity.this,
                                            true, model.getImgUrl());
                                }
                            });
                        }
                        holder.setImgUrl(ChatActivity.this, R.id.iv_right_photo, mMePhoto);
                        break;
                    case TYPE_RIGHT_LOCATION:
                        holder.setImgUrl(ChatActivity.this, R.id.iv_right_photo, mMePhoto);
                        holder.setImgUrl(ChatActivity.this, R.id.iv_right_location_img, model.getMapUrl());
                        holder.setText(R.id.tv_right_address, model.getAddress());
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                LocationActivity.startActivity(ChatActivity.this, false,
                                        model.getLa(), model.getLo(), model.getAddress(), LOCATION_REQUEST_CODE);
                            }
                        });
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
    }

    /**
     * 查询聊天记录
     */
    private void queryMessage() {
        CloudManager.getInstance().getHistoryMessage(mYourId, new RongIMClient.ResultCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> messages) {
                if (messages != null && messages.size() > 0) {
                    try {
                        parseMessage(messages);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
                    parseMessage(messages);
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtils.e("errorCode: " + errorCode);
            }
        });
    }

    /**
     * 解析聊天记录
     *
     * @param messages message
     */
    private void parseMessage(List<Message> messages) {
        // 倒序
        Collections.reverse(messages);

        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            String objectName = message.getObjectName();
            if (objectName.equals(CloudManager.MSG_TEXT_NAME)) {
                TextMessage textMessage = (TextMessage) message.getContent();
                String msg = textMessage.getContent();
                TextBean bean = new Gson().fromJson(msg, TextBean.class);
                if (bean.getType().equals(CloudManager.TYPE_TEXT)) {
                    // 添加到 UI
                    if (message.getSenderUserId().equals(mYourId)) {
                        addLeftText(bean.getMsg());
                    } else {
                        addRightText(bean.getMsg());
                    }
                }
            } else if (objectName.equals(CloudManager.MSG_IMAGE_NAME)) {
                ImageMessage imageMessage = (ImageMessage) message.getContent();
                String url = imageMessage.getRemoteUri().toString();
                if (!TextUtils.isEmpty(url)) {
                    LogUtils.i("Image: " + url);
                }
                // 添加到 UI
                if (message.getSenderUserId().equals(mYourId)) {
                    addLeftImage(url);
                } else {
                    addRightImage(url);
                }
            } else if (objectName.equals(CloudManager.MSG_LOCATION_NAME)) {
                LocationMessage locationMessage = (LocationMessage) message.getContent();
                if (message.getSenderUserId().equals(mYourId)) {
                    addLeftLocation(locationMessage.getLat(), locationMessage.getLng(), locationMessage.getPoi());
                } else {
                    addRightLocation(locationMessage.getLat(), locationMessage.getLng(), locationMessage.getPoi());
                }
            }
        }
    }

    /**
     * 更新主题
     */
    private void updateChatTheme() {
        //主题的选择 0:无主题
        int chat_theme = SpUtils.getInstance().getInt(Constants.SP_CHAT_THEME, 0);
        switch (chat_theme) {
            case 1:
                ll_chat_bg.setBackgroundResource(R.drawable.img_chat_bg_1);
                break;
            case 2:
                ll_chat_bg.setBackgroundResource(R.drawable.img_chat_bg_2);
                break;
            case 3:
                ll_chat_bg.setBackgroundResource(R.drawable.img_chat_bg_3);
                break;
            case 4:
                ll_chat_bg.setBackgroundResource(R.drawable.img_chat_bg_4);
                break;
            case 5:
                ll_chat_bg.setBackgroundResource(R.drawable.img_chat_bg_5);
                break;
            case 6:
                ll_chat_bg.setBackgroundResource(R.drawable.img_chat_bg_6);
                break;
            case 7:
                ll_chat_bg.setBackgroundResource(R.drawable.img_chat_bg_7);
                break;
            case 8:
                ll_chat_bg.setBackgroundResource(R.drawable.img_chat_bg_8);
                break;
            case 9:
                //9的话是默认，可以不设置图片，直接就是纯白
                //ll_chat_bg.setBackgroundResource(R.drawable.img_chat_bg_9);
                break;
        }
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
        switch (view.getId()) {
            case R.id.btn_send_msg:
                String input = et_input_msg.getText().toString();
                if (TextUtils.isEmpty(input)) {
                    return;
                }
                // 发送消息
                CloudManager.getInstance().sendTextMessage(input, CloudManager.TYPE_TEXT, mYourId);
                // 添加 消息  到 UI
                addRightText(input);
                // 清空输入框
                et_input_msg.setText("");
                break;
            case R.id.ll_camera:
                FileHelper.getInstance().toCamera(this);
                break;
            case R.id.ll_pic:
                FileHelper.getInstance().toAlbum(this);
                break;
            case R.id.ll_location: // 发送位置
                LocationActivity.startActivity(ChatActivity.this, true,
                        0, 0, "", LOCATION_REQUEST_CODE);
                break;
            case R.id.ll_voice:
                VoiceManager.getInstance(ChatActivity.this).startSpeak(new RecognizerDialogListener() {
                    /**
                     *
                     * @param recognizerResult 识别的结果
                     * @param b 是否是最后一次返回的结果
                     */
                    @Override
                    public void onResult(RecognizerResult recognizerResult, boolean b) {
                        String result = recognizerResult.getResultString();
                        if (!TextUtils.isEmpty(result)) {
                            LogUtils.i("result: " + result);
                            VoiceBean voiceBean = new Gson().fromJson(result, VoiceBean.class);
                            if (voiceBean.isLs()) {
                                StringBuffer sb = new StringBuffer();
                                for (int i = 0; i < voiceBean.getWs().size(); i++) {
                                    VoiceBean.WsBean wsBean = voiceBean.getWs().get(i);
                                    sb.append(wsBean.getCw().get(0).getW());
                                }
                                LogUtils.i("sb: " + sb.toString());
                                et_input_msg.setText(sb.toString());
                            }
                        }
                    }

                    @Override
                    public void onError(SpeechError speechError) {
                        LogUtils.e("speechError: " + speechError.toString());
                    }
                });
                break;
        }
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

    /**
     * 添加左边图片
     *
     * @param url 图片 url
     */
    private void addLeftImage(String url) {
        ChatModel model = new ChatModel();
        model.setType(TYPE_LEFT_IMAGE);
        model.setImgUrl(url);
        baseAddItem(model);
    }

    /**
     * 添加右边图片
     *
     * @param file 图片 file
     */
    private void addLeftImage(File file) {
        ChatModel model = new ChatModel();
        model.setType(TYPE_LEFT_IMAGE);
        model.setLocalFile(file);
        baseAddItem(model);
    }

    /**
     * 添加右边图片
     *
     * @param url 图片 url
     */
    private void addRightImage(String url) {
        ChatModel model = new ChatModel();
        model.setType(TYPE_RIGHT_IMAGE);
        model.setImgUrl(url);
        baseAddItem(model);
    }

    /**
     * 添加右边图片
     *
     * @param file 图片 file
     */
    private void addRightImage(File file) {
        ChatModel model = new ChatModel();
        model.setType(TYPE_RIGHT_IMAGE);
        model.setLocalFile(file);
        baseAddItem(model);
    }

    /**
     * 添加左边位置
     *
     * @param la      经度
     * @param lo      纬度
     * @param address 地址
     */
    private void addLeftLocation(double la, double lo, String address) {
        ChatModel model = new ChatModel();
        model.setType(TYPE_LEFT_LOCATION);
        model.setLa(la);
        model.setLo(lo);
        model.setAddress(address);
        model.setMapUrl(MapManager.getInstance().getMapUrl(la, lo));
        baseAddItem(model);
    }

    /**
     * 添加右边位置
     *
     * @param la      经度
     * @param lo      纬度
     * @param address 地址
     */
    private void addRightLocation(double la, double lo, String address) {
        ChatModel model = new ChatModel();
        model.setType(TYPE_RIGHT_LOCATION);
        model.setLa(la);
        model.setLo(lo);
        model.setAddress(address);
        model.setMapUrl(MapManager.getInstance().getMapUrl(la, lo));
        baseAddItem(model);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getType()) {
            case EventManager.FLAG_SEND_TEXT:
                if (event.getUserId().equals(mYourId)) {
                    addLeftText(event.getText());
                }
                break;
            case EventManager.FLAG_SEND_IMAGE:
                if (event.getUserId().equals(mYourId)) {
                    addLeftImage(event.getImageUrl());
                }
                break;
            case EventManager.FLAG_SEND_LOCATION:
                if (event.getUserId().equals(mYourId)) {
                    addLeftLocation(event.getLa(), event.getLo(), event.getAddress());
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == FileHelper.CAMERA_CODE) {
                uploadFile = FileHelper.getInstance().getTempFile();
            } else if (requestCode == FileHelper.ALBUM_CODE) {
                Uri uri = data.getData();
                if (uri != null) {
                    //String path = uri.getPath();
                    // 获取真实地址
                    String path = FileHelper.getInstance().getRealPathFromURI(this, uri);
                    LogUtils.i("path: " + path);
                    if (!TextUtils.isEmpty(path)) {
                        uploadFile = new File(path);
                    }
                }
            } else if (requestCode == LOCATION_REQUEST_CODE) {
                double la = data.getDoubleExtra("la", 0);
                double lo = data.getDoubleExtra("l0", 0);
                String address = data.getStringExtra("address");

                LogUtils.e("la: " + la);
                LogUtils.e("lo: " + lo);
                LogUtils.e("address: " + address);

                if (TextUtils.isEmpty(address)) {
                    MapManager.getInstance().poi2address(la, lo, new MapManager.onPoi2addressGeocodeListener() {
                        @Override
                        public void poi2address(String address) {
                            // 发送位置
                            CloudManager.getInstance().sendLocationMessage(la, lo, address, mYourId);
                            // 添加到 UI
                            addRightLocation(la, lo, address);
                        }
                    });
                } else {
                    // 发送位置
                    CloudManager.getInstance().sendLocationMessage(la, lo, address, mYourId);
                    // 添加到 UI
                    addRightLocation(la, lo, address);
                }
            }

            if (uploadFile != null) {
                // 发送图片消息
                CloudManager.getInstance().sendImageMessage(uploadFile, mYourId);
                addRightImage(uploadFile);
                uploadFile = null;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_chat_menu:
                ChatInfoActivity.startChatInfo(this, mYourId, CHAT_INFO_REQUEST_CODE);
                break;
            case R.id.menu_chat_audio:
                if (!checkWindowPermission()) {
                    requestWindowPermission();
                } else {
                    CloudManager.getInstance().startAudioCall(this, mYourId);
                }
                break;
            case R.id.menu_chat_video:
                if (!checkWindowPermission()) {
                    requestWindowPermission();
                } else {
                    CloudManager.getInstance().startVideoCall(this, mYourId);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
