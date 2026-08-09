package com.mujin.cache.caching;

import org.springframework.core.serializer.DefaultDeserializer;
import org.springframework.core.serializer.DefaultSerializer;
import org.springframework.core.serializer.Deserializer;
import org.springframework.core.serializer.Serializer;

/**
 * 简单的本地 jvm 内存配置序列化器
 *
 * @author chenglin.wu
 * @date 2026-05-01
 */
public abstract class AbstractSimpleCaching implements SimpleCacheNameCaching {

    @Override
    public Deserializer<Object> valueDeserializer() {
        return new DefaultDeserializer();
    }

    @Override
    public Serializer<Object> valueSerializer() {
        return new DefaultSerializer();
    }
}
