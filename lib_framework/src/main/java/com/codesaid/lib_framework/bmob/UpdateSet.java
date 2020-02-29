package com.codesaid.lib_framework.bmob;

import cn.bmob.v3.BmobObject;

/**
 * Created By codesaid
 * On :2020-02-29 21:10
 * Package Name: com.codesaid.lib_framework.UpdateSet
 * desc: app update
 */
public class UpdateSet extends BmobObject {

    //描述
    private String desc;
    //下载地址
    private String path;
    //版本号
    private int versionCode;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }
}
