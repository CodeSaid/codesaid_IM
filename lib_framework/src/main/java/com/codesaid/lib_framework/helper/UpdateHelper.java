package com.codesaid.lib_framework.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.codesaid.lib_framework.R;
import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.bmob.UpdateSet;
import com.codesaid.lib_framework.net.HttpManager;
import com.codesaid.lib_framework.utils.log.LogUtils;
import com.codesaid.lib_framework.view.DialogManager;
import com.codesaid.lib_framework.view.DialogView;

import java.io.File;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * Created By codesaid
 * On :2020-02-29 21:08
 * Package Name: com.codesaid.lib_framework.helper
 * desc: App update
 */
public class UpdateHelper {

    private Context mContext;

    private DialogView mUpdateView;
    private TextView tv_desc;
    private TextView tv_confirm;
    private TextView tv_cancel;

    private ProgressDialog mProgressDialog;

    public UpdateHelper(Context mContext) {
        this.mContext = mContext;
    }

    public void updateApp(final OnUpdateAppListener listener) {
        BmobManager.getInstance().queryUpdateSet(new FindListener<UpdateSet>() {
            @Override
            public void done(List<UpdateSet> list, BmobException e) {
                if (e == null) {
                    if (list != null && list.size() > 0) {
                        UpdateSet updateSet = list.get(0);
                        //获取自己的VersionCode
                        try {
                            int AppCode = mContext.getPackageManager().
                                    getPackageInfo(mContext.getPackageName(), 0).versionCode;
                            //有更新
                            if (listener != null) {
                                listener.OnUpdate(updateSet.getVersionCode() > AppCode ? true : false);
                            }
                            if (updateSet.getVersionCode() > AppCode) {
                                //检测到有更新比对版本
                                createUpdateDialog(updateSet);
                            }
                        } catch (PackageManager.NameNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    /**
     * 更新提示框
     */
    private void createUpdateDialog(final UpdateSet updateSet) {
        mUpdateView = DialogManager.getInstance().initView(mContext, R.layout.dialog_update_app);
        tv_desc = mUpdateView.findViewById(R.id.tv_update_desc);
        tv_confirm = mUpdateView.findViewById(R.id.tv_confirm);
        tv_cancel = mUpdateView.findViewById(R.id.tv_cancel);

        tv_desc.setText(updateSet.getDesc());

        tv_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogManager.getInstance().hide(mUpdateView);
                downloadApk(updateSet);
            }
        });
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogManager.getInstance().hide(mUpdateView);
            }
        });
        DialogManager.getInstance().show(mUpdateView);

        initProgress();
    }

    private void initProgress() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
    }

    /**
     * 下载
     */
    private void downloadApk(UpdateSet updateSet) {
        if (TextUtils.isEmpty(updateSet.getPath())) {
            return;
        }

        final String filePath = "/sdcard/Meet/" + System.currentTimeMillis() + ".apk";

        if (mProgressDialog != null) {
            mProgressDialog.show();
        }

        //开始下载：
        HttpManager.getInstance().download(updateSet.getPath(), filePath, new HttpManager.OnDownloadListener() {
            @Override
            public void onDownloadSuccess(String path) {
                mProgressDialog.dismiss();
                LogUtils.i("onDownloadSuccess:" + path);
                if (!TextUtils.isEmpty(path)) {
                    installApk(path);
                }
            }

            @Override
            public void onDownloading(int progress) {
                mProgressDialog.setProgress(progress);
                LogUtils.i("onDownloading:" + progress);
            }

            @Override
            public void onDownloadFailed(Exception e) {
                mProgressDialog.dismiss();
                LogUtils.i("onDownloadFailed:" + e.toString());
            }
        });
    }

    public interface OnUpdateAppListener {
        void OnUpdate(boolean isUpdate);
    }

    /**
     * 安装Apk
     *
     * @param filePath
     * @return
     */
    public void installApk(String filePath) {
        //        try{
        //            Intent intent = new Intent(Intent.ACTION_VIEW);
        //            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //            intent.setDataAndType(Uri.parse("file://" + filePath), "application/vnd.android.package-archive");
        //            mContext.startActivity(intent);
        //        }catch (Exception e){
        //            LogUtils.e("installApk:" + e.toString());
        //            e.toString();
        //        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        File apkFile = new File(filePath);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri uri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".fileprovider", apkFile);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        mContext.startActivity(intent);
    }
}
