package com.im.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.codesaid.lib_framework.view.DialogManager;
import com.codesaid.lib_framework.view.LoadingView;
import com.im.adapter.CloudTagAdapter;
import com.codesaid.lib_framework.base.BaseFragment;
import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.bmob.IMUser;
import com.codesaid.lib_framework.utils.log.LogUtils;
import com.codesaid.lib_framework.utils.toast.ToastUtils;
import com.im.R;
import com.im.model.StarModel;
import com.im.ui.AddFriendActivity;
import com.im.ui.QrCodeActivity;
import com.im.ui.UserInfoActivity;
import com.moxun.tagcloudlib.view.TagCloudView;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * Created By codesaid
 * On :2020-01-02
 * Package Name: com.im.fragment
 */
public class StarFragment extends BaseFragment implements View.OnClickListener {

    //二维码结果
    private static final int REQUEST_CODE = 1235;

    private TextView tv_star_title;
    private ImageView iv_camera;
    private ImageView iv_add;

    private TagCloudView mCloudView;

    private LinearLayout ll_random;
    private LinearLayout ll_soul;
    private LinearLayout ll_fate;
    private LinearLayout ll_love;

    private List<StarModel> mList = new ArrayList<>();

    private CloudTagAdapter mTagAdapter;

    private LoadingView mLoadingView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_star, null);

        initView(view);

        return view;
    }

    private void initView(View view) {
        mLoadingView = new LoadingView(getActivity());

        ll_random = view.findViewById(R.id.ll_random);
        ll_soul = view.findViewById(R.id.ll_soul);
        ll_fate = view.findViewById(R.id.ll_fate);
        ll_love = view.findViewById(R.id.ll_love);

        mCloudView = view.findViewById(R.id.mCloudView);

        iv_camera = view.findViewById(R.id.iv_camera);
        iv_add = view.findViewById(R.id.iv_add);

        ll_random.setOnClickListener(this);
        ll_soul.setOnClickListener(this);
        ll_fate.setOnClickListener(this);
        ll_love.setOnClickListener(this);

        iv_camera.setOnClickListener(this);
        iv_add.setOnClickListener(this);

        mTagAdapter = new CloudTagAdapter(getActivity(), mList);
        mCloudView.setAdapter(mTagAdapter);

        // 监听 点击事件
        mCloudView.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
            @Override
            public void onItemClick(ViewGroup parent, View view, int position) {
                //ToastUtils.show(getActivity(), "position: " + position);
                // 跳转到 用户 信息 页面
                UserInfoActivity.startActivity(getActivity(), mList.get(position).getUserId());
            }
        });

        loadStarUser();
    }

    /**
     * 加载星球 用户
     */
    private void loadStarUser() {
        BmobManager.getInstance().queryAllUser(new FindListener<IMUser>() {
            @Override
            public void done(List<IMUser> list, BmobException e) {
                if (e == null) {
                    if (list != null && list.size() > 0) {

                        int index = 100;

                        // 如果数据超过 100 则只取100个人
                        if (list.size() <= 100) {
                            index = list.size();
                        }

                        for (int i = 0; i < index; i++) {
                            IMUser user = list.get(i);
                            saveStarUser(user.getObjectId(), user.getNickName(), user.getPhoto());
                        }
                        mTagAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    /**
     * 保存 用户信息 到 list
     *
     * @param userId   userId
     * @param name     name
     * @param photoUrl photoUrl
     */
    private void saveStarUser(String userId, String name, String photoUrl) {
        StarModel model = new StarModel();
        model.setUserId(userId);
        model.setNickName(name);
        model.setPhotoUrl(photoUrl);

        mList.add(model);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_add:
                // 添加好友
                startActivity(new Intent(getActivity(), AddFriendActivity.class));
                break;
            case R.id.iv_camera:
                //扫描
                Intent intent = new Intent(getActivity(), QrCodeActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            case R.id.ll_random:
                //随机匹配
                pairUser(0);
                break;
            case R.id.ll_soul:
                //灵魂匹配
                pairUser(1);
                break;
            case R.id.ll_fate:
                //缘分匹配

                break;
            case R.id.ll_love:
                //恋爱匹配

                break;
        }
    }

    /**
     * 匹配规则
     *
     * @param index 0:随机匹配
     *              1:灵魂匹配
     *              2:缘分匹配
     *              3:恋爱匹配
     */
    private void pairUser(int index) {
        switch (index) {
            case 0:
                mLoadingView.show(getString(R.string.text_pair_random));
                break;
            case 1:
                mLoadingView.show(getString(R.string.text_pair_soul));
                break;
            case 2:
                mLoadingView.show(getString(R.string.text_pair_fate));
                break;
            case 3:
                mLoadingView.show(getString(R.string.text_pair_love));
                break;
        }

        //计算
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        /**
         * 处理二维码扫描结果
         */
        if (requestCode == REQUEST_CODE) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    //解析结果
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    LogUtils.i("qrcode result: " + result);

                    if (!TextUtils.isEmpty(result)) {
                        // 判断是否是 我们自己的二维码
                        if (result.startsWith("codesaid_IM")) {
                            String s = result.split("#")[1];

                            UserInfoActivity.startActivity(getActivity(), s);
                        } else {
                            ToastUtils.show(getActivity(), "请使用正确的二维码");
                        }
                    }
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    //解析二维码失败
                    ToastUtils.show(getActivity(), getString(R.string.text_qrcode_fail));
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
