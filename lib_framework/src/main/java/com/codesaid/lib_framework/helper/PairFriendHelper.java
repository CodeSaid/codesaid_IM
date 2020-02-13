package com.codesaid.lib_framework.helper;

import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.bmob.IMUser;
import com.codesaid.lib_framework.utils.log.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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
                break;
            case 3:
                break;
        }
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
            if (list.get(i).getObjectId().equals(BmobManager.getInstance().getUser().getObjectId())) {
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

        final List<String> resultList = mapComperTo(soulMap, 4);

        if (resultList != null && resultList.size() > 0) {
            mDisposable = Observable.timer(DELAY_TIME, TimeUnit.SECONDS)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            // 随机数
                            int r = random.nextInt(resultList.size());
                            String userId = resultList.get(r);
                            if (mOnPairResultListener != null) {
                                mOnPairResultListener.onRandomPairListener(userId);
                            }
                        }
                    });
        }
    }

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
            if (list.get(i).getObjectId().equals(BmobManager.getInstance().getUser().getObjectId())) {
                list.remove(i);
                break;
            }
        }

        mDisposable = Observable.timer(DELAY_TIME, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
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

    public interface onPairResultListener {
        void onRandomPairListener(String userId);
    }

    /**
     * 销毁
     */
    public void disposable() {
        if (mDisposable != null) {
            if (mDisposable.isDisposed()) {
                mDisposable.dispose();
            }
        }
    }
}
