package com.simpleshare.common.utils;

import com.simpleshare.common.constant.Constants;
import java.util.Collection;
import java.util.Map;

/**
 * 字符串工具类
 *
 * @author SimpleShare
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {
    /** 空字符串 */
    private static final String NULLSTR = "";

    /** 下划线 */
    private static final char SEPARATOR = '_';

    /**
     * 获取参数不为空值
     *
     * @param value defaultValue 要判断的value
     * @return value 返回值
     */
    public static <T> T nvl(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * * 判断一个Collection是否为空， 包含List，Set，Queue
     *
     * @param coll 要判断的Collection
     * @return true：为空 false：非空
     */
    public static boolean isEmpty(Collection<?> coll) {
        return isNull(coll) || coll.isEmpty();
    }

    /**
     * * 判断一个Collection是否非空，包含List，Set，Queue
     *
     * @param coll 要判断的Collection
     * @return true：非空 false：空
     */
    public static boolean isNotEmpty(Collection<?> coll) {
        return !isEmpty(coll);
    }

    /**
     * * 判断一个对象数组是否为空
     *
     * @param objects 要判断的对象数组
     * @return true：为空 false：非空
     */
    public static boolean isEmpty(Object[] objects) {
        return isNull(objects) || (objects.length == 0);
    }

    /**
     * * 判断一个对象数组是否非空
     *
     * @param objects 要判断的对象数组
     * @return true：非空 false：空
     */
    public static boolean isNotEmpty(Object[] objects) {
        return !isEmpty(objects);
    }

    /**
     * * 判断一个Map是否为空
     *
     * @param map 要判断的Map
     * @return true：为空 false：非空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return isNull(map) || map.isEmpty();
    }

    /**
     * * 判断一个Map是否为空
     *
     * @param map 要判断的Map
     * @return true：非空 false：空
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * * 判断一个字符串是否为空串
     *
     * @param str String
     * @return true：为空 false：非空
     */
    public static boolean isEmpty(String str) {
        return isNull(str) || NULLSTR.equals(str.trim());
    }

    /**
     * * 判断一个字符串是否为非空串
     *
     * @param str String
     * @return true：非空串 false：空串
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * * 判断一个对象是否为空
     *
     * @param object Object
     * @return true：为空 false：非空
     */
    public static boolean isNull(Object object) {
        return object == null;
    }

    /**
     * * 判断一个对象是否非空
     *
     * @param object Object
     * @return true：非空 false：空
     */
    public static boolean isNotNull(Object object) {
        return !isNull(object);
    }

    /**
     * * 判断一个对象是否是数组类型（Java基本型别的数组）
     *
     * @param object 对象
     * @return true：是数组 false：不是数组
     */
    public static boolean isArray(Object object) {
        return isNotNull(object) && object.getClass().isArray();
    }

    /**
     * 去空格
     */
    public static String trim(String str) {
        return (str == null ? "" : str.trim());
    }

    /**
     * 截取字符串
     *
     * @param str   字符串
     * @param start 开始
     * @return 结果
     */
    public static String substring(final String str, int start) {
        if (str == null) {
            return NULLSTR;
        }

        if (start < 0) {
            start = str.length() + start;
        }

        if (start < 0) {
            start = 0;
        }
        if (start > str.length()) {
            return NULLSTR;
        }

        return str.substring(start);
    }

    /**
     * 截取字符串
     *
     * @param str   字符串
     * @param start 开始
     * @param end   结束
     * @return 结果
     */
    public static String substring(final String str, int start, int end) {
        if (str == null) {
            return NULLSTR;
        }

        if (end < 0) {
            end = str.length() + end;
        }
        if (start < 0) {
            start = str.length() + start;
        }

        if (end > str.length()) {
            end = str.length();
        }

        if (start > end) {
            return NULLSTR;
        }

        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }

        return str.substring(start, end);
    }

    /**
     * 驼峰转下划线命名
     */
    public static String toUnderScoreCase(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        // 前置字符是否大写
        boolean preCharIsUpperCase = true;
        // 当前字符是否大写
        boolean curreCharIsUpperCase = true;
        // 下一字符是否大写
        boolean nexteCharIsUpperCase = true;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (i > 0) {
                preCharIsUpperCase = Character.isUpperCase(str.charAt(i - 1));
            } else {
                preCharIsUpperCase = false;
            }

            curreCharIsUpperCase = Character.isUpperCase(c);

            if (i < (str.length() - 1)) {
                nexteCharIsUpperCase = Character.isUpperCase(str.charAt(i + 1));
            }

            if (preCharIsUpperCase && curreCharIsUpperCase && !nexteCharIsUpperCase) {
                sb.append(SEPARATOR);
            } else if ((i != 0 && !preCharIsUpperCase) && curreCharIsUpperCase) {
                sb.append(SEPARATOR);
            }
            sb.append(Character.toLowerCase(c));
        }

        return sb.toString();
    }

    /**
     * 判断字符串是否在指定的字符串数组中（忽略大小写）
     *
     * @param str 要检查的字符串
     * @param strs 字符串数组
     * @return 如果字符串在数组中返回true，否则返回false
     */
    public static boolean inStringIgnoreCase(String str, String... strs) {
        if (str != null && strs != null) {
            for (String s : strs) {
                if (str.equalsIgnoreCase(trim(s))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断是否为http(s)://开头
     *
     * @param link 链接
     * @return 结果
     */
    public static boolean ishttp(String link)
    {
        return StringUtils.startsWithAny(link, Constants.HTTP, Constants.HTTPS);
    }

    /**
     * 格式化文本, {} 表示占位符<br>
     * 此方法只是简单将占位符 {} 按照顺序替换为参数<br>
     * 如果想输出 {} 使用 \\转义 { 即可，如果想输出 {} 之前的 \\ 使用双转义符 \\\\ 即可<br>
     * 例：<br>
     * 通常使用：format("this is {} for {}", "a", "b") -> this is a for b<br>
     * 转义{}： format("this is \\{} for {}", "a", "b") -> this is \{} for a<br>
     * 转义\： format("this is \\\\{} for {}", "a", "b") -> this is \\a for b<br>
     *
     * @param template 文本模板，被替换的部分用 {} 表示
     * @param params   参数值
     * @return 格式化后的文本
     */
    public static String format(String template, Object... params)
    {
        if (isEmpty(params) || isEmpty(template))
        {
            return template;
        }
        return StrFormatter.format(template, params);
    }

    /**
     * 字符串格式化
     */
    public static class StrFormatter {
        public static final String EMPTY_JSON = "{}";
        public static final char C_BACKSLASH = '\\';
        public static final char C_DELIM_START = '{';
        public static final char C_DELIM_END = '}';

        /**
         * 格式化字符串<br>
         * 此方法只是简单将占位符 {} 按照顺序替换为参数<br>
         * 如果想输出 {} 使用 \\转义 { 即可，如果想输出 {} 之前的 \\ 使用双转义符 \\\\ 即可<br>
         * 例：<br>
         * 通常使用：format("this is {} for {}", "a", "b") -> this is a for b<br>
         * 转义{}： format("this is \\{} for {}", "a", "b") -> this is \{} for a<br>
         * 转义\： format("this is \\\\{} for {}", "a", "b") -> this is \\a for b<br>
         *
         * @param strPattern 字符串模板
         * @param argArray   参数列表
         * @return 结果
         */
        public static String format(final String strPattern, final Object... argArray) {
            if (StringUtils.isEmpty(strPattern) || StringUtils.isEmpty(argArray)) {
                return strPattern;
            }
            final int strPatternLength = strPattern.length();

            // 初始化定义好的长度以获得更好的性能
            StringBuilder sbuf = new StringBuilder(strPatternLength + 50);

            int handledPosition = 0;
            int delimIndex;// 占位符所在位置
            for (int argIndex = 0; argIndex < argArray.length; argIndex++) {
                delimIndex = strPattern.indexOf(EMPTY_JSON, handledPosition);
                if (delimIndex == -1) {
                    if (handledPosition == 0) {
                        return strPattern;
                    } else { // 字符串模板剩余部分不再包含占位符，加入剩余部分后返回结果
                        sbuf.append(strPattern, handledPosition, strPatternLength);
                        return sbuf.toString();
                    }
                } else {
                    if (delimIndex > 0 && strPattern.charAt(delimIndex - 1) == C_BACKSLASH) {
                        if (delimIndex > 1 && strPattern.charAt(delimIndex - 2) == C_BACKSLASH) {
                            // 转义符之前还有一个转义符，占位符依旧有效
                            sbuf.append(strPattern, handledPosition, delimIndex - 1);
                            sbuf.append(utf8Str(argArray[argIndex]));
                            handledPosition = delimIndex + 2;
                        } else {
                            // 占位符被转义
                            argIndex--;
                            sbuf.append(strPattern, handledPosition, delimIndex - 1);
                            sbuf.append(C_DELIM_START);
                            handledPosition = delimIndex + 1;
                        }
                    } else {
                        // 正常占位符
                        sbuf.append(strPattern, handledPosition, delimIndex);
                        sbuf.append(utf8Str(argArray[argIndex]));
                        handledPosition = delimIndex + 2;
                    }
                }
            }
            // 加入最后一个占位符后所有的字符
            sbuf.append(strPattern, handledPosition, strPattern.length());

            return sbuf.toString();
        }

        /**
         * 将对象转为字符串<br>
         *
         * @param obj 对象
         * @return 字符串
         */
        public static String utf8Str(Object obj) {
            return str(obj, "UTF-8");
        }

        /**
         * 将对象转为字符串<br>
         * 1、Byte数组和ByteBuffer会被转换为对应字符串的数组 2、对象数组会调用Arrays.toString方法
         *
         * @param obj     对象
         * @param charset 字符集
         * @return 字符串
         */
        public static String str(Object obj, String charset) {
            if (null == obj) {
                return null;
            }

            if (obj instanceof String) {
                return (String) obj;
            } else if (obj instanceof byte[]) {
                return str((byte[]) obj, charset);
            }
            return obj.toString();
        }

        /**
         * 解码字节码
         *
         * @param data    字符串
         * @param charset 字符集，如果此字段为空，则解码的结果取决于平台
         * @return 解码后的字符串
         */
        public static String str(byte[] data, String charset) {
            if (data == null) {
                return null;
            }

            if (StringUtils.isEmpty(charset)) {
                return new String(data);
            }
            try {
                return new String(data, charset);
            } catch (Exception e) {
                return new String(data);
            }
        }
    }
}