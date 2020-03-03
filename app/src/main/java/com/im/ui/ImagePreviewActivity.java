package com.im.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.codesaid.lib_framework.base.BaseUIActivity;
import com.codesaid.lib_framework.entity.Constants;
import com.codesaid.lib_framework.helper.FileHelper;
import com.codesaid.lib_framework.helper.GlideHelper;
import com.codesaid.lib_framework.utils.toast.ToastUtils;
import com.github.chrisbanes.photoview.PhotoView;
import com.im.R;

import java.io.File;

/**
 * Created By codesaid
 * On :2020-01-26
 * Package Name: com.im.ui
 * desc : 图片 预览类
 */
public class ImagePreviewActivity extends BaseUIActivity implements View.OnClickListener {

    /**
     * 跳转到当前的 Activity
     *
     * @param context context
     * @param isUrl   是否是图片链接
     * @param url     图片链接
     */
    public static void startActivity(Context context, boolean isUrl, String url) {
        Intent intent = new Intent(context, ImagePreviewActivity.class);
        intent.putExtra(Constants.INTENT_IMAGE_TYPE, isUrl);
        intent.putExtra(Constants.INTENT_IMAGE_URL, url);
        context.startActivity(intent);
    }

    private PhotoView photo_view;
    private ImageView iv_back;
    private TextView tv_download;

    //图片地址
    private String url;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        initView();
    }

    private void initView() {
        photo_view = findViewById(R.id.photo_view);
        iv_back = findViewById(R.id.iv_back);
        tv_download = findViewById(R.id.tv_download);

        iv_back.setOnClickListener(this);
        tv_download.setOnClickListener(this);

        Intent intent = getIntent();
        boolean isUrl = intent.getBooleanExtra(Constants.INTENT_IMAGE_TYPE, false);
        url = intent.getStringExtra(Constants.INTENT_IMAGE_URL);

        //图片地址才下载，File代表本次已经存在
        tv_download.setVisibility(isUrl ? View.VISIBLE : View.GONE);

        if (isUrl) {
            GlideHelper.loadUrl(ImagePreviewActivity.this, url, photo_view);
        } else {
            GlideHelper.loadFile(ImagePreviewActivity.this, new File(url), photo_view);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_download:
                ToastUtils.show(this, getString(R.string.text_iv_pre_downloading));
                GlideHelper.loadUrlToBitmap(this, url, new GlideHelper.OnGlideBitmapResultListener() {
                    @Override
                    public void onResourceReady(Bitmap resource) {
                        if (resource != null) {
                            FileHelper.getInstance().saveBitmapToAlbum(ImagePreviewActivity.this, resource);
                        } else {
                            Toast.makeText(ImagePreviewActivity.this, getString(R.string.text_iv_pre_save_fail), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
        }
    }
}
