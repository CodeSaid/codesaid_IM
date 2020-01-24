package com.codesaid.lib_framework.event;

/**
 * Created By codesaid
 * On :2020-01-16
 * Package Name: com.codesaid.lib_framework.event
 */
public class MessageEvent {

    private int type;

    private String userId;

    // 文本消息
    private String text;

    // 图片消息
    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public MessageEvent(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
