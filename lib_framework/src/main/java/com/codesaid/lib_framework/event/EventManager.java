package com.codesaid.lib_framework.event;

import org.greenrobot.eventbus.EventBus;

/**
 * Created By codesaid
 * On :2020-01-16
 * Package Name: com.codesaid.lib_framework.event
 * desc : EventBus 调度类
 */
public class EventManager {

    // 更新好友列表
    public static final int FLAG_UPDATE_FRIEND = 1000;

    // 发送文本数据
    public static final int FLAG_SEND_TEXT = 1001;
    // 发送图片数据
    public static final int FLAG_SEND_IMAGE = 1002;

    /**
     * 注册
     *
     * @param subscriber
     */
    public static void register(Object subscriber) {
        EventBus.getDefault().register(subscriber);
    }

    /**
     * 反注册
     *
     * @param subscriber
     */
    public static void unregister(Object subscriber) {
        EventBus.getDefault().unregister(subscriber);
    }

    public static void post(int type) {
        EventBus.getDefault().post(new MessageEvent(type));
    }

    public static void post(MessageEvent event) {
        EventBus.getDefault().post(event);
    }
}
