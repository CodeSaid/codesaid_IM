package com.codesaid.lib_framework.mediaplayer;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;

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
    public static final int MEDIA_STATUS_PAUSE = 0;
    // 停止
    public static final int MEDIA_STATUS_STOP = 0;

    // 当前播放状态
    public static int MEDIA_STATUS_CUEERNT = MEDIA_STATUS_STOP;

    private MediaPlayer mMediaPlayer;

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
            MEDIA_STATUS_CUEERNT = MEDIA_STATUS_PLAY;
        } catch (IOException e) {
            LogUtils.e(e.toString());
            e.printStackTrace();
        }
    }

    public void startPlay(AssetFileDescriptor assetFileDescriptor) {
        try {
            mMediaPlayer.reset();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mMediaPlayer.setDataSource(assetFileDescriptor);
            }
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            MEDIA_STATUS_CUEERNT = MEDIA_STATUS_PLAY;
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
            MEDIA_STATUS_CUEERNT = MEDIA_STATUS_PAUSE;
            mMediaPlayer.pause();
        }
    }

    /**
     * 继续播放
     */
    public void continuePlay() {
        mMediaPlayer.start();
        MEDIA_STATUS_CUEERNT = MEDIA_STATUS_PLAY;
    }

    /**
     * 停止播放
     */
    public void stopPlay() {
        mMediaPlayer.stop();
        MEDIA_STATUS_CUEERNT = MEDIA_STATUS_STOP;
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
    public void setOnProgressListener(MediaPlayer.OnProgressListener listener) {

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
