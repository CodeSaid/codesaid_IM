package com.codesaid.lib_framework.helper;

import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.bmob.FateUser;
import com.codesaid.lib_framework.bmob.IMUser;
import com.codesaid.lib_framework.utils.log.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created By codesaid
 * On :2020-02-11:17:15
 * Package Name: com.codesaid.lib_framework.helper
 * desc: 好友匹配帮助类
 */
public class PairFriendHelper {
    private static volatile PairFriendHelper mInstance;

    // 延迟时间
    private static final int DELAY_TIME = 2;

    private onPairResultListener mOnPairResultListener;

    // 缘分池 查询 轮询 次数
    private int fateNum = 0;

    // 随机类
    private Random random;
    private Disposable mDisposable;

    private IMUser myUser;

    private PairFriendHelper() {
        random = new Random();
        myUser = BmobManager.getInstance().getUser();
    }

    public static PairFriendHelper getInstance() {
        if (mInstance == null) {
            synchronized (PairFriendHelper.class) {
                if (mInstance == null) {
                    mInstance = new PairFriendHelper();
                }
            }
        }
        return mInstance;
    }

    public void setOnPairResultListener(onPairResultListener onPairResultListener) {
        mOnPairResultListener = onPairResultListener;
    }

    /**
     * @param index 0：从用户组随机抽取一位好友
     *              1：深度匹配：资料的相似度
     *              2：缘分 同一时刻搜索的
     *              3：年龄相似的异性
     */
    public void pairUser(int index, List<IMUser> list) {
        switch (index) {
            case 0:
                randomUser(list);
                break;
            case 1:
                soulUser(list);
                break;
            case 2:
                fateUser();
                break;
            case 3:
                loveUser(list);
                break;
        }
    }

    /**
     * 恋爱匹配
     *
     * @param list list
     */
    private void loveUser(List<IMUser> list) {
        /**
         * 1.抽取所有的用户
         * 2.根据性别抽取出异性
         * 3.根据年龄再抽取
         * 4.可以有一些附加条件：爱好 星座 ~~
         * 5.计算出来
         */

        List<IMUser> loveLists = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            // 过滤自己
            if (myUser.getObjectId().equals(list.get(i).getObjectId())) {
                //跳过本次循环
                continue;
            }

            // 判断是否是异性
            if (myUser.isSex() != list.get(i).isSex()) {
                loveLists.add(list.get(i));
            }

        }

        // 判断是否有数据
        if (loveLists.size() > 0) {
            final List<String> loveIdLists = new ArrayList<>();
            for (int j = 0; j < loveLists.size(); j++) {
                // 计算 年龄是否相差 5岁内
                IMUser user = loveLists.get(j);
                if (Math.abs(user.getAge() - myUser.getAge()) <= 5) {
                    loveIdLists.add(user.getObjectId());
                }
            }

            if (loveIdLists.size() > 0) {
                //在这里增加更多的判断条件
                rxJavaPairUser(new onRxJavaPairUserListener() {
                    @Override
                    public void rxJavaResult() {
                        int r = random.nextInt(loveIdLists.size());
                        mOnPairResultListener.onRandomPairListener(loveIdLists.get(r));
                    }
                });
            } else {
                mOnPairResultListener.OnPairFailListener();
            }
        } else {
            mOnPairResultListener.OnPairFailListener();
        }
    }

    /**
     * 缘分匹配
     */
    private void fateUser() {
        /**
         * 1.创建库
         * 2.将自己添加进去
         * 3.轮询查找好友
         * 4.15s
         * 5.查询到了之后则反馈给外部
         * 6.将自己删除
         */

        BmobManager.getInstance().addFateUser(new SaveListener<String>() {
            @Override
            public void done(final String s, BmobException e) {
                if (e == null) {
                    mDisposable = Observable.interval(1, TimeUnit.SECONDS)
                            .subscribe(new Consumer<Long>() {
                                @Override
                                public void accept(Long aLong) throws Exception {
                                    queryFateSet(s);
                                }
                            });
                }
            }
        });
    }

    /**
     * 根据 用户 id 查询 缘分池
     *
     * @param userId user id
     */
    private void queryFateSet(final String userId) {
        BmobManager.getInstance().queryFateSet(new FindListener<FateUser>() {
            @Override
            public void done(List<FateUser> list, BmobException e) {
                fateNum++;
                if (e == null) {
                    if (list != null && list.size() > 0) {
                        if (list.size() > 1) {// 说明有人在匹配
                            disposable();
                            // 过滤自己
                            for (int i = 0; i < list.size(); i++) {
                                FateUser fateUser = list.get(i);
                                if (fateUser.getUserId().equals(myUser.getObjectId())) {
                                    list.remove(i);
                                    break;
                                }

                                // 最终的结果 抛出
                                int r = random.nextInt(list.size());
                                mOnPairResultListener.onRandomPairListener(list.get(r).getUserId());

                                // 删除 自己
                                deleteFateUser(userId);
                                fateNum = 0;
                            }
                        } else {
                            LogUtils.i("fateNum: " + fateNum);
                            if (fateNum > -15) {
                                disposable();
                                // 超时
                                deleteFateUser(userId);
                                mOnPairResultListener.OnPairFailListener();
                                fateNum = 0;
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * 根据 id 从 缘分池 中删除 用户
     *
     * @param userId user id
     */
    private void deleteFateUser(String userId) {
        BmobManager.getInstance().deleteFateUser(userId, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    LogUtils.i("delete FateUser success");
                }
            }
        });
    }

    /**
     * 灵魂匹配
     */
    @SuppressWarnings("ConstantConditions")
    private void soulUser(List<IMUser> list) {
        Map<String, Integer> soulMap = new HashMap<>();
        // 四要素：星座 年龄 爱好 状态
        for (int i = 0; i < list.size(); i++) {

            IMUser user = list.get(i);

            //过滤自己
            if (list.get(i).getObjectId().equals(myUser.getObjectId())) {
                // 跳过本次循环
                continue;
            }

            //匹配星座
            if (user.getConstellation().equals(myUser.getConstellation())) {
                soulMap.put(user.getObjectId(), 1);
            }

            //匹配年龄
            if (user.getAge() == myUser.getAge()) {
                if (soulMap.containsKey(user.getObjectId())) {
                    soulMap.put(user.getObjectId(), soulMap.get(user.getObjectId()) + 1);
                } else {
                    soulMap.put(user.getObjectId(), 1);
                }
            }

            //匹配爱好
            if (user.getHobby().equals(myUser.getHobby())) {
                if (soulMap.containsKey(user.getObjectId())) {
                    soulMap.put(user.getObjectId(), soulMap.get(user.getObjectId()) + 1);
                } else {
                    soulMap.put(user.getObjectId(), 1);
                }
            }

            //单身状态
            if (soulMap.containsKey(user.getObjectId())) {
                if (soulMap.get(user.getObjectId()) != null) {
                    soulMap.put(user.getObjectId(), soulMap.get(user.getObjectId()) + 1);
                } else {
                    soulMap.put(user.getObjectId(), 1);
                }
            }
        }

        LogUtils.i("soulMap: " + soulMap.toString());

        // 获取最佳的对象
        final List<String> resultList = mapComperTo(soulMap, 4);

        if (resultList != null && resultList.size() > 0) {

            rxJavaPairUser(new onRxJavaPairUserListener() {
                @Override
                public void rxJavaResult() {
                    // 随机数
                    int r = random.nextInt(resultList.size());
                    String userId = resultList.get(r);
                    if (mOnPairResultListener != null) {
                        mOnPairResultListener.onRandomPairListener(userId);
                    }
                }
            });
        } else {
            mOnPairResultListener.OnPairFailListener();
        }
    }

    /**
     * 比较 集合中的对象，获取最佳的对象
     *
     * @param map  map
     * @param size size
     * @return list
     */
    private List<String> mapComperTo(Map<String, Integer> map, int size) {
        List<String> resultList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            String key = entry.getKey();
            int value = entry.getValue();
            if (size == value) {
                resultList.add(key);
            }
        }

        if (resultList.size() == 0) {
            size--;
            if (size == 0) {
                return null;
            }
            mapComperTo(map, size);
        }
        return resultList;
    }

    /**
     * 随机匹配
     */
    private void randomUser(final List<IMUser> list) {
        /**
         * 1.获取到全部的用户组
         * 2.过滤自己
         * 3.开始随机
         * 4.根据随机的数值拿到对应的对象ID
         * 5.接口回传
         */
        for (int i = 0; i < list.size(); i++) {
            //过滤自己
            if (list.get(i).getObjectId().equals(myUser.getObjectId())) {
                list.remove(i);
                break;
            }
        }

        rxJavaPairUser(new onRxJavaPairUserListener() {
            @Override
            public void rxJavaResult() {
                // 随机数
                int r = random.nextInt(list.size());
                IMUser user = list.get(r);
                if (user != null) {
                    if (mOnPairResultListener != null) {
                        mOnPairResultListener.onRandomPairListener(user.getObjectId());
                    }
                }
            }
        });
    }

    private void rxJavaPairUser(final onRxJavaPairUserListener listener) {
        mDisposable = Observable.timer(DELAY_TIME, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        listener.rxJavaResult();
                    }
                });
    }

    public interface onRxJavaPairUserListener {
        void rxJavaResult();
    }

    public interface onPairResultListener {
        /**
         * 匹配成功
         *
         * @param userId 用户 id
         */
        void onRandomPairListener(String userId);

        /**
         * 匹配失败
         */
        void OnPairFailListener();
    }

    /**
     * 销毁
     */
    public void disposable() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }
}
