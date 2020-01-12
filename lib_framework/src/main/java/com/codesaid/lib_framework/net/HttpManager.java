package com.codesaid.lib_framework.net;

import com.codesaid.lib_framework.cloud.CloudManager;
import com.codesaid.lib_framework.utils.SHA1;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created By codesaid
 * On :2020-01-11
 * Package Name: com.codesaid.lib_framework.net
 * desc : 网络请求
 */
public class HttpManager {
    private static volatile HttpManager mInstance = null;

    private OkHttpClient mOkHttpClient;

    private HttpManager() {
        mOkHttpClient = new OkHttpClient();
    }

    public static HttpManager getInstance() {
        if (mInstance == null) {
            synchronized (HttpManager.class) {
                if (mInstance == null) {
                    mInstance = new HttpManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 请求 融云 token
     *
     * @param map 参数
     * @return token
     */
    public String postCloudToken(HashMap<String, String> map) {

        //参数
        String Timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String Nonce = String.valueOf(Math.floor(Math.random() * 100000));
        String Signature = SHA1.sha1(CloudManager.CLOUD_SECRET + Nonce + Timestamp);

        //参数填充
        FormBody.Builder builder = new FormBody.Builder();
        for (String key : map.keySet()) {
            builder.add(key, map.get(key));
        }
        RequestBody requestBody = builder.build();
        //添加签名规则
        Request request = new Request.Builder()
                .url(CloudManager.TOKEN_URL)
                .addHeader("Timestamp", Timestamp)
                .addHeader("App-Key", CloudManager.CLOUD_KEY)
                .addHeader("Nonce", Nonce)
                .addHeader("Signature", Signature)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(requestBody)
                .build();
        try {
            return mOkHttpClient.newCall(request).execute().body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
