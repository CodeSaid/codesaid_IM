package com.codesaid.lib_framework.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorWindow;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;

import com.codesaid.lib_framework.utils.log.LogUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
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
}
