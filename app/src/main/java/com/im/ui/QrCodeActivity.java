package com.im.ui;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.codesaid.lib_framework.base.BaseUIActivity;
import com.codesaid.lib_framework.helper.FileHelper;
import com.im.MainActivity;
import com.im.R;
import com.uuzuche.lib_zxing.activity.CaptureFragment;
import com.uuzuche.lib_zxing.activity.CodeUtils;

/**
 * Created By codesaid
 * On :2020-02-07:14:29
 * Package Name: com.im.ui
 * desc: 二维码 扫描
 */
public class QrCodeActivity extends BaseUIActivity implements View.OnClickListener {

    private static final int REQUEST_IMAGE = 1234;
    //返回键
    private ImageView iv_back;
    //相册选择
    private TextView iv_to_ablum;
    //闪光灯
    private ImageView iv_flashlight;

    // 是否打开闪光灯
    private boolean isOpenLight = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        initQrCode();
        initView();
    }

    /**
     * 二维码解析回调函数
     */
    CodeUtils.AnalyzeCallback analyzeCallback = new CodeUtils.AnalyzeCallback() {
        @Override
        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_SUCCESS);
            bundle.putString(CodeUtils.RESULT_STRING, result);
            resultIntent.putExtras(bundle);
            setResult(RESULT_OK, resultIntent);
            finish();
        }

        @Override
        public void onAnalyzeFailed() {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_FAILED);
            bundle.putString(CodeUtils.RESULT_STRING, "");
            resultIntent.putExtras(bundle);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    };

    /**
     * 初始化 二维码
     */
    private void initQrCode() {
        /**
         * 执行扫面Fragment的初始化操作
         */
        CaptureFragment captureFragment = new CaptureFragment();
        // 为二维码扫描界面设置定制化界面
        CodeUtils.setFragmentArgs(captureFragment, R.layout.layout_qrcode);

        captureFragment.setAnalyzeCallback(analyzeCallback);
        /**
         * 替换我们的扫描控件
         */
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_my_container, captureFragment).commit();
    }

    private void initView() {
        iv_back = findViewById(R.id.iv_back);
        iv_to_ablum = findViewById(R.id.iv_to_ablum);
        iv_flashlight = findViewById(R.id.iv_flashlight);

        iv_back.setOnClickListener(this);
        iv_to_ablum.setOnClickListener(this);
        iv_flashlight.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back: //返回
                finish();
                break;
            case R.id.iv_to_ablum: // 相册选择
                openAblum();
                break;
            case R.id.iv_flashlight: // 闪光灯
                isOpenLight = !isOpenLight;
                CodeUtils.isLightEnable(isOpenLight);
                iv_flashlight.setImageResource(isOpenLight ?
                        R.drawable.img_flashlight_p : R.drawable.img_flashlight);
                break;
        }
    }

    /**
     * 打开相册
     */
    private void openAblum() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == REQUEST_IMAGE) {
            if (data != null) {
                Uri uri = data.getData();
                String path = FileHelper.getInstance()
                        .getRealPathFromURI(QrCodeActivity.this, uri);
                try {
                    CodeUtils.analyzeBitmap(path, new CodeUtils.AnalyzeCallback() {
                        @Override
                        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                            //解析结果
                            analyzeCallback.onAnalyzeSuccess(mBitmap,result);
                        }

                        @Override
                        public void onAnalyzeFailed() {
                            //解析二维码失败
                            analyzeCallback.onAnalyzeFailed();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
