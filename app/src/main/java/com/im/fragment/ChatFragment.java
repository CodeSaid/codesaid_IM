package com.im.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codesaid.lib_framework.base.BaseFragment;
import com.im.R;

/**
 * Created By codesaid
 * On :2020-01-02
 * Package Name: com.im.fragment
 */
public class ChatFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, null);
        return view;
    }
}
