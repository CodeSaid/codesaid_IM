package com.im.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.codesaid.lib_framework.base.BaseUIActivity;
import com.codesaid.lib_framework.entity.Constants;
import com.codesaid.lib_framework.helper.GlideHelper;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        initView();
    }

    private void initView() {
        photo_view = findViewById(R.id.photo_view);
        iv_back = findViewById(R.id.iv_back);

        iv_back.setOnClickListener(this);

        Intent intent = getIntent();
        boolean isUrl = intent.getBooleanExtra(Constants.INTENT_IMAGE_TYPE, false);
        String url = intent.getStringExtra(Constants.INTENT_IMAGE_URL);

        if (isUrl) {
            GlideHelper.loadUrl(ImagePreviewActivity.this, url, photo_view);
        } else {
            GlideHelper.loadFile(ImagePreviewActivity.this, new File(url), photo_view);
        }
    }

    @Override
    public void onClick(View view) {

    }
}
