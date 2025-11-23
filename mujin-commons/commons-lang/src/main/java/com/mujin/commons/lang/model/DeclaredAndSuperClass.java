package com.mujin.commons.lang.model;


import cn.hutool.core.collection.CollectionUtil;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取当前类和当前类父级的class对象及其所有的属性
 *
 * @author chenglin.wu
 */
public class DeclaredAndSuperClass {

    private final Class<?> declaredClass;

    private final Class<?> superClass;

    public DeclaredAndSuperClass(Class<?> declaredClass) {
        this.declaredClass = declaredClass;
        this.superClass = declaredClass.getSuperclass();
    }

    /**
     * 获取本类和父类所有的属性
     *
     * @return List<Field>
     */
    public List<Field> getAllFields() {
        List<Field> fields = new ArrayList<>();
        Field[] declaredFields = declaredClass.getDeclaredFields();
        Field[] superFields = superClass.getDeclaredFields();
        fields.addAll(CollectionUtil.newArrayList(superFields));
        fields.addAll(CollectionUtil.newArrayList(declaredFields));
        return fields;
    }

    /**
     * 获取本类的所有属性
     *
     * @return List<Field>
     */
    public List<Field> getDeclaredFields() {
        Field[] declaredFields = declaredClass.getDeclaredFields();
        return CollectionUtil.newArrayList(declaredFields);
    }

    /**
     * 获取父类的所有属性
     *
     * @return List<Field>
     */
    public List<Field> getSuperFields() {
        Field[] declaredFields = superClass.getDeclaredFields();
        return CollectionUtil.newArrayList(declaredFields);
    }

    /**
     * 通过方法名获取方法
     *
     * @param methodName 方法名
     * @return Method
     * @date 2025/11/23
     */
    public Method getMethod(String methodName,Class<?>... params) throws NoSuchMethodException {
        return declaredClass.getMethod(methodName,params);
    }
}
