package com.codesaid.lib_framework.utils.time;

/**
 * Created By codesaid
 * On :2019-12-22
 * Package Name: com.codesaid.lib_framework.utils
 * desc : 时间转换类
 */
public class TimeUtils {

    /**
     * @param ms 毫秒值
     * @return HH:mm:ss
     */
    public static String formatDuring(long ms) {
        long hours = (ms % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (ms % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (ms % (1000 * 60)) / 1000;

        String h = hours + "";
        if (hours < 10) {
            h = "0" + h;
        }

        String m = minutes + "";
        if (minutes < 10) {
            m = "0" + m;
        }

        String s = seconds + "";
        if (seconds < 10) {
            s = "0" + s;
        }

        return h + ":" + m + ":" + s;
    }
}
