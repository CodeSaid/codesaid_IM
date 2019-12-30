package com.codesaid.lib_framework.bmob;

import cn.bmob.v3.BmobObject;

/**
 * Created By codesaid
 * On :2019-12-29
 * Package Name: com.codesaid.lib_framework.bmob
 */
public class TestData extends BmobObject {
    private String name;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
