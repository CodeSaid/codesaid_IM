package com.codesaid.lib_framework.bmob;

import cn.bmob.v3.BmobObject;

/**
 * Created By codesaid
 * On :2020-01-13
 * Package Name: com.codesaid.lib_framework.bmob
 * desc : 朋友模型
 */
public class Friend extends BmobObject {
    // 我 自己
    private IMUser user;

    // 好友
    private IMUser friendUser;

    public IMUser getUser() {
        return user;
    }

    public void setUser(IMUser user) {
        this.user = user;
    }

    public IMUser getFriendUser() {
        return friendUser;
    }

    public void setFriendUser(IMUser friendUser) {
        this.friendUser = friendUser;
    }
}
