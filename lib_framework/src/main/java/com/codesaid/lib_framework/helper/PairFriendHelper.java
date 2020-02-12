package com.codesaid.lib_framework.helper;

import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.bmob.IMUser;

import java.util.List;
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

    private PairFriendHelper() {
        random = new Random();
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
                break;
            case 2:
                break;
            case 3:
                break;
        }
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
