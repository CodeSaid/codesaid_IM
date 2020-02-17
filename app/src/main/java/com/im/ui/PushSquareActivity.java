package com.im.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.codesaid.lib_framework.base.BaseBackActivity;
import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.bmob.SquareSet;
import com.codesaid.lib_framework.helper.FileHelper;
import com.codesaid.lib_framework.utils.toast.ToastUtils;
import com.codesaid.lib_framework.view.LoadingView;
import com.im.R;

import java.io.File;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadFileListener;

/**
 * Created By codesaid
 * On :2020-02-16:19:26
 * Package Name: com.im.ui
 * desc: 发布 圈子
 */
public class PushSquareActivity extends BaseBackActivity implements View.OnClickListener {

    //输入框
    private EditText et_content;
    //文字数量
    private TextView tv_content_size;
    //清空
    private ImageView iv_error;
    //媒体路径
    private TextView tv_media_path;
    //已存媒体
    private LinearLayout ll_media;
    //相机
    private LinearLayout ll_camera;
    //相册
    private LinearLayout ll_ablum;
    //音乐
    private LinearLayout ll_music;
    //视频
    private LinearLayout ll_video;
    //媒体类型
    private LinearLayout ll_media_type;

    private LoadingView mLoadingView;

    // 要上传的文件
    private File uploadFile = null;

    // 媒体类型
    private int mediaType = SquareSet.PUSH_TEXT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_square);

        initView();
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        mLoadingView = new LoadingView(this);
        mLoadingView.setLoadingText(getString(R.string.text_push_ing));

        et_content = findViewById(R.id.et_content);
        tv_content_size = findViewById(R.id.tv_content_size);
        iv_error = findViewById(R.id.iv_error);
        tv_media_path = findViewById(R.id.tv_media_path);
        ll_media = findViewById(R.id.ll_media);
        ll_camera = findViewById(R.id.ll_camera);
        ll_ablum = findViewById(R.id.ll_ablum);
        ll_music = findViewById(R.id.ll_music);
        ll_video = findViewById(R.id.ll_video);
        ll_media_type = findViewById(R.id.ll_media_type);

        iv_error.setOnClickListener(this);
        ll_camera.setOnClickListener(this);
        ll_ablum.setOnClickListener(this);
        ll_music.setOnClickListener(this);
        ll_video.setOnClickListener(this);

        // 输入框监听
        et_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                tv_content_size.setText(charSequence.length() + "/140");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_error:
                ll_media_type.setVisibility(View.VISIBLE);
                ll_media.setVisibility(View.GONE);
                uploadFile = null;
                mediaType = SquareSet.PUSH_TEXT;
                break;
            case R.id.ll_camera:
                FileHelper.getInstance().toCamera(this);
                break;
            case R.id.ll_ablum:
                FileHelper.getInstance().toAlbum(this);
                break;
            case R.id.ll_music:
                FileHelper.getInstance().toMusic(this);
                break;
            case R.id.ll_video:
                FileHelper.getInstance().toVideo(this);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case FileHelper.ALBUM_CODE:
                case FileHelper.MUSIC_REQUEST_CODE:
                case FileHelper.VIDEO_REQUEST_CODE:
                    if (data != null) {
                        Uri uri = data.getData();
                        String path = FileHelper.getInstance()
                                .getRealPathFromURI(PushSquareActivity.this, uri);
                        if (!TextUtils.isEmpty(path)) {
                            if (path.endsWith(".jpg")
                                    || path.endsWith(".png")
                                    || path.endsWith(".jpeg")) {
                                // 图片
                                tv_media_path.setText(getString(R.string.text_push_type_img));
                                mediaType = SquareSet.PUSH_IMAGE;
                            } else if (path.endsWith("mp3")) {
                                // 音乐
                                tv_media_path.setText(getString(R.string.text_push_type_music));
                                mediaType = SquareSet.PUSH_MUSIC;
                            } else if (path.endsWith("mp4") ||
                                    path.endsWith("wav") ||
                                    path.endsWith("avi")) {
                                // 视频
                                tv_media_path.setText(getString(R.string.text_push_type_video));
                                mediaType = SquareSet.PUSH_VIDEO;
                            }
                            uploadFile = new File(path);
                            ll_media_type.setVisibility(View.GONE);
                            ll_media.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
                case FileHelper.CAMERA_CODE:
                    uploadFile = FileHelper.getInstance().getTempFile();

                    // 图片
                    tv_media_path.setText(getString(R.string.text_push_type_img));
                    mediaType = SquareSet.PUSH_IMAGE;

                    ll_media_type.setVisibility(View.GONE);
                    ll_media.setVisibility(View.VISIBLE);
                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.input_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_input:
                uploadToSquare();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 上传内容到 圈子
     */
    private void uploadToSquare() {
        // 获取 用户 输入的 内容
        final String content = et_content.getText().toString().trim();

        if (TextUtils.isEmpty(content) && uploadFile == null) {
            ToastUtils.show(PushSquareActivity.this, getString(R.string.text_push_ed_null));
            return;
        }
        mLoadingView.show();

        if (uploadFile != null) {
            //上传文件
            final BmobFile bmobFile = new BmobFile(uploadFile);
            bmobFile.uploadblock(new UploadFileListener() {
                @Override
                public void done(BmobException e) {
                    if (e == null) {
                        push(content, bmobFile.getFileUrl());
                    }
                }
            });
        } else {
            push(content, "");
        }
    }

    /**
     * 发布 纯文本
     *
     * @param content 文字
     * @param path    路径
     */
    private void push(String content, String path) {
        BmobManager.getInstance().pushSquare(mediaType, content, path, new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                mLoadingView.hide();
                if (e == null) {
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    ToastUtils.show(PushSquareActivity.this,
                            getString(R.string.text_push_fail) + e.toString());
                }
            }
        });
    }
}
