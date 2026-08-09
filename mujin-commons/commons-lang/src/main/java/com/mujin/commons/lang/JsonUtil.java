package com.mujin.commons.lang;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.text.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 自定义的json 格式化类
 *
 * @author chenglin.wu
 * @date 2025/11/23
 */
@SuppressWarnings("unused")
public final class JsonUtil {

    /**
     * 日期时间格式
     */
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /**
     * 日期格式
     */
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    /**
     * json mapper 对象
     */
    private static final JsonMapper JSON_MAPPER = initJsonMapper();

    private JsonUtil() {
    }


    /**
     * 获取json mapper
     *
     * @return JsonMapper
     * @date 2025/11/23
     */
    public static JsonMapper jsonMapper() {
        return JSON_MAPPER.copy();
    }

    /**
     * 获取 请求返回转换的序列化器 json mapper
     *
     * @return JsonMapper
     * @date 2025/11/23
     */
    public static JsonMapper initHttpMessageConvertMapper() {
        // 设置默认的时间格式,设置支持java8时间类型(LocalDateTime)
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_PATTERN)));
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
        // 设置Long 转 String 的序列化和反序列化模块
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);

        return JsonMapper.builder()
                // 包含所有字段
                .serializationInclusion(JsonInclude.Include.ALWAYS)
                //反序列化的时候如果多了其他属性,不抛出异常
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .addModule(javaTimeModule)
                .addModule(simpleModule)
                .defaultDateFormat(new JsonDateTimeFormatter()).build();
    }

    /**
     * 获取一个主实体对应N个子实体结构的JavaType
     * 如：A<T,S,...>
     *
     * @param mainClass      主实体
     * @param elementClasses 子实体
     * @return JavaType 返回结果
     * @date 2025/11/23
     */
    public static JavaType getJavaType(Class<?> mainClass, Class<?>... elementClasses) {
        return JSON_MAPPER.getTypeFactory().constructParametricType(mainClass, elementClasses);
    }

    /**
     * 通过javaType获取新的javaType对象，可以多次调用组成嵌套层级较深的javaType
     *
     * @param mainClass 主类名
     * @param javaType  javaType
     * @return JavaType 返回结果
     * @date 2025/11/23
     */
    public static JavaType getJavaType(Class<?> mainClass, JavaType javaType) {
        return JSON_MAPPER.getTypeFactory().constructParametricType(mainClass, javaType);
    }

    /**
     * 获取一个主实体包含一个列表泛型实体的JavaType
     *
     * @param mainClass  主实体类型
     * @param listClass  列表实体类型
     * @param childClass 子实体类型
     * @return JavaType 返回结果
     * @date 2025/11/23
     */
    public static JavaType getJavaType(Class<?> mainClass, Class<?> listClass, Class<?> childClass) {
        if (listClass.getTypeParameters().length == 0) {
            return JSON_MAPPER.getTypeFactory().constructParametricType(mainClass, listClass, childClass);
        }
        JavaType listType = JSON_MAPPER.getTypeFactory().constructParametricType(listClass, childClass);
        return JSON_MAPPER.getTypeFactory().constructParametricType(mainClass, listType);

    }

    /**
     * 对象转换JSON字符串（转换失败会抛出异常）
     *
     * @param obj 对象
     * @return String
     * @date 2025/11/23
     */
    public static String toJson(Object obj) {
        return toJson(obj, true);
    }

    /**
     * 对象装好为JSON字符串
     *
     * @param obj              待转换对象
     * @param isThrowException 转换失败是否抛出异常
     * @return String
     */
    public static String toJson(Object obj, boolean isThrowException) {
        if (Objects.isNull(obj)) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        try {
            return JSON_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            if (isThrowException) {
                throw new RuntimeException(e);
            }

        }
        return null;
    }

    /**
     * JSON字符串转换为指定的对象（转换失败会抛出异常）
     *
     * @param text JSON字符串
     * @param type 对象类型
     * @return T 转换结果
     * @date 2025/11/23
     */
    public static <T> T toObject(String text, Class<T> type) {
        return toObject(text, type, true);
    }

    /**
     * JSON字符串转换为指定的对象
     *
     * @param text             JSON字符串
     * @param type             对象类型
     * @param isThrowException 转换失败是否抛出异常
     * @return T 转换结果
     * @date 2025/11/23
     */
    public static <T> T toObject(String text, Class<T> type, boolean isThrowException) {

        if (StrUtil.isBlank(text)) {
            return null;
        }
        try {
            return JSON_MAPPER.readValue(text, type);
        } catch (JsonProcessingException e) {
            if (isThrowException) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * JSON字符串转换为指定的对象
     *
     * @param text    json 字符串
     * @param typeRef 转换类型
     * @return T 转换结果
     * @date 2025/11/23
     */
    public static <T> T toObject(String text, TypeReference<T> typeRef) {

        if (StrUtil.isBlank(text)) {
            return null;
        }
        try {
            return JSON_MAPPER.readValue(text, typeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * JSON字符串转换为指定的对象
     *
     * @param text           输入字符串
     * @param mainClass      主class
     * @param elementClasses 元素class
     * @return T 转换结果
     * @date 2025/11/23
     */
    public static <T> T toObject(String text, Class<?> mainClass, Class<?>... elementClasses) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        try {
            JavaType javaType = JSON_MAPPER.getTypeFactory().constructParametricType(mainClass, elementClasses);
            return JSON_MAPPER.readValue(text, javaType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将Json序列化为主实体带list泛型的对象
     *
     * @param text     json字符串
     * @param javaType 序列化类型
     * @return T 转换结果
     * @date 2025/11/23
     */
    public static <T> T toObject(String text, JavaType javaType) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        try {
            return JSON_MAPPER.readValue(text, javaType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 通过 input stream转换为类实例
     *
     * @param inputStream 输入流
     * @param tClass      class对象
     * @throws IOException input stream中为空或已经关闭则会抛出此异常
     */
    public static <T> T toObject(InputStream inputStream, Class<T> tClass) throws IOException {
        return JSON_MAPPER.readValue(inputStream, tClass);
    }

    /**
     * 通过 input stream转换为类实例
     *
     * @param inputStream 输入流
     * @param javaType    java类型
     * @throws IOException input stream中为空或已经关闭则会抛出此异常
     */
    public static <T> T toObject(InputStream inputStream, JavaType javaType) throws IOException {
        return JSON_MAPPER.readValue(inputStream, javaType);
    }

    /**
     * 将一个对象序列化成另一个指定对象
     *
     * @param objParam 源对象
     * @param type     目标对象类型
     * @return T 转换结果
     * @date 2025/11/23
     */
    public static <T> T objToAssign(Object objParam, Class<T> type) {
        String strParam = toJson(objParam);
        return toObject(strParam, type);
    }

    /**
     * 将一个对象序列化成另一个指定对象
     *
     * @param objParam 源对象
     * @param typeRef  目标对象类型
     * @return T 转换结果
     * @date 2025/11/23
     */
    public static <T> T objToAssign(Object objParam, TypeReference<T> typeRef) {
        String strParam = toJson(objParam);
        return toObject(strParam, typeRef);
    }

    /**
     * 将一个对象序列化成另一个指定对象
     *
     * @param objParam 源对象
     * @param type     目标对象类型
     * @return T 转换结果
     * @date 2025/11/23
     */
    public static <E, T> List<T> objToAssign(Collection<E> objParam, Class<T> type) {
        String strParam = toJson(objParam);
        return toList(strParam, type);
    }

    /**
     * 将一个对象序列化之后，再反序列化为另一个指定对象
     *
     * @param objParam 源对象
     * @param javaType javaType
     * @return T 转换结果
     * @date 2025/11/23
     */
    public static <T> T objToAssign(Object objParam, JavaType javaType) {
        String strParam = toJson(objParam);
        return toObject(strParam, javaType);
    }

    /**
     * JSON转换为指定类型的LIST
     *
     * @param text json字符串
     * @param type list 元素类型
     * @return T 转换结果
     * @date 2025/11/23
     */
    public static <T> List<T> toList(String text, Class<T> type) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        try {
            JavaType listType = JSON_MAPPER.getTypeFactory().constructCollectionType(List.class, type);
            return JSON_MAPPER.readValue(text, listType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * JSON字符串转换为Map
     *
     * @param text json 字符串
     * @return Map<String, Object>
     */
    public static Map<String, Object> toMap(String text) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        return toObject(text, new TypeReference<>() {
        });
    }

    /**
     * JSON字符串转换为LinkedHashMap
     *
     * @param text json 字符串
     * @return LinkedHashMap<String, Object>
     */
    public static LinkedHashMap<String, Object> toLinkedMap(String text) {
        return toMap(text, new TypeReference<>() {
        });
    }


    /**
     * JSON字符串转换为JsonNode
     *
     * @param text json 字符串
     * @return JsonNode 转换结果
     */
    public static JsonNode toJsonNode(String text) {
        try {
            return JSON_MAPPER.readTree(text);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取指定属性的JSON字符串值
     *
     * @param root      node 根节点
     * @param fieldName 字段名
     * @return String 获取出来的数据
     */
    public static String getValue(JsonNode root, String fieldName) {
        try {
            JsonNode node = root.get(fieldName);
            if (Objects.isNull(node)) {
                return null;
            }
            if (node.isValueNode()) {
                return node.asText();
            }
            return node.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 初始化json mapper
     *
     * @return JsonMapper
     * @date 2025/11/23
     */
    private static JsonMapper initJsonMapper() {
        // 设置默认的时间格式,设置支持java8时间类型(LocalDateTime)
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_PATTERN)));
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));

        return JsonMapper.builder()
                // 包含所有字段
                .serializationInclusion(JsonInclude.Include.ALWAYS)
                // 配置忽略字段名的大小写
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                //反序列化的时候如果多了其他属性,不抛出异常
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .addModule(javaTimeModule)
                .defaultDateFormat(new JsonDateTimeFormatter()).build();
    }


    /**
     * json util使用的时间格式化
     *
     * @date 2025/11/23
     */
    private static class JsonDateTimeFormatter extends SimpleDateFormat {

        private static SimpleDateFormat initFormats() {
            SimpleDateFormat format = new SimpleDateFormat(JsonUtil.DATE_TIME_PATTERN, Locale.getDefault());
            format.setTimeZone(TimeZone.getDefault());
            return format;
        }

        @Override
        public Date parse(@NonNull String value, @NonNull ParsePosition pos) {
            try {
                return toDate(value, pos);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Date parse(String value) throws ParseException {
            ParsePosition pos = new ParsePosition(0);
            return toDate(value, pos);
        }

        @Override
        public StringBuffer format(@NonNull Date date, @NonNull StringBuffer toAppendTo, @NonNull FieldPosition fieldPosition) {
            DateFormat formatter = initFormats();
            return new StringBuffer(formatter.format(date));
        }

        /**
         * 将字符串转换成 date
         *
         * @param value 转换的值
         * @param pos   the pos
         * @return Date
         * @date 2025/11/23
         */
        private Date toDate(String value, ParsePosition pos) throws ParseException {
            if (StrUtil.isBlank(value)) {
                return null;
            }
            String format = DateTimeUtils.format(value);
            boolean positiveInteger = RegexUtils.isPositiveInteger(value);
            Date date;
            if (positiveInteger) {
                date = new Date(Long.parseLong(value));
            } else {
                date = DateTimeUtils.getDate(format, DateTimeUtils.TimeFormat.DATETIME);
            }
            if (date != null) {
                return date;
            }
            return super.parse(value, pos);
        }
    }

    /**
     * JSON字符串转换为Map
     *
     * @param text          json 字符串
     * @param typeReference 类型
     * @return Map<String, Object>
     */
    private static <M> M toMap(String text, TypeReference<M> typeReference) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        return toObject(text, Objects.nonNull(typeReference) ? typeReference : new TypeReference<>() {
        });
    }
}
