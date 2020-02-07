package com.codesaid.lib_framework.db;

import org.litepal.crud.LitePalSupport;

/**
 * Created By codesaid
 * On :2020-02-06:20:30
 * Package Name: com.codesaid.lib_framework.db
 * desc: 通话记录
 */
public class CallRecord extends LitePalSupport {

    //媒体类型
    public static final int MEDIA_TYPE_AUDIO = 0;
    public static final int MEDIA_TYPE_VIDEO = 1;

    // 未接
    public static final int CALL_STATUS_UN_ANSWER = 0;
    // 以拨打
    public static final int CALL_STATUS_DIAL = 1;
    // 接听
    public static final int CALL_STATUS_ANSWER = 2;

    /**
     * 媒体类型
     */
    private int mediaType;

    /**
     * 通话状态
     */
    private int callStatus;

    private String userId;

    private long callTime;

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public int getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(int callStatus) {
        this.callStatus = callStatus;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getCallTime() {
        return callTime;
    }

    public void setCallTime(long callTime) {
        this.callTime = callTime;
    }
}
