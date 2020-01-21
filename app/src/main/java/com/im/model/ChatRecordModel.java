package com.im.model;

/**
 * Created By codesaid
 * On :2020-01-18
 * Package Name: com.im.model
 * desc : 聊天回话实体
 */
public class ChatRecordModel {

    // user id
    private String userId;
    // 图像
    private String url;
    // 发送人的昵称
    private String nickName;
    // 最后一条消息内容
    private String endMsg;
    // 接受时间
    private String time;
    // 未读消息数量
    private int unReadSize;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getEndMsg() {
        return endMsg;
    }

    public void setEndMsg(String endMsg) {
        this.endMsg = endMsg;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getUnReadSize() {
        return unReadSize;
    }

    public void setUnReadSize(int unReadSize) {
        this.unReadSize = unReadSize;
    }
}
