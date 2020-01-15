package com.im.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.codesaid.lib_framework.bean.TextBean;
import com.codesaid.lib_framework.cloud.CloudManager;
import com.codesaid.lib_framework.entity.Constants;
import com.codesaid.lib_framework.utils.log.LogUtils;
import com.codesaid.lib_framework.utils.sp.SpUtils;
import com.google.gson.Gson;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;

/**
 * Created By codesaid
 * On :2020-01-04
 * Package Name: com.im.service
 * desc : 云服务
 */
public class CloudService extends Service {

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
            public boolean onReceived(Message message, int i) {
                LogUtils.e("message: " + message);
                String objectName = message.getObjectName();

                if (objectName.equals(CloudManager.MSG_TEXT_NAME)) { // 文本消息
                    // 获取消息主体
                    TextMessage textMessage = (TextMessage) message.getContent();
                    String content = textMessage.getContent();
                    LogUtils.i("content: " + content);
                    if (!TextUtils.isEmpty(content)) {
                        TextBean textBean = new Gson().fromJson(content, TextBean.class);

                        if (textBean.getType().equals(CloudManager.TYPE_TEXT)) { // 普通消息

                        } else if (textBean.getType().equals(CloudManager.TYPE_ADD_FRIEND)) { // 添加好友消息
                            // 存入数据库
                            LogUtils.i("收到添加好友消息");
                        } else if (textBean.getType().equals(CloudManager.TYPE_ARGEED_FRIEND)) { // 同意添加好友消息

                        }
                    }
                }
                return false;
            }
        });
    }
}
