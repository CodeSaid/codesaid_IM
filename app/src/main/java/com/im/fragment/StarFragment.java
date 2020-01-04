package com.im.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.codesaid.lib_framework.adapter.CloudTagAdapter;
import com.codesaid.lib_framework.base.BaseFragment;
import com.codesaid.lib_framework.utils.toast.ToastUtils;
import com.im.R;
import com.moxun.tagcloudlib.view.TagCloudView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created By codesaid
 * On :2020-01-02
 * Package Name: com.im.fragment
 */
public class StarFragment extends BaseFragment implements View.OnClickListener {

    private TextView tv_star_title;
    private ImageView iv_camera;
    private ImageView iv_add;

    private TagCloudView mCloudView;

    private LinearLayout ll_random;
    private LinearLayout ll_soul;
    private LinearLayout ll_fate;
    private LinearLayout ll_love;

    private List<String> mList = new ArrayList<>();

    private CloudTagAdapter mTagAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_star, null);

        initData();
        initView(view);

        return view;
    }

    private void initData() {
        for (int i = 0; i < 100; i++) {
            mList.add("Star" + i);
        }
    }

    private void initView(View view) {

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

        mCloudView.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
            @Override
            public void onItemClick(ViewGroup parent, View view, int position) {
                ToastUtils.show(getActivity(), "position: " + position);
            }
        });
    }

    @Override
    public void onClick(View view) {

    }
}
