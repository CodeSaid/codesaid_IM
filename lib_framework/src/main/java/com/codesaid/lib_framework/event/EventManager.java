package com.codesaid.lib_framework.event;

import org.greenrobot.eventbus.EventBus;

/**
 * Created By codesaid
 * On :2020-01-16
 * Package Name: com.codesaid.lib_framework.event
 * desc : EventBus 调度类
 */
public class EventManager {

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
}
