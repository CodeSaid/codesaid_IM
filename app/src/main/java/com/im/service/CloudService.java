package com.im.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

/**
 * Created By codesaid
 * On :2020-01-04
 * Package Name: com.im.service
 * desc : 云服务
 */
public class CloudService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
