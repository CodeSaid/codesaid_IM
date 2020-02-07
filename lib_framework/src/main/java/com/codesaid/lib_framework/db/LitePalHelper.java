package com.codesaid.lib_framework.db;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.util.List;

/**
 * Created By codesaid
 * On :2020-01-16
 * Package Name: com.codesaid.lib_framework.db
 */
public class LitePalHelper {

    private static volatile LitePalHelper mInstance = null;

    private LitePalHelper() {

    }

    public static LitePalHelper getInstance() {
        if (mInstance == null) {
            synchronized (LitePalHelper.class) {
                if (mInstance == null) {
                    mInstance = new LitePalHelper();
                }
            }
        }
        return mInstance;
    }

    /**
     * 保存 基类
     */
    private void baseSave(LitePalSupport support) {
        support.save();
    }

    /**
     * 保存 新的好友
     *
     * @param msg 留言
     * @param id  好友 id
     */
    public void saveNewFriend(String msg, String id) {
        NewFriend friend = new NewFriend();
        friend.setMsg(msg);
        friend.setId(id);
        friend.setIsAgree(-1);
        friend.setSaveTime(System.currentTimeMillis());
        baseSave(friend);
    }

    /**
     * 保存通话记录
     *
     * @param userId     用户 id
     * @param mediaType  媒体类型
     * @param callStatus 通话状态
     */
    public void saveCallRecord(String userId, int mediaType, int callStatus) {
        CallRecord record = new CallRecord();
        record.setUserId(userId);
        record.setMediaType(mediaType);
        record.setCallStatus(callStatus);
        record.setCallTime(System.currentTimeMillis());
        baseSave(record);
    }

    /**
     * 查询基类
     */
    private List<? extends LitePalSupport> baseQuery(Class classz) {
        return LitePal.findAll(classz);
    }

    /**
     * 查询 新的好友
     */
    public List<NewFriend> queryNewFriend() {
        return (List<NewFriend>) baseQuery(NewFriend.class);
    }

    /**
     * 查询通话记录
     *
     * @return 通话记录
     */
    public List<CallRecord> queryCallRecord() {
        return (List<CallRecord>) baseQuery(CallRecord.class);
    }

    /**
     * 更新 新的好友 数据
     *
     * @param userId 好友 id
     * @param agree  是否同意添加
     */
    public void updateNewFriend(String userId, int agree) {
        NewFriend friend = new NewFriend();
        friend.setIsAgree(agree);

        friend.updateAll("userId = ?", userId);
    }
}
