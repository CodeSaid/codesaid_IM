package com.im.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.codesaid.lib_framework.base.BaseFragment;
import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.bmob.IMUser;
import com.codesaid.lib_framework.event.EventManager;
import com.codesaid.lib_framework.event.MessageEvent;
import com.codesaid.lib_framework.helper.GlideHelper;
import com.im.R;
import com.im.ui.MyInfoActivity;
import com.im.ui.NewFriendActivity;
import com.im.ui.NoticeActivity;
import com.im.ui.PrivateSettingActivity;
import com.im.ui.ShareImageActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created By codesaid
 * On :2020-01-02
 * Package Name: com.im.fragment
 * des : 个人 页面
 */
public class MeFragment extends BaseFragment implements View.OnClickListener {

    private CircleImageView iv_me_photo;
    private TextView tv_nickname;
    private LinearLayout ll_me_info;
    private LinearLayout ll_new_friend;
    private LinearLayout ll_private_set;
    private LinearLayout ll_share;
    private LinearLayout ll_setting;
    private LinearLayout ll_notice;

    private TextView tv_server_status;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me, null);

        initView(view);

        return view;
    }

    private void initView(View view) {
        iv_me_photo = view.findViewById(R.id.iv_me_photo);
        tv_nickname = view.findViewById(R.id.tv_nickname);

        ll_me_info = view.findViewById(R.id.ll_me_info);
        ll_new_friend = view.findViewById(R.id.ll_new_friend);
        ll_private_set = view.findViewById(R.id.ll_private_set);
        ll_share = view.findViewById(R.id.ll_share);
        ll_setting = view.findViewById(R.id.ll_setting);
        ll_notice = view.findViewById(R.id.ll_notice);
        tv_server_status = view.findViewById(R.id.tv_server_status);

        ll_me_info.setOnClickListener(this);
        ll_new_friend.setOnClickListener(this);
        ll_private_set.setOnClickListener(this);
        ll_share.setOnClickListener(this);
        ll_setting.setOnClickListener(this);
        ll_notice.setOnClickListener(this);

        loadMeInfo();
    }

    /**
     * 加载个人信息
     */
    private void loadMeInfo() {
        IMUser imUser = BmobManager.getInstance().getUser();
        GlideHelper.loadSmollUrl(getActivity(), imUser.getPhoto(), 100, 100, iv_me_photo);
        tv_nickname.setText(imUser.getNickName());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_me_info: // 个人信息
                startActivity(new Intent(getActivity(), MyInfoActivity.class));
                break;
            case R.id.ll_new_friend:
                //新朋友
                startActivity(new Intent(getActivity(), NewFriendActivity.class));
                break;
            case R.id.ll_share: // 分享
                startActivity(new Intent(getActivity(), ShareImageActivity.class));
                break;
            case R.id.ll_private_set: // 隐私设置
                startActivity(new Intent(getActivity(), PrivateSettingActivity.class));
                break;
            case R.id.ll_notice:
                //通知
                startActivity(new Intent(getActivity(), NoticeActivity.class));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getType()) {
            case EventManager.EVENT_REFRE_ME_INFO:
                loadMeInfo();
                break;
        }
    }
}
