package com.codesaid.lib_framework.bmob;

import cn.bmob.v3.BmobObject;

/**
 * Created By codesaid
 * On :2020-02-16:19:20
 * Package Name: com.codesaid.lib_framework.bmob
 * desc: 圈子 数据库
 */
public class SquareSet extends BmobObject {

    //文本
    public static final int PUSH_TEXT = 0;
    //图片
    public static final int PUSH_IMAGE = 1;
    //音乐
    public static final int PUSH_MUSIC = 2;
    //视频
    public static final int PUSH_VIDEO = 3;

    /**
     * 发送类型
     */
    private int push_type;

    /**
     * 发布者的 id
     */
    private String userId;

    /**
     * 发布的时间
     */
    private long pushTime;

    /**
     * 文字
     */
    private String text;

    /**
     * 图片 音乐 视频 url
     */
    private String mediaUrl;

    public int getPush_type() {
        return push_type;
    }

    public void setPush_type(int push_type) {
        this.push_type = push_type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getPushTime() {
        return pushTime;
    }

    public void setPushTime(long pushTime) {
        this.pushTime = pushTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }
}
