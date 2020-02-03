package com.im.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.SurfaceView;

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
import io.rong.calllib.IRongCallListener;
import io.rong.calllib.IRongReceivedCallListener;
import io.rong.calllib.RongCallCommon;
import io.rong.calllib.RongCallSession;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;
import io.rong.message.LocationMessage;
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
                    try {
                        ImageMessage imageMessage = (ImageMessage) message.getContent();
                        String url = imageMessage.getRemoteUri().toString();
                        if (!TextUtils.isEmpty(url)) {
                            LogUtils.i("Image: " + url);
                            MessageEvent event = new MessageEvent(EventManager.FLAG_SEND_IMAGE);
                            event.setImageUrl(url);
                            event.setUserId(message.getSenderUserId());
                            EventManager.post(event);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (objectName.equals(CloudManager.MSG_LOCATION_NAME)) {
                    LocationMessage locationMessage = (LocationMessage) message.getContent();

                    MessageEvent event = new MessageEvent(EventManager.FLAG_SEND_LOCATION);
                    event.setLa(locationMessage.getLat());
                    event.setLo(locationMessage.getLng());
                    event.setAddress(locationMessage.getPoi());
                    event.setUserId(message.getSenderUserId());
                    EventManager.post(event);
                }
                return false;
            }
        });

        // 监听通话
        CloudManager.getInstance().setReceivedCallListener(new IRongReceivedCallListener() {
            /**
             * 来电回调
             * @param rongCallSession 通话实体
             */
            @Override
            public void onReceivedCall(RongCallSession rongCallSession) {
                //accept or hangup the call
                String call = new Gson().toJson(rongCallSession);
                LogUtils.i("rongCallSession: " + call);

                if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)) {
                    // 音频通话
                    LogUtils.i("音频通话");
                } else if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)) {
                    // 视频通话
                    LogUtils.i("视频通话");
                }
            }

            /**
             * targetSDKVersion>＝23时检查权限的回调。当targetSDKVersion<23的时候不需要实现。
             * 在这个回调里用户需要使用Android6.0新增的动态权限分配接口requestCallPermissions通知用户授权，
             * 然后在onRequestPermissionResult回调里根据用户授权或者不授权分别回调
             * RongCallClient.getInstance().onPermissionGranted()和
             * RongCallClient.getInstance().onPermissionDenied()来通知CallLib。
             * 其中audio call需要获取Manifest.permission.RECORD_AUDIO权限；
             * video call需要获取Manifest.permission.RECORD_AUDIO和Manifest.permission.CAMERA两项权限。
             * @param rongCallSession 通话实体
             */
            @Override
            public void onCheckPermission(RongCallSession rongCallSession) {
                LogUtils.i("rongCallSession: " + rongCallSession.toString());
            }
        });

        // 监听通话状态
        CloudManager.getInstance().setVoIPCallListener(new IRongCallListener() {
            /**
             * 电话已拨出。
             * 主叫端拨出电话后，通过回调 onCallOutgoing 通知当前 call 的详细信息。
             *
             * @param rongCallSession 通话实体。
             * @param surfaceView  本地 camera 信息。
             */
            @Override
            public void onCallOutgoing(RongCallSession rongCallSession, SurfaceView surfaceView) {

            }

            /**
             * 已建立通话。
             * 通话接通时，通过回调 onCallConnected 通知当前 call 的详细信息。
             *
             * @param rongCallSession 通话实体。
             * @param surfaceView  本地 camera 信息。
             */
            @Override
            public void onCallConnected(RongCallSession rongCallSession, SurfaceView surfaceView) {

            }

            /**
             * 通话结束。
             * 通话中，对方挂断，己方挂断，或者通话过程网络异常造成的通话中断，都会回调 onCallDisconnected。
             *
             * @param rongCallSession 通话实体。
             * @param callDisconnectedReason      通话中断原因。
             */
            @Override
            public void onCallDisconnected(RongCallSession rongCallSession, RongCallCommon.CallDisconnectedReason callDisconnectedReason) {

            }

            /**
             * 被叫端正在振铃。
             * 主叫端拨出电话，被叫端收到请求，发出振铃响应时，回调 onRemoteUserRinging。
             *
             * @param userId 振铃端用户 id。
             */
            @Override
            public void onRemoteUserRinging(String userId) {

            }

            /**
             * 被叫端加入通话。
             * 主叫端拨出电话，被叫端收到请求后，加入通话，回调 onRemoteUserJoined。
             *
             * @param userId      加入用户的 id。
             * @param callMediaType   加入用户的媒体类型，audio or video。
             * @param userType    加入用户的类型，正常用户或者观察者
             * @param surfaceView 加入用户者的 camera 信息。
             */
            @Override
            public void onRemoteUserJoined(String userId, RongCallCommon.CallMediaType callMediaType,
                                           int userType, SurfaceView surfaceView) {

            }

            /**
             * 通话中的某一个参与者，邀请好友加入通话，发出邀请请求后，回调 onRemoteUserInvited。
             *
             * @param userId    被邀请者的 id。
             * @param callMediaType 被邀请者的 id。
             */
            @Override
            public void onRemoteUserInvited(String userId, RongCallCommon.CallMediaType callMediaType) {

            }

            /**
             * 通话中的远端参与者离开。
             * 回调 onRemoteUserLeft 通知状态更新。
             *
             * @param userId 远端参与者的 id。
             * @param callDisconnectedReason 远端参与者离开原因。
             */
            @Override
            public void onRemoteUserLeft(String userId, RongCallCommon.CallDisconnectedReason callDisconnectedReason) {

            }

            /**
             * 当通话中的某一个参与者切换通话类型，例如由 audio 切换至 video，回调 onMediaTypeChanged。
             *
             * @param userId    切换者的 userId。
             * @param callMediaType 切换者，切换后的媒体类型。
             * @param surfaceView     切换着，切换后的 camera 信息，如果由 video 切换至 audio，则为 null。
             */
            @Override
            public void onMediaTypeChanged(String userId, RongCallCommon.CallMediaType callMediaType, SurfaceView surfaceView) {

            }

            /**
             * 通话过程中，发生异常。
             *
             * @param callErrorCode 异常原因。
             */
            @Override
            public void onError(RongCallCommon.CallErrorCode callErrorCode) {

            }

            /**
             * 远端参与者 camera 状态发生变化时，回调 onRemoteCameraDisabled 通知状态变化。
             *
             * @param userId   远端参与者 id。
             * @param disabled 远端参与者 camera 是否可用。
             */
            @Override
            public void onRemoteCameraDisabled(String userId, boolean disabled) {

            }


            @Override
            public void onRemoteMicrophoneDisabled(String s, boolean b) {

            }

            @Override
            public void onNetworkReceiveLost(String s, int i) {

            }

            @Override
            public void onNetworkSendLost(int i, int i1) {

            }

            @Override
            public void onFirstRemoteVideoFrame(String s, int i, int i1) {

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
