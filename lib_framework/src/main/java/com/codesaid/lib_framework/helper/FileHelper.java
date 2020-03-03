package com.codesaid.lib_framework.helper;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;

import com.codesaid.lib_framework.utils.log.LogUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created By codesaid
 * On :2020-01-04
 * Package Name: com.codesaid.lib_framework.helper
 * desc : 文件帮助类
 */
public class FileHelper {
    private static volatile FileHelper mInstance = null;
    private final SimpleDateFormat mSimpleDateFormat;

    private File tempFile = null;
    private Uri mImgUrl;

    // 相机
    public static final int CAMERA_CODE = 1008;
    // 相册
    public static final int ALBUM_CODE = 1009;

    //音乐
    public static final int MUSIC_REQUEST_CODE = 1006;
    //视频
    public static final int VIDEO_REQUEST_CODE = 1007;

    //裁剪结果
    public static final int CAMERA_CROP_RESULT = 1001;

    //裁剪文件
    private String cropPath;

    public File getTempFile() {
        return tempFile;
    }


    private FileHelper() {
        mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    }

    public static FileHelper getInstance() {
        if (mInstance == null) {
            synchronized (FileHelper.class) {
                if (mInstance == null) {
                    mInstance = new FileHelper();
                }
            }
        }
        return mInstance;
    }

    /**
     * 相机
     *
     * @param activity activity
     */
    public void toCamera(Activity activity) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String fileName = mSimpleDateFormat.format(new Date());
        tempFile = new File(Environment.getExternalStorageDirectory(), fileName + ".jpg");
        // 兼容 Android N
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mImgUrl = Uri.fromFile(tempFile);
        } else {
            // 利用 FileProvider
            mImgUrl = FileProvider.getUriForFile(activity,
                    activity.getPackageName() + ".fileprovider", tempFile);
            // 添加 读写 权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        }
        LogUtils.i("imgUrl: " + mImgUrl);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImgUrl);
        activity.startActivityForResult(intent, CAMERA_CODE);
    }

    /**
     * 跳转到相册
     *
     * @param activity activity
     */
    public void toAlbum(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        activity.startActivityForResult(intent, ALBUM_CODE);
    }

    /**
     * 跳转到音乐
     *
     * @param activity activity
     */
    public void toMusic(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        activity.startActivityForResult(intent, MUSIC_REQUEST_CODE);
    }

    /**
     * 跳转到视频
     *
     * @param activity activity
     */
    public void toVideo(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        activity.startActivityForResult(intent, VIDEO_REQUEST_CODE);
    }

    /**
     * 通过 Uri 查询真实地址
     *
     * @param uri uri
     */
    public String getRealPathFromURI(Context context, Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader =
                new CursorLoader(context, uri, proj, null, null, null);
        // 在后台查询
        Cursor cursor = cursorLoader.loadInBackground();

        int index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(index);
    }

    /**
     * 获取网络视频第一帧
     *
     * @param videoUrl
     * @return
     */
    public Bitmap getNetVideoBitmap(String videoUrl) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            //根据url获取缩略图
            retriever.setDataSource(videoUrl, new HashMap());
            //获得第一帧图片
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
        return bitmap;
    }

    public String getCropPath() {
        return cropPath;
    }

    /**
     * 裁剪
     *
     * @param mActivity
     * @param file
     */
    public void startPhotoZoom(Activity mActivity, File file) {
        LogUtils.i("startPhotoZoom" + file.getPath());
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(mActivity, "com.imooc.meet.fileprovider", file);
        } else {
            uri = Uri.fromFile(file);
        }

        if (uri == null) {
            return;
        }

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        //设置裁剪
        intent.putExtra("crop", "true");
        //裁剪宽高比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        //裁剪图片的质量
        intent.putExtra("outputX", 320);
        intent.putExtra("outputY", 320);
        //发送数据
        //intent.putExtra("return-data", true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        //单独存储裁剪文件，解决手机兼容性问题
        cropPath = Environment.getExternalStorageDirectory().getPath() + "/" + "meet.jpg";
        Uri mUriTempFile = Uri.parse("file://" + "/" + cropPath);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mUriTempFile);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        mActivity.startActivityForResult(intent, CAMERA_CROP_RESULT);
    }

    /**
     * 保存Bitmap到相册
     *
     * @param mContext
     * @param mBitmap
     */
    public boolean saveBitmapToAlbum(Context mContext, Bitmap mBitmap) {
        //根布局
        File rootPath = new File(Environment.getExternalStorageDirectory() + "/Meet/");

        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }

        File file = new File(rootPath, System.currentTimeMillis() + ".png");
        try {
            FileOutputStream out = new FileOutputStream(file);
            //自带的保存方法
            mBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Toast.makeText(mContext, "保存成功", Toast.LENGTH_SHORT).show();
            updateAlbum(mContext, file.getPath());
            return true;
        } catch (IOException e) {
            LogUtils.i("e:" + e.toString());
            Toast.makeText(mContext, "保存失败", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /**
     * 刷新图库
     */
    private void updateAlbum(Context mContext, String path) {
        /**
         * 存在兼容性的问题
         */

        try {
            //通过广播刷新图库
            mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(path)));

            //通过数据库的方式插入
            ContentValues values = new ContentValues(4);
            values.put(MediaStore.Video.Media.TITLE, "");
            values.put(MediaStore.Video.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Video.Media.DATA, path);
            values.put(MediaStore.Video.Media.DURATION, 0);
            mContext.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
