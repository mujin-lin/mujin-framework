package com.mujin.logging.serializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.mujin.commons.lang.JsonUtil;
import com.mujin.logging.annotations.LogIgnore;
import com.mujin.logging.annotations.LogMask;
import com.mujin.logging.annotations.MaskType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志专用的 JSON 序列化工具
 * <p>
 * 基于 Jackson 的 {@link BeanSerializerModifier} 机制，在序列化器构造阶段识别
 * {@link LogMask} 与 {@link LogIgnore} 注解：
 * <ul>
 *     <li>{@link LogIgnore}：直接过滤该字段，不参与序列化</li>
 *     <li>{@link LogMask}：为该字段分配自定义的 {@link LogMaskSerializer}，
 *         按指定策略（{@code KEEP_HEAD} / {@code KEEP_TAIL} / {@code MIDDLE} / {@code ALL}）
 *         对字符串值进行脱敏</li>
 * </ul>
 * 由于 {@link BeanSerializerModifier} 在 Jackson 序列化每个 Bean 时都会生效，
 * 嵌套对象、集合元素、Map 的值均会自动继承脱敏与忽略规则，无需额外递归逻辑。
 * <p>
 * 该类仅服务于操作日志的入参/出参序列化，复用 {@link JsonUtil#jsonMapper()} 的全局配置，
 * 避免重复构造 {@link ObjectMapper} 带来的性能损耗。
 *
 * @author chenglin.wu
 * @date 2026/08/09
 */
public final class ParamJsonSerializer {

    /**
     * 日志专用的 ObjectMapper（单例）
     */
    private static final ObjectMapper MAPPER = initMapper();

    private ParamJsonSerializer() {
    }

    /**
     * 初始化 ObjectMapper，注册自定义序列化修饰器
     * <p>
     * 复用 {@link JsonUtil#jsonMapper()}：
     * <ul>
     *     <li>JavaTimeModule：支持 LocalDateTime 等 java8 时间类型</li>
     *     <li>{@code FAIL_ON_UNKNOWN_PROPERTIES=false}：反序列化多字段不抛异常</li>
     * </ul>
     * 在此基础上额外配置：
     * <ul>
     *     <li>{@code setVisibility}：放宽字段访问权限，识别 Lombok 生成的非 public 字段/getter</li>
     *     <li>{@code FAIL_ON_EMPTY_BEANS=false}：空对象序列化为 {@code {}}</li>
     *     <li>显式禁用命名策略：保留原始字段名（兜底防御）</li>
     * </ul>
     *
     * @return ObjectMapper 日志专用 mapper
     */
    private static ObjectMapper initMapper() {
        ObjectMapper mapper = JsonUtil.jsonMapper();
        // 允许访问 Lombok 生成的 private/protected 字段与 getter，否则测试类与内部类的非 public 属性无法序列化
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY);
        mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY);
        // 空对象序列化为 {} 而非对象 toString
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 显式禁用 SNAKE_CASE，防止 JsonUtil 后续重新开启导致字段被改名
        mapper.setPropertyNamingStrategy(null);
        SimpleModule module = new SimpleModule("LoggingSerializerModule");
        module.setSerializerModifier(new LoggingBeanSerializerModifier());
        mapper.registerModule(module);
        return mapper;
    }

    /**
     * 将对象序列化为 JSON 字符串，自带 {@link LogMask} 脱敏与 {@link LogIgnore} 忽略处理
     *
     * @param obj 待序列化对象
     * @return String JSON 字符串；入参为 {@code null} 时返回 {@code null}；
     *         序列化失败时降级为 {@link Object#toString()}
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            // 序列化失败不应影响主流程，降级为 toString
            return obj.toString();
        }
    }

    /**
     * 自定义 Bean 序列化修饰器，识别 {@link LogIgnore} 与 {@link LogMask} 注解
     *
     * @author chenglin.wu
     * @date 2026/08/09
     */
    private static class LoggingBeanSerializerModifier extends BeanSerializerModifier {

        @Override
        public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                         BeanDescription beanDesc,
                                                         List<BeanPropertyWriter> beanProperties) {
            List<BeanPropertyWriter> result = new ArrayList<>(beanProperties.size());
            for (BeanPropertyWriter writer : beanProperties) {
                // 1. 处理 @LogIgnore 忽略标记
                if (writer.getAnnotation(LogIgnore.class) != null) {
                    continue;
                }
                // 2. 处理 @LogMask 脱敏标记
                LogMask mask = writer.getAnnotation(LogMask.class);
                if (mask != null) {
                    writer.assignSerializer(new LogMaskSerializer(mask.value(), mask.head(), mask.tail()));
                }
                result.add(writer);
            }
            return result;
        }
    }

    /**
     * 脱敏序列化器：把字符串值按指定策略替换为星号
     *
     * @author chenglin.wu
     * @date 2026/08/09
     */
    private static class LogMaskSerializer extends JsonSerializer<Object> {

        /**
         * 脱敏策略
         */
        private final MaskType maskType;

        /**
         * 保留头部字符数
         */
        private final int head;

        /**
         * 保留尾部字符数
         */
        private final int tail;

        LogMaskSerializer(MaskType maskType, int head, int tail) {
            this.maskType = maskType;
            this.head = head;
            this.tail = tail;
        }

        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }
            gen.writeString(mask(value.toString()));
        }

        /**
         * 对单个字符串应用脱敏规则
         *
         * @param str 原始字符串
         * @return String 脱敏后字符串
         */
        private String mask(String str) {
            if (str == null || str.isEmpty()) {
                return str;
            }
            int len = str.length();
            switch (maskType) {
                case KEEP_HEAD:
                    if (len <= head) {
                        return str;
                    }
                    return str.substring(0, head) + repeatAsterisk(len - head);
                case KEEP_TAIL:
                    if (len <= tail) {
                        return str;
                    }
                    return repeatAsterisk(len - tail) + str.substring(len - tail);
                case MIDDLE:
                    if (len <= head + tail) {
                        return repeatAsterisk(len);
                    }
                    return str.substring(0, head)
                            + repeatAsterisk(len - head - tail)
                            + str.substring(len - tail);
                case ALL:
                default:
                    return repeatAsterisk(len);
            }
        }

        /**
         * 构造 N 个星号的字符串
         *
         * @param count 数量
         * @return String 星号字符串
         */
        private String repeatAsterisk(int count) {
            StringBuilder sb = new StringBuilder(count);
            for (int i = 0; i < count; i++) {
                sb.append('*');
            }
            return sb.toString();
        }
    }
}
