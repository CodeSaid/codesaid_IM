package com.im.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.codesaid.lib_framework.base.BaseActivity;
import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.helper.FileHelper;
import com.codesaid.lib_framework.utils.log.LogUtils;
import com.codesaid.lib_framework.utils.toast.ToastUtils;
import com.codesaid.lib_framework.view.DialogManager;
import com.codesaid.lib_framework.view.DialogView;
import com.codesaid.lib_framework.view.LoadingView;
import com.im.R;

import java.io.File;

import cn.bmob.v3.exception.BmobException;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created By codesaid
 * On :2020-01-04
 * Package Name: com.im.ui
 */
public class FirstUploadActivity extends BaseActivity implements View.OnClickListener {

    private DialogView mPhotoSelectView;

    // 用户头像
    private File uploadFile = null;
    private LoadingView mLoadingView;

    /**
     * 跳转 Activity 到当前页面
     *
     * @param activity    activity
     * @param requestCode 返回码
     */
    public static void startActivity(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, FirstUploadActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    private CircleImageView mIvPhoto;
    private EditText etName;
    private Button mBtnUpload;

    private TextView tvCamera;
    private TextView tvAblum;
    private TextView tvCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_upload);

        initView();
    }

    private void initView() {

        initPhotoView();

        mIvPhoto = findViewById(R.id.iv_photo);
        etName = findViewById(R.id.et_nickname);
        mBtnUpload = findViewById(R.id.btn_upload);
        mBtnUpload.setEnabled(false);

        mIvPhoto.setOnClickListener(this);
        mBtnUpload.setOnClickListener(this);

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    mBtnUpload.setEnabled(uploadFile != null);
                } else {
                    mBtnUpload.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    /**
     * 显示 跳转到相册 or  拍照的提示框
     */
    private void initPhotoView() {

        mLoadingView = new LoadingView(this);
        mLoadingView.setLoadingText("正在上传头像中...");

        mPhotoSelectView = DialogManager
                .getInstance().initView(this, R.layout.dialog_select_photo, Gravity.BOTTOM);
        tvCamera = mPhotoSelectView.findViewById(R.id.tv_camera);
        tvCamera.setOnClickListener(this);
        tvAblum = mPhotoSelectView.findViewById(R.id.tv_ablum);
        tvAblum.setOnClickListener(this);
        tvCancel = mPhotoSelectView.findViewById(R.id.tv_cancel);
        tvCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_camera:
                DialogManager.getInstance().hide(mPhotoSelectView);
                // 跳转到相机
                FileHelper.getInstance().toCamera(this);
                break;
            case R.id.tv_ablum:
                // 跳转到相册
                DialogManager.getInstance().hide(mPhotoSelectView);

                FileHelper.getInstance().toAlbum(this);
                break;
            case R.id.tv_cancel:
                DialogManager.getInstance().hide(mPhotoSelectView);
                break;
            case R.id.iv_photo:
                // 显示 选择提示框
                DialogManager.getInstance().show(mPhotoSelectView);
                break;
            case R.id.btn_upload:
                uploadPhoto();
                break;
        }
    }

    /**
     * 上传头像
     */
    private void uploadPhoto() {
        String name = etName.getText().toString().trim();
        mLoadingView.show();
        BmobManager.getInstance().uploadFirstPhoto(name, uploadFile, new BmobManager.onUploadPhotoListener() {
            @Override
            public void onUploadSuccess() {
                mLoadingView.hide();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onUploadFail(BmobException e) {
                mLoadingView.hide();
                ToastUtils.show(FirstUploadActivity.this, "上传失败: " + e.toString());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        LogUtils.i("requestCode: " + requestCode);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == FileHelper.CAMERA_CODE) {
                uploadFile = FileHelper.getInstance().getTempFile();
            } else if (requestCode == FileHelper.ALBUM_CODE) {
                Uri uri = data.getData();
                if (uri != null) {
                    //String path = uri.getPath();
                    // 获取真实地址
                    String path = FileHelper.getInstance().getRealPathFromURI(this, uri);
                    LogUtils.i("path: " + path);
                    if (!TextUtils.isEmpty(path)) {
                        uploadFile = new File(path);
                    }
                }
            }
        }

        // 设置头像
        if (uploadFile != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(uploadFile.getPath());
            mIvPhoto.setImageBitmap(bitmap);

            // 判断当前输入框是否有内容
            String name = etName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                mBtnUpload.setEnabled(false);
            } else {
                mBtnUpload.setEnabled(true);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
