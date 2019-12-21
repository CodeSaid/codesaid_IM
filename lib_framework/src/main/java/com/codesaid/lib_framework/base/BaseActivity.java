package com.codesaid.lib_framework.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.codesaid.lib_framework.utils.ui.SystemUI;

/**
 * Created By codesaid
 * On :2019-12-22
 * Package Name: com.codesaid.lib_framework.base
 * desc : Activity 基类
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemUI.fixSystemUI(this);
    }
}
