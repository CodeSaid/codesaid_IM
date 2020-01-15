package com.codesaid.lib_framework.db;

import org.litepal.crud.LitePalSupport;

/**
 * Created By codesaid
 * On :2020-01-16
 * Package Name: com.codesaid.lib_framework.db
 * desc : 新的好友
 */
public class NewFriend extends LitePalSupport {
    //留言
    private String msg;
    //对方id
    private String userId;
    //时间
    private long saveTime;
    //状态 -1：待确认 0：同意 1：拒绝
    private int isAgree = -1;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getId() {
        return userId;
    }

    public void setId(String id) {
        this.userId = id;
    }

    public long getSaveTime() {
        return saveTime;
    }

    public void setSaveTime(long saveTime) {
        this.saveTime = saveTime;
    }

    public int getIsAgree() {
        return isAgree;
    }

    public void setIsAgree(int isAgree) {
        this.isAgree = isAgree;
    }
}
