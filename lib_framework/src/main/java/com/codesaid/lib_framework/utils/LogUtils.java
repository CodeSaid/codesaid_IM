package com.codesaid.lib_framework.utils;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import com.codesaid.lib_framework.BuildConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created By codesaid
 * On :2019-12-21
 * Package Name: com.codesaid.lib_framework.utils
 * desc : Log 工具类
 * 功能: 打印日志 并且记录日志到本地
 */
public class LogUtils {

    private static SimpleDateFormat mSimpleDateFormat = new
            SimpleDateFormat("YYYY-MM-dd HH:mm:ss", Locale.CHINA);

    public static void i(String text) {
        // 判断是否是 debug 模式
        if (BuildConfig.LOG_DEBUG) {
            // 判断是否为空
            if (!TextUtils.isEmpty(text)) {
                Log.i(BuildConfig.LOG_TAG, text);
                writeLogToFile(text);
            }
        }
    }

    public static void e(String text) {
        // 判断是否是 debug 模式
        if (BuildConfig.LOG_DEBUG) {
            // 判断是否为空
            if (!TextUtils.isEmpty(text)) {
                Log.e(BuildConfig.LOG_TAG, text);
                writeLogToFile(text);
            }
        }
    }

    public static void w(String text) {
        // 判断是否是 debug 模式
        if (BuildConfig.LOG_DEBUG) {
            // 判断是否为空
            if (!TextUtils.isEmpty(text)) {
                Log.w(BuildConfig.LOG_TAG, text);
                writeLogToFile(text);
            }
        }
    }

    public static void d(String text) {
        // 判断是否是 debug 模式
        if (BuildConfig.LOG_DEBUG) {
            // 判断是否为空
            if (!TextUtils.isEmpty(text)) {
                Log.d(BuildConfig.LOG_TAG, text);
                writeLogToFile(text);
            }
        }
    }

    public static void v(String text) {
        // 判断是否是 debug 模式
        if (BuildConfig.LOG_DEBUG) {
            // 判断是否为空
            if (!TextUtils.isEmpty(text)) {
                Log.v(BuildConfig.LOG_TAG, text);
                writeLogToFile(text);
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("SdCardPath")
    private static void writeLogToFile(String text) {
        // 文件路径
        String fileName = "/sdcard/CodeSaid/IM.log";

        // log 格式 : 时间 + 内容
        String log = mSimpleDateFormat.format(new Date()) + " " + text;

        // 检查父路径是否存在
        File groupFile = new File("/sdcard/CodeSaid/");
        if (!groupFile.exists()) {
            groupFile.mkdirs();
        }
        // 开始写入
        FileOutputStream fileOutputStream = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileOutputStream = new FileOutputStream(fileName, true);
            // 编码问题 GBK 才能正确写入中文
            bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(fileOutputStream, Charset.forName("gbk")));
            bufferedWriter.write(log);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
