package com.crte.sipstackhome.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TimeUtils {

    // 时间
    public static String day(String s) {
        SimpleDateFormat format1 = new SimpleDateFormat("HH:mm:ss");
        String timestring1 = format1.format(Integer.valueOf(s));
        String[] my1 = timestring1.split(":");
        String times = my1[0] + "时 " + my1[1] + "分" + my1[2] + "秒";
        return times;
    }

    // 年月
    public static String year(String tr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String timestring = format.format(new Date(Integer.valueOf(tr)));
        String[] my = timestring.split("-");
        String year = my[0];
        String month = my[1];
        String day = my[2];
        String a = year + "年" + month + "月" + day + "日";
        return a;
    }

    // 将字符串转为时间戳

    public static String getTime(String user_time) {
        String re_time = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Date d;
        try {
            d = sdf.parse(user_time);
            long l = d.getTime();
            String str = String.valueOf(l);
            re_time = str.substring(0, 10);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return re_time;
    }

    // 将时间戳转为字符串
    public static String getStrTime(String cc_time) {
        String re_StrTime = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        // 例如：cc_time=1291778220
        long lcc_time = Long.valueOf(cc_time);
        re_StrTime = sdf.format(new Date(lcc_time * 1000L));
        return re_StrTime;

    }

    // 将时间戳转为字符串
    public static String getStrTimeInfo(String cc_time) {
        String re_StrTime = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒");
        // 例如：cc_time=1291778220
        long lcc_time = Long.valueOf(cc_time);
        re_StrTime = sdf.format(new Date(lcc_time * 1000L));
        return re_StrTime;

    }

    /***
     * 根据传入的时间戳，获得距离现在的时间
     *
     * @return
     */
    public static String getTimeLen(long time) {
        long newTime = System.currentTimeMillis();
        if (newTime < time) {
            return null;
        }

        long timeSecond = (newTime - time) / 1000;
        if (timeSecond < 60) {
            return (timeSecond + 1) + "秒前";
        } else if (timeSecond < 60 * 60) {
            return timeSecond / 60 + "分钟前";
        } else if (timeSecond < 60 * 60 * 24) {
            return timeSecond / 60 / 60 + "小时前";
        } else if (timeSecond > 60 * 60 * 24) {
            return timeSecond / 60 / 60 / 24 + "天前";
        } else {
            return "未知";
        }
    }

    //将毫秒转化为一段时间
    public static String getTime(long time) {
        long i = time / 1000;
        int minute = (int) (i / 60);
        int second = (int) (i % 60);
        return (minute + "'" + second + "\"");
    }
}
