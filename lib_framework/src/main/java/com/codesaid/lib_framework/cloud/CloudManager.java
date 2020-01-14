package com.codesaid.lib_framework.cloud;

import android.content.Context;
import android.media.Image;
import android.net.Uri;

import com.codesaid.lib_framework.utils.log.LogUtils;

import java.io.File;

import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

/**
 * Created By codesaid
 * On :2020-01-11
 * Package Name: com.codesaid.lib_framework.cloud
 * desc : 融云管理
 */
public class CloudManager {
    //Url
    public static final String TOKEN_URL = "http://api-cn.ronghub.com/user/getToken.json";

    //Key
    public static final String CLOUD_KEY = "lmxuhwaglezhd";
    public static final String CLOUD_SECRET = "dxz9J5RPrwY";

    //ObjectName
    public static final String MSG_TEXT_NAME = "RC:TxtMsg";
    public static final String MSG_IMAGE_NAME = "RC:ImgMsg";
    public static final String MSG_LOCATION_NAME = "RC:LBSMsg";

    private static volatile CloudManager mInstance = null;

    private CloudManager() {

    }

    public static CloudManager getInstance() {
        if (mInstance == null) {
            synchronized (CloudManager.class) {
                if (mInstance == null) {
                    mInstance = new CloudManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化 SDK
     *
     * @param context context
     */
    public void initCloud(Context context) {
        RongIMClient.init(context);
    }

    /**
     * 连接融云 服务
     *
     * @param token token
     */
    public void connect(String token) {
        /**
         * <p>连接服务器，在整个应用程序全局，只需要调用一次，需在 {@link #init(Context)} 之后调用。</p>
         * <p>如果调用此接口遇到连接失败，SDK 会自动启动重连机制进行最多10次重连，分别是1, 2, 4, 8, 16, 32, 64, 128, 256, 512秒后。
         * 在这之后如果仍没有连接成功，还会在当检测到设备网络状态变化时再次进行重连。</p>
         *
         * @param token    从服务端获取的用户身份令牌（Token）。
         * @param callback 连接回调。
         * @return RongIMClient  客户端核心类的实例。
         */
        RongIMClient.connect(token, new RongIMClient.ConnectCallback() {
            /**
             * Token 错误。可以从下面两点检查 1.  Token 是否过期，如果过期您需要向 App Server 重新请求一个新的 Token
             *                            2.  token 对应的 appKey 和工程里设置的 appKey 是否一致
             */
            @Override
            public void onTokenIncorrect() {
                LogUtils.e("Token Error");
            }

            /**
             * 连接融云成功
             * @param userId 当前 token 对应的用户 id
             */
            @Override
            public void onSuccess(String userId) {
                LogUtils.e("connect success: " + userId);
                CloudManager.getInstance().sendTextMessage("Hello World!", "7425fe25ae");
            }

            /**
             * 连接融云失败
             * @param errorCode 错误码，可到官网 查看错误码对应的注释
             */
            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtils.e("connect error: " + errorCode);
            }
        });
    }

    /**
     * <p>断开与融云服务器的连接。当调用此接口断开连接后，仍然可以接收 Push 消息。</p>
     * <p>若想断开连接后不接受 Push 消息，可以调用{@link #logout()}</p>
     */
    public void disconnect() {
        RongIMClient.getInstance().disconnect();
    }

    /**
     * <p>断开与融云服务器的连接，并且不再接收 Push 消息。</p>
     * <p>若想断开连接后仍然接受 Push 消息，可以调用 {@link #disconnect()}</p>
     */
    public void logout() {
        RongIMClient.getInstance().logout();
    }

    /**
     * 接受消息的监听器
     *
     * @param listener listener
     */
    public void setOnReceiveMessageListener(RongIMClient.OnReceiveMessageListener listener) {
        RongIMClient.setOnReceiveMessageListener(listener);
    }

    /**
     * 发送文本消息的结果回调
     */
    private IRongCallback.ISendMessageCallback sendMessageCallback = new IRongCallback.ISendMessageCallback() {

        @Override
        public void onAttached(Message message) {
            // 消息成功存到本地数据库的回调
        }

        @Override
        public void onSuccess(Message message) {
            // 消息发送成功的回调
            LogUtils.i("sendMessage Success");
        }

        @Override
        public void onError(Message message, RongIMClient.ErrorCode errorCode) {
            // 消息发送失败的回调
            LogUtils.i("sendMessage Error: " + errorCode);
        }
    };

    /**
     * 发送文本消息
     *
     * @param msg      消息内容
     * @param targetId 目标 id
     */
    public void sendTextMessage(String msg, String targetId) {
        LogUtils.i("sendTextMessage()");

        // 构建文本消息实例
        TextMessage textMessage = TextMessage.obtain(msg);
        /**
         * <p>根据会话类型，发送消息。
         * 通过 {@link io.rong.imlib.IRongCallback.ISendMessageCallback} 中的方法回调发送的消息状态及消息体。</p>
         *
         * @param type        会话类型。
         * @param targetId    目标 Id。根据不同的 conversationType，可能是用户 Id、群组 Id 或聊天室 Id。
         * @param content     消息内容，例如 {@link TextMessage}, {@link ImageMessage}。
         * @param pushContent 当下发 push 消息时，在通知栏里会显示这个字段。
         *                    如果发送的是自定义消息，该字段必须填写，否则无法收到 push 消息。
         *                    如果发送 sdk 中默认的消息类型，例如 RC:TxtMsg, RC:VcMsg, RC:ImgMsg，则不需要填写，默认已经指定。
         * @param pushData    push 附加信息。如果设置该字段，用户在收到 push 消息时，
         *                    能通过 {@link io.rong.push.notification.PushNotificationMessage#getPushData()} 方法获取。
         * @param callback    发送消息的回调。参考 {@link io.rong.imlib.IRongCallback.ISendMessageCallback}。
         *                    {@link #sendMessage(Message, String, String, IRongCallback.ISendMessageCallback)}
         */
        RongIMClient
                .getInstance()
                .sendMessage(
                        Conversation.ConversationType.PRIVATE,
                        targetId,
                        textMessage,
                        null, null,
                        sendMessageCallback);
    }

    /**
     * 发送图片消息 回调
     */
    private RongIMClient.SendImageMessageCallback mSendImageMessageCallback = new RongIMClient.SendImageMessageCallback() {

        @Override
        public void onAttached(Message message) {
            //保存数据库成功

        }

        @Override
        public void onError(Message message, RongIMClient.ErrorCode errorCode) {
            //发送失败
            LogUtils.i("send error: " + errorCode);
        }

        @Override
        public void onSuccess(Message message) {
            //发送成功
            LogUtils.i("send success");
        }

        @Override
        public void onProgress(Message message, int i) {
            //发送进度
        }
    };

    /**
     * 发送图片 消息
     *
     * @param file     图片 file
     * @param targetId 目标用户 id
     */
    public void sendImageMessage(File file, String targetId) {
        /**
         * thumUri:缩略图地址
         * localUri:大图地址
         * isFull:是否发送原图。
         */
        // 生成ImageMessage对象
        ImageMessage imageMessage = ImageMessage.obtain(Uri.fromFile(file), Uri.fromFile(file), true);

        RongIMClient
                .getInstance()
                .sendImageMessage(Conversation.ConversationType.PRIVATE,
                        targetId,
                        imageMessage,
                        null, null,
                        mSendImageMessageCallback);
    }
}
