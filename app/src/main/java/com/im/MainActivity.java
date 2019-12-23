package com.im;

import android.content.res.AssetFileDescriptor;
import android.os.Bundle;

import com.codesaid.lib_framework.base.BaseActivity;
import com.codesaid.lib_framework.mediaplayer.MediaPlayerManager;
import com.codesaid.lib_framework.utils.log.LogUtils;

/**
 * @author codesaid
 */
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final MediaPlayerManager mediaPlayerManager = new MediaPlayerManager();
        AssetFileDescriptor assetFileDescriptor =
                getResources().openRawResourceFd(R.raw.guide);
        mediaPlayerManager.startPlay(assetFileDescriptor);

        mediaPlayerManager.setOnProgressListener(new MediaPlayerManager.OnProgressListener() {
            @Override
            public void onProgress(int position, int pos) {
                LogUtils.d("progress: " + pos + "total: " + mediaPlayerManager.getDuration());
            }
        });
    }
}
