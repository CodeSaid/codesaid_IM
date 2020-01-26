package com.im.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.codesaid.lib_framework.bean.TextBean;
import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.cloud.CloudManager;
import com.codesaid.lib_framework.db.LitePalHelper;
import com.codesaid.lib_framework.db.NewFriend;
import com.codesaid.lib_framework.entity.Constants;
import com.codesaid.lib_framework.event.EventManager;
import com.codesaid.lib_framework.event.MessageEvent;
import com.codesaid.lib_framework.utils.log.LogUtils;
import com.codesaid.lib_framework.utils.sp.SpUtils;
import com.google.gson.Gson;

import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;
import io.rong.message.TextMessage;

/**
 * Created By codesaid
 * On :2020-01-04
 * Package Name: com.im.service
 * desc : 云服务
 */
public class CloudService extends Service {

    private Disposable mDisposable;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        linkCloudServer();
    }

    /**
     * 连接云服务
     */
    private void linkCloudServer() {
        // 获取 token
        String token = SpUtils.getInstance().getString(Constants.SP_TOKEN, "");
        LogUtils.e("token: " + token);
        // 连接 服务
        CloudManager.getInstance().connect(token);
        // 接受消息
        CloudManager.getInstance().setOnReceiveMessageListener(new RongIMClient.OnReceiveMessageListener() {
            @Override
            public boolean onReceived(final Message message, int i) {
                LogUtils.e("message: " + message);
                String objectName = message.getObjectName();

                if (objectName.equals(CloudManager.MSG_TEXT_NAME)) { // 文本消息
                    // 获取消息主体
                    TextMessage textMessage = (TextMessage) message.getContent();
                    String content = textMessage.getContent();
                    LogUtils.i("content: " + content);
                    if (!TextUtils.isEmpty(content)) {
                        final TextBean textBean = new Gson().fromJson(content, TextBean.class);

                        if (textBean.getType().equals(CloudManager.TYPE_TEXT)) { // 普通消息
                            MessageEvent event = new MessageEvent(EventManager.FLAG_SEND_TEXT);
                            event.setText(textBean.getMsg());
                            event.setUserId(message.getSenderUserId());
                            EventManager.post(event);
                        } else if (textBean.getType().equals(CloudManager.TYPE_ADD_FRIEND)) { // 添加好友消息
                            LogUtils.i("收到添加好友消息");
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
                                            boolean isHave = false;
                                            if (newFriends != null && newFriends.size() > 0) {
                                                for (int j = 0; j < newFriends.size(); j++) {
                                                    NewFriend friend = newFriends.get(j);
                                                    if (message.getSenderUserId().equals(friend.getId())) {
                                                        isHave = true;
                                                        break;
                                                    }
                                                }
                                                // 防止重复添加
                                                if (!isHave) {
                                                    // 存入数据库
                                                    LitePalHelper
                                                            .getInstance()
                                                            .saveNewFriend(textBean.getMsg(), message.getSenderUserId());
                                                }
                                            } else {
                                                // 存入数据库
                                                LitePalHelper
                                                        .getInstance()
                                                        .saveNewFriend(textBean.getMsg(), message.getSenderUserId());
                                            }
                                        }
                                    });
                        } else if (textBean.getType().equals(CloudManager.TYPE_ARGEED_FRIEND)) { // 同意添加好友消息
                            // 添加到好友列表
                            BmobManager.getInstance().addFriend(message.getSenderUserId(), new SaveListener<String>() {
                                @Override
                                public void done(String s, BmobException e) {
                                    if (e == null) {
                                        // 刷新好友列表
                                        EventManager.post(EventManager.FLAG_UPDATE_FRIEND);
                                    }
                                }
                            });
                        }
                    }
                } else if (objectName.equals(CloudManager.MSG_IMAGE_NAME)) { // 图片消息
                    ImageMessage imageMessage = (ImageMessage) message.getContent();
                    String url = imageMessage.getRemoteUri().toString();
                    if (!TextUtils.isEmpty(url)) {
                        LogUtils.i("Image: " + url);
                        MessageEvent event = new MessageEvent(EventManager.FLAG_SEND_IMAGE);
                        event.setImageUrl(url);
                        event.setUserId(message.getSenderUserId());
                        EventManager.post(event);
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }
}
