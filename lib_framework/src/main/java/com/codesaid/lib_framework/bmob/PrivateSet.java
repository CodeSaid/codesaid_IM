package com.codesaid.lib_framework.bmob;

import cn.bmob.v3.BmobObject;

/**
 * Created By codesaid
 * On :2020-02-10:20:27
 * Package Name: com.codesaid.lib_framework.bmob
 * desc: 用户隐私 私有库
 */
public class PrivateSet extends BmobObject {

    // 用户 id
    private String userId;

    // 用户 电话
    private String phone;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
