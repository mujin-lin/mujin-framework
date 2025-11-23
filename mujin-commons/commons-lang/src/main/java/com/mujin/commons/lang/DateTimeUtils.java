package com.mujin.commons.lang;



import cn.hutool.core.util.StrUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间类型工具类
 *
 * @author chenglin.wu
 * @date 2021年4月22日
 */
@SuppressWarnings("unused")
public final class DateTimeUtils {

    private DateTimeUtils() {
    }

    /**
     * 将Long、String(yyyy-MM-dd HH:mm:ss格式或yyyy-MM-dd格式)转成时间类型
     *
     * @param time 时间数据
     * @return Date
     * @date 2021/4/22
     */
    public static Date objectToDate(Object time) throws Exception {
        Date date = null;
        if (time == null) {
            return null;
        } else if (time instanceof Long) {
            long l = (long) time;
            if (l <= 0) {
                return null;
            }
            date = new Date((Long) time);
        } else if (time instanceof Date) {
            date = (Date) time;
        } else if (time instanceof String) {
            try {
                long t = Long.parseLong((String) time);
                date = new Date(t);
            } catch (Exception e) {
                if (((String) time).length() > 10) {
                    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    date = sdf1.parse((String) time);
                } else {
                    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
                    date = sdf2.parse((String) time);
                }
            }
        } else if (time instanceof Integer) {
            if ((Integer) time > 0) {
                date = new Date((Integer) time);
            }
        } else {
            throw new Exception("时间类型数据错误");
        }
        return date;
    }

    /**
     * 将Date、String(yyyy-MM-dd HH:mm:ss格式或yyyy-MM-dd格式)转成Long
     *
     * @param time 时间数据
     * @return Long
     * @date 2021/4/22
     */
    public static Long objectToLong(Object time) throws Exception {
        Long date = null;
        if (time == null) {
            return null;
        } else if (time instanceof Long) {
            date = (Long) time;
        } else if (time instanceof Date) {
            date = ((Date) time).getTime();
        } else if (time instanceof String) {
            try {
                date = Long.parseLong((String) time);
            } catch (Exception e) {
                if (((String) time).length() > 10) {
                    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    date = sdf1.parse((String) time).getTime();
                } else {
                    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
                    date = sdf2.parse((String) time).getTime();
                }
            }
        } else if (time instanceof Integer) {
            if ((Integer) time > 0) {
                date = (long) time;
            }
        } else {
            throw new Exception("请输入正确时间类型");
        }
        return date;
    }

    /**
     * 将Date、Long转成String
     *
     * @param time 时间数据
     * @param type 返回String类型的格式
     * @return String
     * @date 2021/4/22
     */
    public static String objectToString(Object time, TimeFormat type) throws Exception {
        String date = null;
        if (time == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(type.getValue());
        if (time instanceof Long) {
            date = sdf.format(new Date((long) time));
        } else if (time instanceof Date) {
            date = sdf.format((Date) time);
        } else if (time instanceof String) {
            try {
                long t = Long.parseLong((String) time);
                date = sdf.format(new Date(t));
            } catch (Exception e) {
                date = (String) time;
            }
        } else if (time instanceof Integer) {
            if ((Integer) time > 0) {
                date = sdf.format(new Date((long) time));
            }
        } else {
            throw new Exception("请输入正确时间类型");
        }
        return date;
    }

    /**
     * 获取与当前时间相差某个时间段的日期
     *
     * @param year  年
     * @param month 月
     * @param day   日
     * @param type  返回的时间格式
     * @return Date
     * @date 2021/4/22
     */
    public static Date getDate(int year, int month, int day, TimeFormat type) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat(type.getValue());
        Date date;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, year);
        calendar.add(Calendar.MONTH, month);
        calendar.add(Calendar.DATE, day);
        date = calendar.getTime();
        date = df.parse(df.format(date));
        return date;
    }

    /**
     * 获取与给定时间相差某个时间段的日期
     *
     * @param date  日期
     * @param year  年
     * @param month 月
     * @param day   日
     * @param type  返回数据的时间格式
     * @return Date
     * @date 2021/4/22
     */
    public static Date getDate(Date date, int year, int month, int day, TimeFormat type) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat(type.getValue());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, year);
        calendar.add(Calendar.MONTH, month);
        calendar.add(Calendar.DATE, day);
        return df.parse(df.format(calendar.getTime()));
    }

    /**
     * 通过已经格式化的时间类型的字符串和格式获取到对应的时间Date
     *
     * @param formatDate 格式化完成的时间字符串
     * @param format     格式
     * @return Date
     * @date 2021/4/22
     */
    public static Date getDate(String formatDate, TimeFormat format) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat(format.getValue());
        return df.parse(formatDate);
    }

    /**
     * 获取与当前时间相差某个时间段的时间
     *
     * @param hour   时
     * @param minute 分
     * @param second 秒
     * @param type   返回时间的格式
     * @return Date
     * @date 2021/4/22
     */
    public static Date getTime(int hour, int minute, int second, TimeFormat type) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat(type.getValue());
        Date date;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, hour);
        calendar.add(Calendar.MINUTE, minute);
        calendar.add(Calendar.SECOND, second);
        date = calendar.getTime();
        date = df.parse(df.format(date));
        return date;
    }

    /**
     * 获取与当前时间相差某个时间段的时间
     *
     * @param date   时间数据
     * @param hour   时
     * @param minute 分
     * @param second 秒
     * @param type   返回时间的格式
     * @return Date
     * @date 2021/4/22
     */
    public static Date getTime(Date date, int hour, int minute, int second, TimeFormat type) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat(type.getValue());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, hour);
        calendar.add(Calendar.MINUTE, minute);
        calendar.add(Calendar.SECOND, second);
        return df.parse(df.format(calendar.getTime()));
    }

    /**
     * 获取与传入时间相差某个时间段的时间
     *
     * @param date         时间
     * @param timeInterval 间隔时间
     * @param timeType     间隔时间类型(使用Calendar类的常量)
     * @return Date
     * @date 2021/4/22
     */
    @SuppressWarnings("all")
    public static Date getTime(Date date, int timeInterval, TimeTypes timeType) throws ParseException {
        String format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat df = new SimpleDateFormat(format);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(timeType.getType(), timeInterval);
        return df.parse(df.format(calendar.getTime()));
    }

    /**
     * 时间保留到小时
     *
     * @param date 时间数据
     * @return Date
     * @date 2021/4/22
     */
    public static Date getDateHold2Hour(Date date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(TimeFormat.HOUR.getValue());
        String formatStr = format.format(date);
        return format.parse(formatStr);
    }

    /**
     * 去除时分秒，只保留日期
     *
     * @param time 时间数据
     * @return Date
     * @date 2021/4/22
     */
    public static Date getDateFromTime(Date time) throws Exception {
        if (time == null) {
            throw new Exception("传入时间为null");
        }
        SimpleDateFormat df = new SimpleDateFormat(TimeFormat.DATE.getValue());
        return df.parse(df.format(time));
    }

    /***
     * 判断两个时间大小(date1 - date2)
     *
     * @param date1    时间1
     * @param date2    时间2
     * @param timeType 对比单位(year,month,day,hour,minutes,second,millisecond)
     * @return long
     * @date 2021/4/22
     */
    public static long compareTo(Date date1, Date date2, TimeTypes timeType) throws Exception {
        if (date1 == null && date2 == null) {
            return 0;
        } else if (date1 == null || date2 == null) {
            throw new Exception("传入时间不同时为null，无法进行比较");
        }
        // 获取两个时间代表的毫秒数
        long d1 = objectToLong(date1);
        long d2 = objectToLong(date2);
        return (d1 - d2) / timeType.getTime();
    }

    /**
     * 获取前天的日期
     *
     * @return Date
     * @date 2021/4/22
     */
    public static Date getBeforeYesterday() throws ParseException {
        // 得到一个Calendar的实例
        Calendar ca = Calendar.getInstance();
        // 设置时间为当前时间
        ca.setTime(new Date());
        ca.add(Calendar.DATE, -2);
        Date date = ca.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TimeFormat.DATE.getValue());
        String format = simpleDateFormat.format(date);
        return simpleDateFormat.parse(format);
    }

    /**
     * 获取给定时间的前天的日期
     *
     * @param date 给定的时间
     * @return Date
     * @date 2021/4/22
     */
    public static Date getBeforeYesterday(Date date) throws ParseException {
        // 得到一个Calendar的实例
        Calendar ca = Calendar.getInstance();
        // 设置时间为当前时间
        ca.setTime(date);
        ca.add(Calendar.DATE, -2);
        Date result = ca.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TimeFormat.DATE.getValue());
        String format = simpleDateFormat.format(result);
        return simpleDateFormat.parse(format);
    }

    /**
     * 获取指定时间的下一天的日期
     *
     * @param date 指定时间
     * @return Date
     * @date 2021/4/22
     */
    public static Date getNextDay(Date date) {
        Calendar ca = Calendar.getInstance();// 得到一个Calendar的实例
        ca.setTime(date); // 设置时间为当前时间
        ca.add(Calendar.DATE, 1);
        return ca.getTime();
    }

    /**
     * 获取指定时间的上一天的日期
     *
     * @param date 指定时间
     * @return Date
     * @date 2021/4/22
     */
    public static Date getYesterday(Date date) {
        Calendar ca = Calendar.getInstance();// 得到一个Calendar的实例
        ca.setTime(date); // 设置时间为当前时间
        ca.add(Calendar.DATE, -1);
        return ca.getTime();
    }

    /**
     * Date1是否是Date2之前的时间
     *
     * @param date1 时间1
     * @param date2 时间2
     * @param flag  是否包含等于
     * @return boolean
     * @date 2021/4/22
     */
    public static boolean Date1BeforeDate2(Date date1, Date date2, boolean flag) throws Exception {
        boolean result = true;
        if (date1 == null || date2 == null) {
            throw new Exception("传入时间为null!");
        } else {
            if (date1.getTime() > date2.getTime()) {
                result = false;
            } else if (!flag && date1.getTime() == date2.getTime()) {
                result = false;
            }
        }
        return result;
    }

    /**
     * date是否是指定时间段的时间
     *
     * @param date      时间数据
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @return boolean
     * @date 2021/4/22
     */
    public static boolean dateBetween(Date date, Date beginTime, Date endTime) throws Exception {
        boolean result = true;
        if (date == null || beginTime == null || endTime == null) {
            throw new Exception("传入时间为null!");
        } else {
            long time = date.getTime();
            if (time <= beginTime.getTime() || time >= endTime.getTime()) {
                result = false;
            }
        }
        return result;
    }

    /**
     * 获取两个时间的间隔时间(x天x小时x分)
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return String 返回过去了多久
     * @date 2021/4/22
     */
    public static String getTimeStr(Date begin, Date end) {
        long date = end.getTime() - begin.getTime();
        long dayF = TimeTypes.DAY.getTime();
        long hourF = TimeTypes.HOUR.getTime();
        long minF = TimeTypes.MINUTES.getTime();
        // 计算天数
        long day = date / dayF;
        // 计算分钟
        long hour = date % dayF / hourF;
        // 计算分钟
        long min = date % dayF % hourF / minF;
        StringBuilder strBuilder = new StringBuilder();
        if (day != 0) {
            strBuilder.append(day).append("天");
        }
        if (hour != 0) {
            strBuilder.append(hour).append("小时");
        }
        strBuilder.append(min).append("分钟");
        return strBuilder.toString();
    }

    /**
     * 通过身份证号返回生日
     *
     * @param idCard 身份证号
     * @return Date
     * @date 2021/4/22
     */
    public static Date getBirthday2IdCard(String idCard) throws ParseException {
        if (StrUtil.isBlank(idCard)) {
            return null;
        }
        // 匹配身份证的正则表达式
        boolean idCardFlag = RegexUtils.isIdCard(idCard);
        if (!idCardFlag) {
            return null;
        }
        String str = idCard.substring(6, 14);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        return format.parse(str);
    }

    /**
     * 获取上个月的第一天
     *
     * @return Date
     * @date 2021/4/22
     */
    public static Date getLastMonthFirstDay() {
        Calendar cal = Calendar.getInstance();
        // 获取当前时间上一个月
        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.DATE, cal.getActualMinimum(Calendar.DATE));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        // 分
        cal.set(Calendar.MINUTE, 0);
        // 秒
        cal.set(Calendar.SECOND, 0);
        // 毫秒
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 获取上个月的最后一天
     *
     * @return Date
     * @date 2021/4/22
     */
    public static Date getLastMonthLastDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, 1);
        calendar.add(Calendar.DATE, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        // 分
        calendar.set(Calendar.MINUTE, 59);
        // 秒
        calendar.set(Calendar.SECOND, 59);
        // 毫秒
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 格式化时间字符串成标准格式
     *
     * @param text 时间字符串
     * @return String
     * @date 2025/11/23
     */
    public static String format(String text) {
        text = text.replaceAll("(?:/|年|月)", "-");
        text = text.replaceAll("(?:时|分)", ":");
        text = text.replace("T", " ");
        text = text.replace("日", " ");
        text = text.replace("秒", "");
        return text.trim();
    }

    public enum TimeTypes {
        /**
         * YEAR: 年
         */
        YEAR("year"),
        /**
         * MONTH: 月
         */
        MONTH("month"),
        /**
         * DAY: 日
         */
        DAY("day"),
        /**
         * HOUR: 小时
         */
        HOUR("hour"),
        /**
         * MINUTES: 分钟
         */
        MINUTES("minutes"),
        /**
         * SECOND: 秒
         */
        SECOND("second"),
        /**
         * MILLISECOND: 毫秒
         */
        MILLISECOND("millisecond");

        private long time;

        private int type;

        TimeTypes(String value) {
            if ("year".equals(value)) {
                this.time = 1000L * 60L * 60L * 24L * 365L;
                this.type = Calendar.YEAR;
            } else if ("month".equals(value)) {
                this.time = 1000L * 60L * 60L * 24L * 30L;
                this.type = Calendar.MONTH;
            } else if ("day".equals(value)) {
                this.time = 1000 * 60 * 60 * 24;
                this.type = Calendar.DATE;
            } else if ("hour".equals(value)) {
                this.time = 1000 * 60 * 60;
                this.type = Calendar.HOUR_OF_DAY;
            } else if ("minutes".equals(value)) {
                this.time = 1000 * 60;
                this.type = Calendar.MINUTE;
            } else if ("second".equals(value)) {
                this.time = 1000;
                this.type = Calendar.SECOND;
            } else if ("millisecond".equals(value)) {
                this.time = 1;
                this.type = Calendar.MILLISECOND;
            }

        }

        /**
         * 获取当前时间类型代表的毫秒数(year - 365天 ， month - 30天)
         *
         * @return int
         * @date 2021/4/22
         */
        public long getTime() {
            return this.time;
        }

        /**
         * 获取当前时间的类型(Calendar常量值)
         *
         * @return int
         * @date 2021/4/22
         */
        public int getType() {
            return this.type;
        }

    }

    public enum TimeFormat {
        /**
         * MONTH: 时间格式：yyyy-MM
         */
        MONTH("month"),
        /**
         * DATE: 时间格式：yyyy-MM-dd
         */
        DATE("date"),
        /**
         * HOUR: 时间格式：yyyy-MM-dd HH
         */
        HOUR("hour"),
        /**
         * TIME: 时间格式：yyyy-MM-dd HH:mm:ss
         */
        DATETIME("date_time");

        private final String timeType;

        private String value;

        TimeFormat(String timeType) {
            this.timeType = timeType;
            if ("month".equals(timeType)) {
                this.value = "yyyy-MM";
            } else if ("date".equals(timeType)) {
                this.value = "yyyy-MM-dd";
            } else if ("date_time".equals(timeType)) {
                this.value = "yyyy-MM-dd HH:mm:ss";
            } else if ("hour".equals(timeType)) {
                this.value = "yyyy-MM-dd HH";
            }
        }

        /**
         * 获取时间类型
         *
         * @return String
         * @date 2021/4/22
         */
        public String getTimeType() {
            return this.timeType;
        }

        /**
         * 获取时间格式
         *
         * @return String
         * @date 2021/4/22
         */
        public String getValue() {
            return this.value;
        }
    }
}
