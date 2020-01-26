package com.im.fragment.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.codesaid.lib_framework.base.BaseFragment;
import com.im.R;

/**
 * Created By codesaid
 * On :2020-01-18
 * Package Name: com.im.fragment.chat
 * desc : call 聊天记录
 */
public class CallRecordFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_record, null);
        initView(view);
        return view;
    }

    private void initView(View view) {

    }
}
