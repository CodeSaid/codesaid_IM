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

import com.codesaid.lib_framework.adapter.CloudTagAdapter;
import com.codesaid.lib_framework.base.BaseFragment;
import com.codesaid.lib_framework.utils.log.LogUtils;
import com.codesaid.lib_framework.utils.toast.ToastUtils;
import com.im.R;
import com.im.ui.AddFriendActivity;
import com.im.ui.QrCodeActivity;
import com.im.ui.UserInfoActivity;
import com.moxun.tagcloudlib.view.TagCloudView;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.ArrayList;
import java.util.List;

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
        }
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
