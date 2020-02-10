package com.im.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Switch;

import com.codesaid.lib_framework.base.BaseBackActivity;
import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.bmob.PrivateSet;
import com.codesaid.lib_framework.utils.toast.ToastUtils;
import com.codesaid.lib_framework.view.LoadingView;
import com.im.R;

import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created By codesaid
 * On :2020-02-10:20:22
 * Package Name: com.im.ui
 * desc: 隐私设置
 */
public class PrivateSettingActivity extends BaseBackActivity implements View.OnClickListener {

    /**
     * 私有库的创建：
     * 1.创建一个BmobObject PrivateSet
     * 2.通过查询PrivateSet里面是否存在自己来判断开关
     * 3.开关的一些操作
     * 打开：则将自己添加到PrivateSet
     * 关闭：则将自己在PrivateSet中删除
     * 4.在查询联系人的时候通过查询PrivateSet过滤
     */

    private Switch sw_kill_contact;

    // 当前 id
    private String currentId = "";

    // 是否 打开隐私开关
    private boolean isCheck = false;

    private LoadingView mLoadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_set);

        initView();
    }

    private void initView() {

        mLoadingView = new LoadingView(this);

        sw_kill_contact = findViewById(R.id.sw_kill_contact);

        sw_kill_contact.setOnClickListener(this);

        queryPrivateSet();
    }

    private void queryPrivateSet() {
        BmobManager.getInstance().queryPrivateSet(new FindListener<PrivateSet>() {
            @Override
            public void done(List<PrivateSet> list, BmobException e) {
                if (e == null) {
                    if (list != null && list.size() > 0) {
                        for (int i = 0; i < list.size(); i++) {
                            PrivateSet set = list.get(i);
                            if (set.getUserId().equals(BmobManager.getInstance().getUser().getObjectId())) {

                                currentId = set.getObjectId();

                                isCheck = true;
                                break;
                            }
                        }
                        sw_kill_contact.setChecked(isCheck);
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sw_kill_contact:
                isCheck = !isCheck;
                sw_kill_contact.setChecked(isCheck);

                if (isCheck) {
                    // 选中 ---- 添加到库中
                    addPrivateSet();
                } else {
                    // 关闭 ---- 从库中 删除
                    deletePrivateSet();
                }
                break;
        }
    }

    /**
     * 允许  从 联系人中查询 我
     */
    private void deletePrivateSet() {

        mLoadingView.show(getString(R.string.text_private_set_close_ing));

        BmobManager.getInstance().deletePrivateSet(currentId, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                mLoadingView.hide();
                if (e == null) {
                    ToastUtils.show(PrivateSettingActivity.this, getString(R.string.text_private_set_fail));
                }
            }
        });
    }

    /**
     * 添加 禁止 从 联系人中查询 我
     */
    private void addPrivateSet() {

        mLoadingView.show(getString(R.string.text_private_set_open_ing));

        BmobManager.getInstance().addPrivateSet(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if (e == null) {
                    currentId = s;
                    mLoadingView.hide();
                    ToastUtils.show(PrivateSettingActivity.this, getString(R.string.text_private_set_succeess));
                }
            }
        });
    }
}
