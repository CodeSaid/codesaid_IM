package com.im;

import android.os.Bundle;

import com.codesaid.lib_framework.base.BaseUIActivity;
import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.bmob.IMUser;
import com.codesaid.lib_framework.utils.toast.ToastUtils;

import java.util.List;

/**
 * @author codesaid
 */
public class MainActivity extends BaseUIActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IMUser user = BmobManager.getInstance().getUser();
        ToastUtils.show(this, user.getMobilePhoneNumber());

        requestPermission();
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
}
