package com.codesaid.lib_framework.event;

/**
 * Created By codesaid
 * On :2020-01-16
 * Package Name: com.codesaid.lib_framework.event
 */
public class MessageEvent {

    private int type;

    public MessageEvent(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
