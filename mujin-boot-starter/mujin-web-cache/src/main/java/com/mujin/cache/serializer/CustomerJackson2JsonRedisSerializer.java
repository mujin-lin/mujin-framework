package com.mujin.cache.serializer;


import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.mujin.commons.lang.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.StandardCharsets;

/**
 * Redis 使用 jackson 序列化
 *
 * @author chenglin.wu
 */
@Slf4j
@SuppressWarnings("unused")
public class CustomerJackson2JsonRedisSerializer<T> implements RedisSerializer<T> {

    /**
     * 需要序列化的对象
     */
    private final Class<T> clazz;


    public CustomerJackson2JsonRedisSerializer(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(T t) {
        if (t == null) {
            return new byte[0];
        }
        String str = JsonUtil.toJson(t);
        return str.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public T deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return JsonUtil.toObject(new String(bytes, StandardCharsets.UTF_8), clazz);
        } catch (Exception e) {
            log.debug("反序列化对象失败:", e);
            throw new SerializationException("反序列化对象失败");
        }
    }

    protected JavaType getJavaType(Class<?> clazz) {
        return TypeFactory.defaultInstance().constructType(clazz);
    }
}
