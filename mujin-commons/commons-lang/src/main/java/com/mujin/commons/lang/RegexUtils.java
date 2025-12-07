package com.mujin.commons.lang;


import cn.hutool.core.util.StrUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式工具类
 *
 * @title: RegexUtils
 * @date 2021年06月03日
 */
@SuppressWarnings("ALL")
public final class RegexUtils {
    /**
     * 私有化构造方法
     */
    private RegexUtils() {
    }

    /**
     * 身份证的正则
     */
    private static final String ID_CARD_REG = "(^[1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$)|(^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{2}$)";

    /**
     * 手机号的正则
     */
    private static final String PHONE = "^1(3[0-9]|4[01456879]|5[0-35-9]|6[2567]|7[0-8]|8[0-9]|9[0-35-9])\\d{8}$";
    /**
     * 邮箱的正则
     */
    private static final String E_MAIL = "^([a-zA-Z0-9]+[_|_|\\-|.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|_|.]?)*[a-zA-Z0-9]+\\.[a-zA-Z]{2,3}$";

    /**
     * 数字的正则
     */
    public static String INT_REGEX = "^[-\\+]?[\\d]*$";
    /**
     * 浮点数
     */
    public static String FLOAT_REGEX = "^[-\\+]?[\\d]*\\.[\\d]*$";
    /**
     * 整数和浮点数
     */
    public static String NUMBER_REGEX = "(^[+-]?[0-9]+)|(^[+-]?[0-9]+\\.[0-9]+)";
    /**
     * 正整数的正则
     */
    private static final String POSITIVE_INTEGER = "[0-9]\\d*";
    /**
     * 查找双引号之间数据的正则表达式
     */
    private static final Pattern DOUBLE_QUOTE = Pattern.compile("\"(.*?)\"");
    /**
     * 查找json 集合类型的正则表达式
     */
    private static final Pattern JSON_LIST = Pattern.compile("\"\\[(.*?)\\)\"");
    /**
     * 查找json 对象类型的正则表达式
     */
    private static final Pattern JSON_OBJ = Pattern.compile("\"\\{(.*?)}\"");

    /**
     * 判断身份证号是否有误
     *
     * @return boolean
     * @param: idCard 身份证号
     * @date 2021-06-03
     */
    public static boolean isIdCard(String idCard) {
        if (StrUtil.isBlank(idCard)) {
            return false;
        }
        return Pattern.matches(ID_CARD_REG, idCard);
    }

    /**
     * 判断手机号是否有误
     *
     * @return boolean
     * @param: phone 手机号
     * @date 2021-06-03
     */
    public static boolean isPhone(String phone) {
        if (StrUtil.isBlank(phone)) {
            return false;
        }
        return Pattern.matches(PHONE, phone);
    }

    /**
     * 判断邮箱是否有误
     *
     * @return boolean
     * @param: eMail 邮箱号
     * @date 2021-06-03
     */
    public static boolean isEMail(String eMail) {
        if (StrUtil.isBlank(eMail)) {
            return false;
        }
        return Pattern.matches(E_MAIL, eMail);
    }

    /**
     * 判断是否是数字
     *
     * @param number number
     * @return boolean
     * @date 2025/11/23
     */
    public static boolean isNumber(String number) {
        if (StrUtil.isBlank(number)) {
            return false;
        }
        return Pattern.matches(NUMBER_REGEX, number);
    }

    /**
     * 判断是否是数字
     *
     * @param number number
     * @return boolean
     * @date 2025/11/23
     */
    public static boolean isFloat(String number) {
        if (StrUtil.isBlank(number)) {
            return false;
        }
        number = number.toLowerCase().replaceAll("[d|f]",StrUtil.EMPTY);
        return Pattern.matches(FLOAT_REGEX, number);
    }

    /**
     * 判断是否是数字
     *
     * @param number number
     * @return boolean
     * @date 2025/11/23
     */
    public static boolean isInt(String number) {
        if (StrUtil.isBlank(number)) {
            return false;
        }
        number = number.toLowerCase().replaceAll("[d|f|l]",StrUtil.EMPTY);
        return Pattern.matches(INT_REGEX, number);
    }

    /**
     * 判断是否是正整数
     *
     * @param number number
     * @return boolean
     * @date 2025/11/23
     */
    public static boolean isPositiveInteger(String text) {
        if (StrUtil.isBlank(text)) {
            return false;
        }
        return Pattern.matches(POSITIVE_INTEGER, text);
    }

    /**
     * 判断当前字符串是否包含双引号
     *
     * @param text 字符串
     * @return boolean
     * @date 2025/11/23
     */
    public static boolean isDoubleQuote(String text) {
        if (StrUtil.isBlank(text)) {
            return false;
        }
        return doubleQuoteMatcher(text).matches();
    }

    /**
     * 判断当前字符串是否是json list
     *
     * @param text 被检测字符串
     * @return boolean
     * @date 2025/11/23
     */
    public static boolean isJsonList(String text) {
        if (StrUtil.isBlank(text)) {
            return false;
        }
        return jsonListMatcher(text).matches();
    }

    /**
     * 判断当前字符串是否是json独享
     *
     * @param text 被检测字符串
     * @return boolean
     * @date 2025/11/23
     */
    public static boolean isJsonObj(String text) {
        if (StrUtil.isBlank(text)) {
            return false;
        }
        return jsonObjMatcher(text).matches();
    }

    /**
     * 获取双引号的matcher
     *
     * @param text 被检测字符串
     * @return Matcher
     * @date 2025/11/23
     */
    public static Matcher doubleQuoteMatcher(String text) {
        return DOUBLE_QUOTE.matcher(text);
    }

    /**
     * 获取json list的matcher
     *
     * @param text 被检测字符串
     * @return Matcher
     * @date 2025/11/23
     */
    public static Matcher jsonListMatcher(String text) {
        return JSON_LIST.matcher(text);
    }

    /**
     * 获取json对象的matcher
     *
     * @param text 被检测字符串
     * @return Matcher
     * @date 2025/11/23
     */
    public static Matcher jsonObjMatcher(String text) {
        return JSON_OBJ.matcher(text);
    }


}
