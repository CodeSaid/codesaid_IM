package com.codesaid.lib_framework.mediaplayer;

import android.annotation.SuppressLint;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.codesaid.lib_framework.utils.log.LogUtils;

import java.io.IOException;

/**
 * Created By codesaid
 * On :2019-12-23
 * Package Name: com.codesaid.lib_framework.mediaplayer
 */
public class MediaPlayerManager {

    // 播放
    public static final int MEDIA_STATUS_PLAY = 0;
    // 暂停
    public static final int MEDIA_STATUS_PAUSE = 1;
    // 停止
    public static final int MEDIA_STATUS_STOP = 2;

    // 当前播放状态
    public  int MEDIA_STATUS_CURRENT = MEDIA_STATUS_STOP;

    private OnProgressListener onProgressListener;

    private MediaPlayer mMediaPlayer;

    private static final int H_PROGRESS = 1000;

    /**
     * 计算歌曲进度
     * 1. 开始播放的时候就开启循环计算时长
     * 2. 将进度计算结果对外抛出
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case H_PROGRESS:
                    if (onProgressListener != null) {
                        // 获取当前播放进度
                        int currentPosition = getCurrentPosition();
                        int pos = (int) (((float) currentPosition) / ((float) getDuration()) * 100);
                        onProgressListener.onProgress(currentPosition, pos);
                        mHandler.sendEmptyMessageDelayed(H_PROGRESS, 1000);
                    }
                    break;
            }
            return false;
        }
    }) {
    };

    public MediaPlayerManager() {
        mMediaPlayer = new MediaPlayer();
    }

    /**
     * 播放
     *
     * @param path 播放路径
     */
    public void startPlay(String path) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            MEDIA_STATUS_CURRENT = MEDIA_STATUS_PLAY;
            mHandler.sendEmptyMessage(H_PROGRESS);
        } catch (IOException e) {
            LogUtils.e(e.toString());
            e.printStackTrace();
        }
    }

    public void startPlay(AssetFileDescriptor path) {
        try {
            mMediaPlayer.reset();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mMediaPlayer.setDataSource(path);
            } else {
                mMediaPlayer.setDataSource(path.getFileDescriptor(), path.getStartOffset(), path.getLength());
            }
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            MEDIA_STATUS_CURRENT = MEDIA_STATUS_PLAY;
            mHandler.sendEmptyMessage(H_PROGRESS);
        } catch (IOException e) {
            LogUtils.e(e.toString());
            e.printStackTrace();
        }
    }

    /**
     * 暂停播放
     */
    public void pausePlay() {
        if (isPlaying()) {
            MEDIA_STATUS_CURRENT = MEDIA_STATUS_PAUSE;
            mMediaPlayer.pause();
            mHandler.removeMessages(H_PROGRESS);
        }
    }

    /**
     * 继续播放
     */
    public void continuePlay() {
        mMediaPlayer.start();
        MEDIA_STATUS_CURRENT = MEDIA_STATUS_PLAY;
        mHandler.sendEmptyMessage(H_PROGRESS);
    }

    /**
     * 停止播放
     */
    public void stopPlay() {
        mMediaPlayer.stop();
        MEDIA_STATUS_CURRENT = MEDIA_STATUS_STOP;
        mHandler.removeMessages(H_PROGRESS);
    }

    /**
     * 获取当前播放位置
     *
     * @return 当前播放位置
     */
    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    /**
     * 获取播放的文件总时长
     *
     * @return 播放文件的总时长
     */
    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    /**
     * 是否循环播放
     *
     * @param isLooping true : 循环播放
     */
    public void setLooping(boolean isLooping) {
        mMediaPlayer.setLooping(isLooping);
    }

    /**
     * 跳转到指定位置播放
     *
     * @param position 指定的播放位置
     */
    public void seekTo(int position) {
        mMediaPlayer.seekTo(position);
    }

    /**
     * 波动结束监听
     *
     * @param listener listener
     */
    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
        mMediaPlayer.setOnCompletionListener(listener);
    }

    /**
     * 播放出错监听
     *
     * @param listener listener
     */
    public void seOnErrortListener(MediaPlayer.OnErrorListener listener) {
        mMediaPlayer.setOnErrorListener(listener);
    }

    /**
     * 播放进度监听
     *
     * @param listener listener
     */
    public void setOnProgressListener(OnProgressListener listener) {
        onProgressListener = listener;
    }

    public interface OnProgressListener {
        /**
         * @param position 当前播放进度
         * @param pos      当前播放进度的百分比
         */
        void onProgress(int position, int pos);
    }

    /**
     * 判断当时是否是正在播放
     *
     * @return true 正在播放
     */
    private boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }
}
