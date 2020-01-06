package com.im;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.codesaid.lib_framework.base.BaseUIActivity;
import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.entity.Constants;
import com.codesaid.lib_framework.utils.sp.SpUtils;
import com.codesaid.lib_framework.view.DialogManager;
import com.codesaid.lib_framework.view.DialogView;
import com.im.fragment.ChatFragment;
import com.im.fragment.MeFragment;
import com.im.fragment.SquareFragment;
import com.im.fragment.StarFragment;
import com.im.service.CloudService;
import com.im.ui.FirstUploadActivity;

import java.util.List;

/**
 * @author codesaid
 */
public class MainActivity extends BaseUIActivity implements View.OnClickListener {

    //星球
    private ImageView iv_star;
    private TextView tv_star;
    private LinearLayout ll_star;
    private StarFragment mStarFragment = null;
    private FragmentTransaction mStarTransaction = null;

    //广场
    private ImageView iv_square;
    private TextView tv_square;
    private LinearLayout ll_square;
    private SquareFragment mSquareFragment = null;
    private FragmentTransaction mSquareTransaction = null;

    //聊天
    private ImageView iv_chat;
    private TextView tv_chat;
    private LinearLayout ll_chat;
    private ChatFragment mChatFragment = null;
    private FragmentTransaction mChatTransaction = null;

    //我的
    private ImageView iv_me;
    private TextView tv_me;
    private LinearLayout ll_me;
    private MeFragment mMeFragment = null;
    private FragmentTransaction mMeTransaction = null;
    private DialogView mUploadView;

    // 跳转到上传头像页面的回调 code
    public static int UPLOAD_CODE = 1002;

    /**
     * 1.初始化 Fragment
     * 2.显示Fragment
     * 3.隐藏所有的Fragment
     * 4.恢复Fragment
     * 优化的手段
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();

        initView();
    }

    /**
     * init view
     */
    private void initView() {
        iv_star = findViewById(R.id.iv_star);
        tv_star = findViewById(R.id.tv_star);
        ll_star = findViewById(R.id.ll_star);

        iv_square = findViewById(R.id.iv_square);
        tv_square = findViewById(R.id.tv_square);
        ll_square = findViewById(R.id.ll_square);

        iv_chat = findViewById(R.id.iv_chat);
        tv_chat = findViewById(R.id.tv_chat);
        ll_chat = findViewById(R.id.ll_chat);

        iv_me = findViewById(R.id.iv_me);
        tv_me = findViewById(R.id.tv_me);
        ll_me = findViewById(R.id.ll_me);

        ll_star.setOnClickListener(this);
        ll_square.setOnClickListener(this);
        ll_chat.setOnClickListener(this);
        ll_me.setOnClickListener(this);

        // 设置文本
        tv_star.setText(R.string.text_main_star);
        tv_square.setText(R.string.text_main_square);
        tv_chat.setText(R.string.text_main_chat);
        tv_me.setText(R.string.text_main_me);

        initFragment();

        // 切换默认选项卡
        checkMainTab(0);

        // check Token
        checkToken();
    }

    private void checkToken() {
        // 获取 Token     需要三个 参数 1.用户ID  2.头像地址   3.昵称
        String token = SpUtils.getInstance().getString(Constants.SP_TOKEN, "");
        if (!TextUtils.isEmpty(token)) {
            // 启动云服务 去连接 融云服务器
            startService(new Intent(MainActivity.this, CloudService.class));
        } else {
            // 有 三个参数
            String tokenPhoto = BmobManager.getInstance().getUser().getTokenPhoto();
            String tokenName = BmobManager.getInstance().getUser().getTokenNickName();
            if (!TextUtils.isEmpty(tokenPhoto) && !TextUtils.isEmpty(tokenName)) {
                // 创建 Token
                createToken();
            } else {
                createUploadDialog();
            }
        }
    }

    /**
     * 创建上传提示框
     */
    private void createUploadDialog() {
        mUploadView = DialogManager.getInstance()
                .initView(this, R.layout.dialog_first_upload);
        // 设置外部不能点击
        mUploadView.setCancelable(false);
        ImageView ivUpload = mUploadView.findViewById(R.id.iv_go_upload);
        ivUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogManager.getInstance().hide(mUploadView);
                FirstUploadActivity.startActivity(MainActivity.this, UPLOAD_CODE);

            }
        });
        DialogManager.getInstance().show(mUploadView);
    }

    /**
     * 创建 Token
     */
    private void createToken() {

    }

    /**
     * init fragment
     */
    private void initFragment() {

        // 星球
        if (mStarFragment == null) {
            mStarFragment = new StarFragment();
            mStarTransaction = getSupportFragmentManager().beginTransaction();
            mStarTransaction.add(R.id.mMainLayout, mStarFragment);
            mStarTransaction.commit();
        }

        // 聊天
        if (mChatFragment == null) {
            mChatFragment = new ChatFragment();
            mChatTransaction = getSupportFragmentManager().beginTransaction();
            mChatTransaction.add(R.id.mMainLayout, mChatFragment);
            mChatTransaction.commit();
        }

        // 广场
        if (mSquareFragment == null) {
            mSquareFragment = new SquareFragment();
            mSquareTransaction = getSupportFragmentManager().beginTransaction();
            mSquareTransaction.add(R.id.mMainLayout, mSquareFragment);
            mSquareTransaction.commit();
        }

        // 个人
        if (mMeFragment == null) {
            mMeFragment = new MeFragment();
            mMeTransaction = getSupportFragmentManager().beginTransaction();
            mMeTransaction.add(R.id.mMainLayout, mMeFragment);
            mMeTransaction.commit();
        }
    }

    /**
     * show fragment
     *
     * @param fragment fragment
     */
    private void showFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            hideAllFragment(transaction);
            transaction.show(fragment);
            transaction.commitAllowingStateLoss();
        }
    }

    /**
     * 隐藏所有的 Fragment
     *
     * @param transaction transaction
     */
    private void hideAllFragment(FragmentTransaction transaction) {
        if (mStarFragment != null) {
            transaction.hide(mStarFragment);
        }

        if (mSquareFragment != null) {
            transaction.hide(mSquareFragment);
        }

        if (mChatFragment != null) {
            transaction.hide(mChatFragment);
        }

        if (mMeFragment != null) {
            transaction.hide(mMeFragment);
        }
    }

    /**
     * 切换主页选项卡
     *
     * @param index 0:星球
     *              1:广场
     *              2:聊天
     *              3:我的
     */
    private void checkMainTab(int index) {
        switch (index) {
            case 0:
                showFragment(mStarFragment);
                iv_star.setImageResource(R.drawable.img_star_p);
                iv_square.setImageResource(R.drawable.img_square);
                iv_chat.setImageResource(R.drawable.img_chat);
                iv_me.setImageResource(R.drawable.img_me);

                tv_star.setTextColor(getResources().getColor(R.color.colorAccent));
                tv_square.setTextColor(Color.BLACK);
                tv_chat.setTextColor(Color.BLACK);
                tv_me.setTextColor(Color.BLACK);

                break;
            case 1:
                showFragment(mSquareFragment);
                iv_star.setImageResource(R.drawable.img_star);
                iv_square.setImageResource(R.drawable.img_square_p);
                iv_chat.setImageResource(R.drawable.img_chat);
                iv_me.setImageResource(R.drawable.img_me);

                tv_star.setTextColor(Color.BLACK);
                tv_square.setTextColor(getResources().getColor(R.color.colorAccent));
                tv_chat.setTextColor(Color.BLACK);
                tv_me.setTextColor(Color.BLACK);
                break;
            case 2:
                showFragment(mChatFragment);
                iv_star.setImageResource(R.drawable.img_star);
                iv_square.setImageResource(R.drawable.img_square);
                iv_chat.setImageResource(R.drawable.img_chat_p);
                iv_me.setImageResource(R.drawable.img_me);

                tv_star.setTextColor(Color.BLACK);
                tv_square.setTextColor(Color.BLACK);
                tv_chat.setTextColor(getResources().getColor(R.color.colorAccent));
                tv_me.setTextColor(Color.BLACK);
                break;
            case 3:
                showFragment(mMeFragment);

                iv_star.setImageResource(R.drawable.img_star);
                iv_square.setImageResource(R.drawable.img_square);
                iv_chat.setImageResource(R.drawable.img_chat);
                iv_me.setImageResource(R.drawable.img_me_p);

                tv_star.setTextColor(Color.BLACK);
                tv_square.setTextColor(Color.BLACK);
                tv_chat.setTextColor(Color.BLACK);
                tv_me.setTextColor(getResources().getColor(R.color.colorAccent));
                break;
        }
    }

    /**
     * 请求 APP 所需权限
     */
    private void requestPermission() {
        request(new OnPermissionsResult() {
            @Override
            public void OnSuccess() {

            }

            @Override
            public void OnFail(List<String> noPermissions) {

            }
        });
    }

    /**
     * 防止重叠
     * 当应用的内存紧张的时候，系统会回收掉Fragment对象
     * 再一次进入的时候会重新创建Fragment
     * 非原来对象，我们无法控制，导致重叠
     */
    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        if (mStarFragment != null && fragment instanceof StarFragment) {
            mStarFragment = (StarFragment) fragment;
        }
        if (mSquareFragment != null && fragment instanceof SquareFragment) {
            mSquareFragment = (SquareFragment) fragment;
        }
        if (mChatFragment != null && fragment instanceof ChatFragment) {
            mChatFragment = (ChatFragment) fragment;
        }
        if (mMeFragment != null && fragment instanceof MeFragment) {
            mMeFragment = (MeFragment) fragment;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_star:
                checkMainTab(0);
                break;
            case R.id.ll_square:
                checkMainTab(1);
                break;
            case R.id.ll_chat:
                checkMainTab(2);
                break;
            case R.id.ll_me:
                checkMainTab(3);
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == UPLOAD_CODE) {
                // 上传头像成功
                checkToken();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
