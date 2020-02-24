package com.im.service;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.codesaid.lib_framework.bean.TextBean;
import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.bmob.IMUser;
import com.codesaid.lib_framework.cloud.CloudManager;
import com.codesaid.lib_framework.db.CallRecord;
import com.codesaid.lib_framework.db.LitePalHelper;
import com.codesaid.lib_framework.db.NewFriend;
import com.codesaid.lib_framework.entity.Constants;
import com.codesaid.lib_framework.event.EventManager;
import com.codesaid.lib_framework.event.MessageEvent;
import com.codesaid.lib_framework.helper.GlideHelper;
import com.codesaid.lib_framework.mediaplayer.MediaPlayerManager;
import com.codesaid.lib_framework.notification.NotificationHelper;
import com.codesaid.lib_framework.utils.log.LogUtils;
import com.codesaid.lib_framework.utils.sp.SpUtils;
import com.codesaid.lib_framework.utils.time.TimeUtils;
import com.codesaid.lib_framework.window.WindowHelper;
import com.google.gson.Gson;
import com.im.MainActivity;
import com.im.R;
import com.im.ui.ChatActivity;
import com.im.ui.NewFriendActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import de.hdodenhof.circleimageview.CircleImageView;
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
public class CloudService extends Service implements View.OnClickListener {

    // 计时
    private static final int H_TIME_WHAT = 1100;

    // 通话时间
    private int callTime = 0;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull android.os.Message message) {
            switch (message.what) {
                case H_TIME_WHAT:
                    callTime++;
                    String time = TimeUtils.formatDuring(callTime * 1000);
                    audio_tv_status.setText(time);
                    mSmallTime.setText(time);
                    mHandler.sendEmptyMessageDelayed(H_TIME_WHAT, 1000);
                    break;
            }
            return false;
        }
    });

    private Disposable mDisposable;

    // 音频窗口
    private View mFullAudioView;

    //头像
    private CircleImageView audio_iv_photo;
    //状态
    private TextView audio_tv_status;
    //录音图片
    private ImageView audio_iv_recording;
    //录音按钮
    private LinearLayout audio_ll_recording;
    //接听图片
    private ImageView audio_iv_answer;
    //接听按钮
    private LinearLayout audio_ll_answer;
    //挂断图片
    private ImageView audio_iv_hangup;
    //挂断按钮
    private LinearLayout audio_ll_hangup;
    //免提图片
    private ImageView audio_iv_hf;
    //免提按钮
    private LinearLayout audio_ll_hf;
    //最小化
    private ImageView audio_iv_small;

    //视频窗口
    private View mFullVideoView;
    //大窗口
    private RelativeLayout video_big_video;
    //小窗口
    private RelativeLayout video_small_video;
    //头像
    private CircleImageView video_iv_photo;
    //昵称
    private TextView video_tv_name;
    //状态
    private TextView video_tv_status;
    //个人信息窗口
    private LinearLayout video_ll_info;
    //时间
    private TextView video_tv_time;
    //接听
    private LinearLayout video_ll_answer;
    //挂断
    private LinearLayout video_ll_hangup;

    // 媒体类
    private MediaPlayerManager mAudioCallMedia;
    private MediaPlayerManager mAudioHangupMedia;

    // 摄像类
    private SurfaceView mLocalView;
    private SurfaceView mRemoteView;

    // 通话 id
    private String mCallId = "";

    // 最小化 音频 View
    private WindowManager.LayoutParams mLpSmallViewParams;
    private View mSmallAudioView;

    //时间
    private TextView mSmallTime;

    private int mLastX;
    private int mLastY;

    // 是否移动
    private boolean isMove = false;
    // 是否点击
    private boolean isDrag = false;

    // 拨打电话 标记位
    private int isCallTo = 0;
    // 接通电话 标记位
    private int isReceiverTo = 0;

    // 拨打还是接听
    private boolean isCallOrReceiver = true;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initService();
        initWindow();

        linkCloudServer();
    }

    /**
     * 初始化 服务
     */
    private void initService() {

        EventManager.register(this);

        // 来电 铃声
        mAudioCallMedia = new MediaPlayerManager();
        // 挂断 铃声
        mAudioHangupMedia = new MediaPlayerManager();
        mAudioCallMedia.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mAudioCallMedia.startPlay(CloudManager.callAudioPath);
            }
        });
    }

    /**
     * 初始化 窗口
     */
    private void initWindow() {
        // 音频
        mFullAudioView = WindowHelper.getInstance().getView(R.layout.layout_chat_audio);

        audio_iv_photo = mFullAudioView.findViewById(R.id.audio_iv_photo);
        audio_tv_status = mFullAudioView.findViewById(R.id.audio_tv_status);
        audio_iv_recording = mFullAudioView.findViewById(R.id.audio_iv_recording);
        audio_ll_recording = mFullAudioView.findViewById(R.id.audio_ll_recording);
        audio_iv_answer = mFullAudioView.findViewById(R.id.audio_iv_answer);
        audio_ll_answer = mFullAudioView.findViewById(R.id.audio_ll_answer);
        audio_iv_hangup = mFullAudioView.findViewById(R.id.audio_iv_hangup);
        audio_ll_hangup = mFullAudioView.findViewById(R.id.audio_ll_hangup);
        audio_iv_hf = mFullAudioView.findViewById(R.id.audio_iv_hf);
        audio_ll_hf = mFullAudioView.findViewById(R.id.audio_ll_hf);
        audio_iv_small = mFullAudioView.findViewById(R.id.audio_iv_small);

        audio_ll_recording.setOnClickListener(this);
        audio_ll_answer.setOnClickListener(this);
        audio_ll_hangup.setOnClickListener(this);
        audio_ll_hf.setOnClickListener(this);
        audio_iv_small.setOnClickListener(this);

        //视频
        mFullVideoView = WindowHelper.getInstance().getView(R.layout.layout_chat_video);
        video_big_video = mFullVideoView.findViewById(R.id.video_big_video);
        video_small_video = mFullVideoView.findViewById(R.id.video_small_video);
        video_iv_photo = mFullVideoView.findViewById(R.id.video_iv_photo);
        video_tv_name = mFullVideoView.findViewById(R.id.video_tv_name);
        video_tv_status = mFullVideoView.findViewById(R.id.video_tv_status);
        video_ll_info = mFullVideoView.findViewById(R.id.video_ll_info);
        video_tv_time = mFullVideoView.findViewById(R.id.video_tv_time);
        video_ll_answer = mFullVideoView.findViewById(R.id.video_ll_answer);
        video_ll_hangup = mFullVideoView.findViewById(R.id.video_ll_hangup);

        video_ll_answer.setOnClickListener(this);
        video_ll_hangup.setOnClickListener(this);
        video_small_video.setOnClickListener(this);

        createSmallAudioView();
    }

    /**
     * 创建最小化 音频 窗口
     */
    private void createSmallAudioView() {
        mLpSmallViewParams = WindowHelper.getInstance().createLayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.LEFT);
        mSmallAudioView = WindowHelper.getInstance().getView(R.layout.layout_chat_small_audio);

        mSmallTime = mSmallAudioView.findViewById(R.id.mSmallTime);

        mSmallAudioView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WindowHelper.getInstance().showView(mFullAudioView);
                WindowHelper.getInstance().hideView(mSmallAudioView);
            }
        });

        mSmallAudioView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                /**
                 * OnTouch 和 OnClick 点击冲突
                 * 如何判断是点击 还是 移动
                 * 通过点击下的坐标 - 落地的坐标 如果移动则说明是移动 如果 = 0 ，那说明没有移动则是点击
                 */

                int mStartX = (int) event.getRawX();
                int mStartY = (int) event.getRawY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isMove = false;
                        isDrag = false;

                        mLastX = (int) event.getRawX();
                        mLastY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // 计算偏移量
                        int dx = mStartX - mLastX;
                        int dy = mStartY - mLastY;

                        if (isMove) {
                            isDrag = true;
                        } else {
                            if (dx == 0 && dy == 0) {
                                isMove = false;
                            } else {
                                isMove = true;
                                isDrag = true;
                            }
                        }

                        // 设置移动
                        mLpSmallViewParams.x += dx;
                        mLpSmallViewParams.y += dy;

                        // 重置坐标
                        mLastX = mStartX;
                        mLastY = mStartY;

                        WindowHelper.getInstance().updateView(mSmallAudioView, mLpSmallViewParams);
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return isDrag;
            }
        });
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
                            pushSystem(message.getSenderUserId(), 1, 0, 0, textBean.getMsg());
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
                                                    pushSystem(message.getSenderUserId(), 0, 0, 0, textBean.getMsg());
                                                }
                                            } else {
                                                // 存入数据库
                                                LitePalHelper
                                                        .getInstance()
                                                        .saveNewFriend(textBean.getMsg(), message.getSenderUserId());
                                                pushSystem(message.getSenderUserId(), 0, 0, 0, textBean.getMsg());
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
                                        pushSystem(message.getSenderUserId(), 0, 1, 0, "");
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
                            pushSystem(message.getSenderUserId(), 1, 0, 1, getString(R.string.text_chat_record_img));
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
                    pushSystem(message.getSenderUserId(), 1, 0, 2, getString(R.string.text_chat_record_location));
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
                //LogUtils.i("rongCallSession: " + rongCallSession.toString());

                // 检查设备是否可用
                if (!CloudManager.getInstance().isVoIPEnabled(CloudService.this)) {
                    return;
                }

                /**
                 * 1.获取拨打和接通的ID
                 * 2.来电的话播放铃声
                 * 3.加载个人信息去填充
                 * 4.显示Window
                 */

                // 呼叫端 id
                String callerUserId = rongCallSession.getCallerUserId();

                // 通话 id
                mCallId = rongCallSession.getCallId();

                // 播放来电铃声
                mAudioCallMedia.startPlay(CloudManager.callAudioPath);

                updateWindowInfo(0, rongCallSession.getMediaType(), callerUserId);

                if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)) {
                    // 音频通话
                    LogUtils.i("音频通话");
                    WindowHelper.getInstance().showView(mFullAudioView);

                } else if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)) {
                    // 视频通话
                    LogUtils.i("视频通话");
                    WindowHelper.getInstance().showView(mFullVideoView);
                }

                isReceiverTo = 1;

                isCallOrReceiver = false;
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

                LogUtils.i("onCallOutgoing");

                isCallOrReceiver = true;

                isCallTo = 1;

                // 目标 id
                String targetId = rongCallSession.getTargetId();
                updateWindowInfo(1, rongCallSession.getMediaType(), targetId);

                // 通话 id
                mCallId = rongCallSession.getCallId();

                if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)) {
                    // 音频通话
                    LogUtils.i("音频通话");
                    WindowHelper.getInstance().showView(mFullAudioView);

                } else if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)) {
                    // 视频通话
                    LogUtils.i("视频通话");
                    WindowHelper.getInstance().showView(mFullVideoView);

                    // 显示摄像头
                    mLocalView = surfaceView;
                    video_big_video.addView(mLocalView);
                }
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
                /**
                 * 1. 开始计时
                 * 2. 关闭铃声
                 * 3. 更新按钮状态
                 */

                isCallTo = 2;
                isReceiverTo = 2;

                // 判断是否在播放铃声
                if (mAudioCallMedia.isPlaying()) {
                    // 关闭铃声
                    mAudioCallMedia.stopPlay();
                }

                // 开始计时
                mHandler.sendEmptyMessage(H_TIME_WHAT);

                if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)) {
                    // 音频通话
                    goneAudioView(true, false, true, true, true);
                } else if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)) {
                    // 视频通话
                    goneVideoView(false, true, true, false, true, true);
                    mLocalView = surfaceView;
                    // 双方的相机 全部 刷新

                }
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
                LogUtils.i("onCallDisconnected");
                // 关闭计时
                mHandler.removeMessages(H_TIME_WHAT);

                // 铃声 挂断
                mAudioCallMedia.pausePlay();
                // 播放 挂断 铃声
                mAudioHangupMedia.startPlay(CloudManager.callAudioHangup);

                // 重置计时器
                callTime = 0;
                if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)) {
                    // 音频通话
                    WindowHelper.getInstance().hideView(mFullAudioView);
                    WindowHelper.getInstance().hideView(mSmallAudioView);

                    if (isCallOrReceiver) {
                        if (isCallTo == 1) {
                            // 代表只拨打, 没有接听
                            saveAudioRecord(rongCallSession.getTargetId(), CallRecord.CALL_STATUS_DIAL);
                        } else if (isCallTo == 2) {
                            //代表接通
                            saveAudioRecord(rongCallSession.getTargetId(), CallRecord.CALL_STATUS_ANSWER);
                        }
                    } else {
                        if (isReceiverTo == 1) {
                            saveAudioRecord(rongCallSession.getCallerUserId(), CallRecord.CALL_STATUS_UN_ANSWER);
                        } else if (isReceiverTo == 2) {
                            saveAudioRecord(rongCallSession.getCallerUserId(), CallRecord.CALL_STATUS_ANSWER);
                        }
                    }
                } else if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)) {
                    // 视频通话
                    WindowHelper.getInstance().hideView(mFullVideoView);

                    if (isCallOrReceiver) {
                        if (isCallTo == 1) {
                            // 代表只拨打, 没有接听
                            saveVideoRecord(rongCallSession.getTargetId(), CallRecord.CALL_STATUS_DIAL);
                        } else if (isCallTo == 2) {
                            //代表接通
                            saveVideoRecord(rongCallSession.getTargetId(), CallRecord.CALL_STATUS_ANSWER);
                        }
                    } else {
                        if (isReceiverTo == 1) {
                            saveVideoRecord(rongCallSession.getCallerUserId(), CallRecord.CALL_STATUS_UN_ANSWER);
                        } else if (isReceiverTo == 2) {
                            saveVideoRecord(rongCallSession.getCallerUserId(), CallRecord.CALL_STATUS_ANSWER);
                        }
                    }
                }

                isCallTo = 0;
                isReceiverTo = 0;
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
                // 这是是子线程
                MessageEvent event = new MessageEvent(EventManager.FLAG_SEND_CAMERA);
                event.setSurfaceView(surfaceView);
                EventManager.post(event);
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

    /**
     * @param id          发消息id
     * @param type        0：特殊消息 1：聊天消息
     * @param friendType  0: 添加好友请求 1：同意好友请求
     * @param messageType 0：文本  1：图片 2：位置
     */
    private void pushSystem(final String id, final int type,
                            final int friendType, final int messageType, final String msgText) {
        LogUtils.i("pushSystem");
        BmobManager.getInstance().queryObjectIdUser(id, new FindListener<IMUser>() {
            @Override
            public void done(List<IMUser> list, BmobException e) {
                if (e == null) {
                    if (list != null && list.size() > 0) {
                        IMUser imUser = list.get(0);
                        String text = "";
                        if (type == 0) {
                            switch (friendType) {
                                case 0:
                                    text = imUser.getNickName() + getString(R.string.text_server_noti_send_text);
                                    break;
                                case 1:
                                    text = imUser.getNickName() + getString(R.string.text_server_noti_receiver_text);
                                    break;
                            }
                        } else if (type == 1) {
                            switch (messageType) {
                                case 0:
                                    text = msgText;
                                    break;
                                case 1:
                                    text = getString(R.string.text_chat_record_img);
                                    break;
                                case 2:
                                    text = getString(R.string.text_chat_record_location);
                                    break;
                            }
                        }
                        pushBitmap(type, friendType, imUser, imUser.getNickName(), text, imUser.getPhoto());
                    }
                }
            }
        });
    }

    /**
     * 发送通知
     *
     * @param type       0：特殊消息 1：聊天消息
     * @param friendType 0: 添加好友请求 1：同意好友请求
     * @param imUser     用户对象
     * @param title      标题
     * @param text       内容
     * @param url        头像Url
     */
    private void pushBitmap(final int type, final int friendType, final IMUser imUser, final String title, final String text, String url) {
        LogUtils.i("pushBitmap");
        GlideHelper.loadUrlToBitmap(this, url, new GlideHelper.OnGlideBitmapResultListener() {
            @Override
            public void onResourceReady(Bitmap resource) {
                if (type == 0) {
                    if (friendType == 0) {
                        Intent intent = new Intent(CloudService.this, NewFriendActivity.class);
                        PendingIntent pi = PendingIntent.getActivities(CloudService.this, 0, new Intent[]{intent}, PendingIntent.FLAG_CANCEL_CURRENT);
                        NotificationHelper.getInstance().pushAddFriendNotification(imUser.getObjectId(), title, text, resource, pi);
                    } else if (friendType == 1) {
                        Intent intent = new Intent(CloudService.this, MainActivity.class);
                        PendingIntent pi = PendingIntent.getActivities(CloudService.this, 0, new Intent[]{intent}, PendingIntent.FLAG_CANCEL_CURRENT);
                        NotificationHelper.getInstance().pushArgeedFriendNotification(imUser.getObjectId(), title, text, resource, pi);
                    }
                } else if (type == 1) {
                    Intent intent = new Intent(CloudService.this, ChatActivity.class);
                    intent.putExtra(Constants.INTENT_USER_ID, imUser.getObjectId());
                    intent.putExtra(Constants.INTENT_USER_NAME, imUser.getNickName());
                    intent.putExtra(Constants.INTENT_USER_PHOTO, imUser.getPhoto());
                    PendingIntent pi = PendingIntent.getActivities(CloudService.this, 0, new Intent[]{intent}, PendingIntent.FLAG_CANCEL_CURRENT);
                    NotificationHelper.getInstance().pushMessageNotification(imUser.getObjectId(), title, text, resource, pi);
                }
            }
        });
    }

    /**
     * 更新窗口上的用户信息
     *
     * @param index 0:接听    1:拨打
     * @param type  类型 音频 or  视频
     * @param id    user id
     */
    private void updateWindowInfo(final int index, final RongCallCommon.CallMediaType type, String id) {

        // 音频
        if (type.equals(RongCallCommon.CallMediaType.AUDIO)) {
            if (index == 0) {
                goneAudioView(false, true, true, false, false);
            } else if (index == 1) {
                goneAudioView(false, false, true, false, false);
            }

        } else if (type.equals(RongCallCommon.CallMediaType.VIDEO)) {
            // 视频
            if (index == 0) {
                goneVideoView(true, false, false, true, true, false);
            } else if (index == 1) {
                goneVideoView(true, false, true, false, true, false);
            }
        }

        // 加载信息
        BmobManager.getInstance().queryObjectIdUser(id, new FindListener<IMUser>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void done(List<IMUser> list, BmobException e) {
                if (e == null) {
                    if (list != null && list.size() > 0) {
                        // 呼叫端的 user 信息
                        IMUser user = list.get(0);

                        if (type.equals(RongCallCommon.CallMediaType.AUDIO)) {
                            // 设置呼叫的 user photo
                            GlideHelper.loadUrl(CloudService.this, user.getPhoto(), audio_iv_photo);

                            if (0 == index) {
                                audio_tv_status.setText(user.getNickName()
                                        + getString(R.string.text_service_calling));
                            } else if (1 == index) {
                                audio_tv_status.setText(getString(R.string.text_service_call_ing)
                                        + user.getNickName() + "...");
                            }
                        } else if (type.equals(RongCallCommon.CallMediaType.VIDEO)) {
                            // 设置呼叫的 user photo
                            GlideHelper.loadUrl(CloudService.this, user.getPhoto(), video_iv_photo);

                            if (0 == index) {
                                video_tv_status.setText(user.getNickName()
                                        + getString(R.string.text_service_video_calling));
                            } else if (1 == index) {
                                video_tv_status.setText(getString(R.string.text_service_call_video_ing)
                                        + user.getNickName() + "...");
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * 隐藏 音频通话 view  的 按钮
     */
    private void goneAudioView(boolean recording, boolean answer, boolean hangup, boolean hf,
                               boolean small) {
        // 录音 接听 挂断 免提 最小化
        audio_ll_recording.setVisibility(recording ? View.VISIBLE : View.GONE);
        audio_ll_answer.setVisibility(answer ? View.VISIBLE : View.GONE);
        audio_ll_hangup.setVisibility(hangup ? View.VISIBLE : View.GONE);
        audio_ll_hf.setVisibility(hf ? View.VISIBLE : View.GONE);
        audio_iv_small.setVisibility(small ? View.VISIBLE : View.GONE);
    }

    /**
     * 隐藏 视频通话 view  的 按钮
     */
    private void goneVideoView(boolean info, boolean small,
                               boolean big, boolean answer, boolean hangup,
                               boolean time) {
        // 个人信息 小窗口  接听  挂断 时间
        video_ll_info.setVisibility(info ? View.VISIBLE : View.GONE);
        video_small_video.setVisibility(small ? View.VISIBLE : View.GONE);
        video_big_video.setVisibility(big ? View.VISIBLE : View.GONE);
        video_ll_answer.setVisibility(answer ? View.VISIBLE : View.GONE);
        video_ll_hangup.setVisibility(hangup ? View.VISIBLE : View.GONE);
        video_tv_time.setVisibility(time ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventManager.unregister(this);

        if (!mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    private boolean isRecording = false;
    private boolean isHf = false;

    @SuppressLint("SdCardPath")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.audio_ll_recording:
                // 录音
                if (isRecording) {
                    isRecording = false;
                    CloudManager.getInstance().stopAudioRecording();
                    audio_iv_recording.setImageResource(R.drawable.img_recording);
                } else {
                    isRecording = true;
                    CloudManager.getInstance()
                            .startAudioRecording("/sdcard/codesaid_IM/" + System.currentTimeMillis() + ".wav");
                    audio_iv_recording.setImageResource(R.drawable.img_recording_p);
                }
                break;
            case R.id.audio_ll_answer:
            case R.id.video_ll_answer:
                //接听
                CloudManager.getInstance().acceptCall(mCallId);
                break;
            case R.id.audio_ll_hangup:
            case R.id.video_ll_hangup:
                //挂断
                CloudManager.getInstance().hangUpCall(mCallId);
                break;
            case R.id.audio_ll_hf:
                // 免提
                if (isHf) {
                    isHf = false;
                    CloudManager.getInstance().setEnableSpeakerphone(false);
                    audio_iv_hf.setImageResource(R.drawable.img_hf);
                } else {
                    isHf = true;
                    CloudManager.getInstance().setEnableSpeakerphone(true);
                    audio_iv_hf.setImageResource(R.drawable.img_hf_p);
                }
                break;
            case R.id.audio_iv_small:
                //最小化
                WindowHelper.getInstance().hideView(mFullAudioView);
                WindowHelper.getInstance().showView(mSmallAudioView, mLpSmallViewParams);
                break;
            case R.id.video_small_video:
                // 小窗切换
                isSmallShowLocal = !isSmallShowLocal;
                //小窗切换
                updateVideoView();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getType()) {
            case EventManager.FLAG_SEND_CAMERA:
                SurfaceView surfaceView = event.getSurfaceView();
                if (surfaceView != null) {
                    mRemoteView = surfaceView;
                }
                updateVideoView();
                break;
        }
    }

    // 是否小窗口显示本地视频
    private boolean isSmallShowLocal = false;

    /**
     * 更新 视频 View
     */
    private void updateVideoView() {
        video_big_video.removeAllViews();
        video_small_video.removeAllViews();

        if (isSmallShowLocal) {
            if (mLocalView != null) {
                video_small_video.addView(mLocalView);
                mLocalView.setZOrderOnTop(true);
            }
            if (mRemoteView != null) {
                video_big_video.addView(mRemoteView);
                mRemoteView.setZOrderOnTop(false);
            }
        } else {
            if (mLocalView != null) {
                video_big_video.addView(mLocalView);
                mLocalView.setZOrderOnTop(false);
            }
            if (mRemoteView != null) {
                video_small_video.addView(mRemoteView);
                mRemoteView.setZOrderOnTop(true);
            }
        }
    }

    /**
     * 保存音频通话记录
     *
     * @param userId     用户 id
     * @param callStatus 通话状态
     */
    private void saveAudioRecord(String userId, int callStatus) {
        LitePalHelper.getInstance().saveCallRecord(userId,
                CallRecord.MEDIA_TYPE_AUDIO, callStatus);
    }

    /**
     * 保存视频通话记录
     *
     * @param userId     用户 id
     * @param callStatus 通话状态
     */
    private void saveVideoRecord(String userId, int callStatus) {
        LitePalHelper.getInstance().saveCallRecord(userId,
                CallRecord.MEDIA_TYPE_VIDEO, callStatus);
    }
}
