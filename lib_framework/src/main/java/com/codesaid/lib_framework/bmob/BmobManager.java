package com.codesaid.lib_framework.bmob;

import android.content.Context;

import java.io.File;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

/**
 * Created By codesaid
 * On :2019-12-29
 * Package Name: com.codesaid.lib_framework.bmob
 * desc : Bmob Manager
 */
public class BmobManager {

    private static final String APP_KEY = "06f10a2dabbb5e3c84121c048e69dd98";

    private volatile static BmobManager mInstance = null;

    private BmobManager() {

    }

    public static BmobManager getInstance() {
        if (mInstance == null) {
            synchronized (BmobManager.class) {
                if (mInstance == null) {
                    mInstance = new BmobManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * init Bmob
     */
    public void init(Context context) {
        Bmob.initialize(context, APP_KEY);
    }

    /**
     * 获取当前登录的 User
     *
     * @return User
     */
    public IMUser getUser() {
        return BmobUser.getCurrentUser(IMUser.class);
    }

    /**
     * 发送短信验证码
     *
     * @param phone    手机号码
     * @param listener listener
     */
    public void requestSMS(String phone, QueryListener<Integer> listener) {
        BmobSMS.requestSMSCode(phone, "", listener);
    }

    /**
     * 通过手机短信验证码注册 or 登录
     *
     * @param phone    手机号
     * @param code     验证码
     * @param listener listener
     */
    public void signOrLoginByMobilePhone(String phone, String code, LogInListener<IMUser> listener) {
        BmobUser.signOrLoginByMobilePhone(phone, code, listener);
    }

    /**
     * 判断是否登录过
     */
    public boolean isLogin() {
        return BmobUser.isLogin();
    }

    /**
     * 上传头像
     *
     * @param name     名称
     * @param file     头像
     * @param listener listener
     */
    public void uploadFirstPhoto(final String name, File file, final onUploadPhotoListener listener) {
        /**
         * 上传文件   拿到地址
         * 更新用户 信息
         */

        final IMUser user = getUser();
        final BmobFile bmobFile = new BmobFile(file);
        bmobFile.uploadblock(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    // 上传成功
                    user.setNickName(name);
                    user.setPhoto(bmobFile.getFileUrl());

                    user.setTokenNickName(name);
                    user.setTokenPhoto(bmobFile.getFileUrl());

                    // 更新用户信息
                    user.update(new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e == null) {
                                listener.onUploadSuccess();
                            } else {
                                listener.onUploadFail(e);
                            }
                        }
                    });
                } else {
                    listener.onUploadFail(e);
                }
            }
        });
    }

    /**
     * 查询基类
     *
     * @param key
     * @param values
     * @param listener
     */
    public void baseQuery(String key, String values, FindListener<IMUser> listener) {
        BmobQuery<IMUser> query = new BmobQuery<>();
        query.addWhereEqualTo(key, values);
        query.findObjects(listener);
    }

    /**
     * 根据电话号码查询用户
     *
     * @param phone    电话号码
     * @param listener listener
     */
    public void queryPhoneUser(String phone, FindListener<IMUser> listener) {
        baseQuery("mobilePhoneNumber", phone, listener);
    }

    /**
     * 根据 user id 查询用户
     *
     * @param objectId user id
     * @param listener listener
     */
    public void queryObjectIdUser(String objectId, FindListener<IMUser> listener) {
        baseQuery("objectId", objectId, listener);
    }

    /**
     * 查询好友
     *
     * @param listener listener
     */
    public void queryFriends(FindListener<Friend> listener) {
        BmobQuery<Friend> query = new BmobQuery<>();
        query.addWhereEqualTo("user", getUser());
        query.findObjects(listener);
    }

    /**
     * 查询所有好友
     *
     * @param listener listener
     */
    public void queryAllUser(FindListener<IMUser> listener) {
        BmobQuery<IMUser> query = new BmobQuery<>();
        query.findObjects(listener);
    }

    /**
     * 查询 私有库
     *
     * @param listener listener
     */
    public void queryPrivateSet(FindListener<PrivateSet> listener) {
        BmobQuery<PrivateSet> query = new BmobQuery<>();
        query.findObjects(listener);
    }

    /**
     * 查询 缘分池
     *
     * @param listener listener
     */
    public void queryFateSet(FindListener<FateUser> listener) {
        BmobQuery<FateUser> query = new BmobQuery<>();
        query.findObjects(listener);
    }

    /**
     * 查询 圈子
     *
     * @param listener listener
     */
    public void querySquareSet(FindListener<SquareSet> listener) {
        BmobQuery<SquareSet> query = new BmobQuery<>();
        query.findObjects(listener);
    }

    /**
     * 添加好友
     *
     * @param user     user
     * @param listener listener
     */
    public void addFriend(IMUser user, SaveListener<String> listener) {
        Friend friend = new Friend();
        friend.setUser(getUser());
        friend.setFriendUser(user);
        friend.save(listener);
    }

    /**
     * 添加好友
     *
     * @param userId   user id
     * @param listener listener
     */
    public void addFriend(String userId, final SaveListener<String> listener) {
        queryObjectIdUser(userId, new FindListener<IMUser>() {
            @Override
            public void done(List<IMUser> list, BmobException e) {
                if (e == null) {
                    if (list != null && list.size() > 0) {
                        addFriend(list.get(0), listener);
                    }
                }
            }
        });
    }

    /**
     * 添加 私有库
     *
     * @param listener listener
     */
    public void addPrivateSet(SaveListener<String> listener) {
        PrivateSet set = new PrivateSet();
        set.setUserId(getUser().getObjectId());
        set.setPhone(getUser().getMobilePhoneNumber());
        set.save(listener);
    }

    public void deletePrivateSet(String userId, UpdateListener listener) {
        PrivateSet set = new PrivateSet();
        set.setObjectId(userId);

        set.delete(listener);
    }

    /**
     * 添加 自己到 缘分池中
     *
     * @param listener listener
     */
    public void addFateUser(SaveListener<String> listener) {
        FateUser user = new FateUser();
        user.setUserId(getUser().getObjectId());
        user.save(listener);
    }

    /**
     * 从 缘分池 中 删除 用户
     *
     * @param userId   需要删除的用户 id
     * @param listener listener
     */
    public void deleteFateUser(String userId, UpdateListener listener) {
        FateUser user = new FateUser();
        user.setObjectId(userId);
        user.delete(listener);
    }

    /**
     * 发布广场
     *
     * @param mediaType 媒体类型
     * @param text      文本
     * @param path      路径
     */
    public void pushSquare(int mediaType, String text, String path, SaveListener<String> listener) {
        SquareSet squareSet = new SquareSet();
        squareSet.setPush_type(mediaType);
        squareSet.setUserId(getUser().getObjectId());
        squareSet.setPushTime(System.currentTimeMillis());
        squareSet.setText(text);
        squareSet.setMediaUrl(path);
        squareSet.save(listener);
    }

    public interface onUploadPhotoListener {

        void onUploadSuccess();

        void onUploadFail(BmobException e);
    }
}
