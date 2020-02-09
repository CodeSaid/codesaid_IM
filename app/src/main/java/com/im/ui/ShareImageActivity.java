package com.im.ui;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.codesaid.lib_framework.base.BaseBackActivity;
import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.bmob.IMUser;
import com.codesaid.lib_framework.helper.GlideHelper;
import com.codesaid.lib_framework.utils.toast.ToastUtils;
import com.codesaid.lib_framework.view.LoadingView;
import com.im.R;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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

    private LoadingView mLoadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_share_img);

        initView();
    }

    private void initView() {

        mLoadingView = new LoadingView(ShareImageActivity.this);
        mLoadingView.setLoadingText(getString(R.string.text_shar_save_ing));

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

                /**
                 * 1.View截图
                 * 2.创建一个Bitmap
                 * 3.保存到相册
                 */
                mLoadingView.show();

                /**
                 * setDrawingCacheEnabled
                 * 保留我们的绘制副本
                 * 1.重新测量
                 * 2.重新布局
                 * 3.得到我们的DrawingCache
                 * 4.转换成Bitmap
                 */

                ll_content.setDrawingCacheEnabled(true);


                ll_content.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

                ll_content.layout(0, 0, ll_content.getMeasuredWidth(),
                        ll_content.getMeasuredHeight());

                Bitmap bitmap = ll_content.getDrawingCache();

                if (bitmap != null) {
                    saveBitmapToAlbum(bitmap);
                }
                break;
        }
    }

    private void saveBitmapToAlbum(Bitmap bitmap) {
        File rootPath = new File(Environment.getExternalStorageDirectory() + "/codesaid_IM/");

        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }

        File file = new File(rootPath, System.currentTimeMillis() + ".png");

        try {
            FileOutputStream outputStream = new FileOutputStream(file);

            // 自带的保存方法
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
            outputStream.flush();
            outputStream.close();

            mLoadingView.hide();
            ToastUtils.show(ShareImageActivity.this, "保存成功");

            updateAlbum(file.getPath());
        } catch (IOException e) {
            e.printStackTrace();
            mLoadingView.hide();
            ToastUtils.show(ShareImageActivity.this, "保存失败");
        }
    }

    /**
     * 刷新图库
     */
    private void updateAlbum(String path) {
        /**
         * 存在兼容性问题
         */

        // 通过广播刷新
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(path)));

        // 通过数据库的方式插入
        ContentValues values = new ContentValues(4);
        values.put(MediaStore.Video.Media.TITLE, "");
        values.put(MediaStore.Video.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Video.Media.DATA, path);
        values.put(MediaStore.Video.Media.DURATION, 0);
        getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
    }
}
