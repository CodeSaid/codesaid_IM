package com.im.model;

/**
 * Created By codesaid
 * On :2020-02-11:16:48
 * Package Name: com.im.model
 * desc: 星球 用户
 */
public class StarModel {
    //昵称
    private String nickName;
    //ID
    private String userId;
    //头像
    private String photoUrl;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
