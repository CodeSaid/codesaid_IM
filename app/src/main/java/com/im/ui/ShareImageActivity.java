package com.im.ui;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.codesaid.lib_framework.base.BaseBackActivity;
import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.bmob.IMUser;
import com.codesaid.lib_framework.helper.GlideHelper;
import com.im.R;
import com.uuzuche.lib_zxing.activity.CodeUtils;

/**
 * Created By codesaid
 * On :2020-02-09:19:13
 * Package Name: com.im.ui
 * desc: 图片分享
 */
public class ShareImageActivity extends BaseBackActivity implements View.OnClickListener {

    //头像
    private ImageView iv_photo;
    //昵称
    private TextView tv_name;
    //性别
    private TextView tv_sex;
    //年龄
    private TextView tv_age;
    //电话
    private TextView tv_phone;
    //简介
    private TextView tv_desc;
    //二维码
    private ImageView iv_qrcode;
    //根布局
    private LinearLayout ll_content;
    //下载
    private LinearLayout ll_download;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_share_img);

        initView();
    }

    private void initView() {

        iv_photo = findViewById(R.id.iv_photo);
        tv_name = findViewById(R.id.tv_name);
        tv_sex = findViewById(R.id.tv_sex);
        tv_age = findViewById(R.id.tv_age);
        tv_phone = findViewById(R.id.tv_phone);
        tv_desc = findViewById(R.id.tv_desc);
        iv_qrcode = findViewById(R.id.iv_qrcode);
        ll_content = findViewById(R.id.ll_content);
        ll_download = findViewById(R.id.ll_download);

        ll_download.setOnClickListener(this);

        loadInfo();
    }

    /**
     * 加载个人信息
     */
    @SuppressLint("SetTextI18n")
    private void loadInfo() {
        IMUser user = BmobManager.getInstance().getUser();

        GlideHelper.loadUrl(this, user.getPhoto(), iv_photo);
        tv_name.setText(user.getNickName());
        tv_sex.setText(user.isSex() ? R.string.text_me_info_boy : R.string.text_me_info_girl);
        tv_age.setText(user.getAge() + " " + getString(R.string.text_search_age));
        tv_phone.setText(user.getMobilePhoneNumber());
        tv_desc.setText(user.getDesc());

        createQRCode(user.getObjectId());
    }

    /**
     * 生成 二维码
     *
     * @param userId 用户 id
     */
    private void createQRCode(final String userId) {

        iv_qrcode.post(new Runnable() {
            @Override
            public void run() {
                String text = "codesaid_IM#" + userId;
                Bitmap bitmap = CodeUtils.createImage(text,
                        iv_qrcode.getWidth(), iv_qrcode.getHeight(), null);
                iv_qrcode.setImageBitmap(bitmap);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_download:

                break;
        }
    }
}
