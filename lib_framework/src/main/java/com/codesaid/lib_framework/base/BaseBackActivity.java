package com.codesaid.lib_framework.base;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import java.util.Objects;


/**
 * FileName: BaseBackActivity
 * Profile: 有返回键的基类
 */
public class BaseBackActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //显示返回键
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            //清除阴影
            getSupportActionBar().setElevation(0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
