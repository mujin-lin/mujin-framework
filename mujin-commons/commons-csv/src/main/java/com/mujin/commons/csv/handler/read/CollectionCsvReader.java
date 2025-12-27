package com.mujin.commons.csv.handler.read;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.mujin.commons.csv.constants.CsvHandlerConstants;
import com.mujin.commons.csv.entry.FieldCacheEntry;
import com.mujin.commons.csv.enums.CsvHandlerEnum;
import com.mujin.commons.csv.exception.CsvReadException;
import com.mujin.commons.csv.handler.CsvCollectionAbstractHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 集合类型、数组类型属性处理
 *
 * @author chenglin.wu
 * @date 2025/11/23
 */
public class CollectionCsvReader extends CsvCollectionAbstractHandler {

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> void readCsvData(CsvHandlerEnum csvHandlerEnum, FieldCacheEntry fieldCacheEntry, String columData, T tObject, Function<String, Boolean> boolFunction) throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        if (StrUtil.isBlank(columData)) {
            return;
        }
        Collection collection = CollectionUtil.create(fieldCacheEntry.getFieldClass());
        // 如果可以直接赋值，则使用当前方法直接给集合赋值
        Consumer<String[]> consumer = (columDataArr) -> {
            // 判断是否是boolean类型的集合
            if (fieldCacheEntry.isBoolean()) {
                for (String booleanStr : columDataArr) {
                    Boolean apply = boolFunction.apply(booleanStr);
                    collection.add(apply);
                }
            } else {
                List columnData = CollectionUtil.newArrayList(columDataArr);
                collection.addAll(columnData);
            }
        };
        // 赋值task
        InvokeTask invokeTask = () -> {
            if (fieldCacheEntry.isArray()) {
                Object[] array = collection.toArray();
                Object castArray = ArrayUtil.cast(fieldCacheEntry.getFieldClass(), array);
                try {
                    fieldCacheEntry.getFieldSetter().invoke(tObject, castArray);
                    return;
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                fieldCacheEntry.getFieldSetter().invoke(tObject, collection);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        };

        // 如果没有定义对应泛型类，则默认为String
        Class<?> genericsSubClass = fieldCacheEntry.getGenericsSubClass();

        String[] columDataArr = columData.replace(CsvHandlerConstants.CSV_STR_REPLACE_COMMA, StrUtil.COMMA).split(StrUtil.COMMA);
        if (Objects.isNull(genericsSubClass) || fieldCacheEntry.isBaseDataType() || CsvHandlerConstants.STRING_CLASS_OBJ.equals(genericsSubClass)) {
            consumer.accept(columDataArr);
            invokeTask.execute();
            return;
        }
        // 不为string 的情况
        Method subSetterMethod = fieldCacheEntry.getGenericsSubSetterMethod();
        if (Objects.isNull(subSetterMethod)) {
            throw new CsvReadException("Generics sub setter method cannot be empty!");
        }
        // 如果泛型中的对象不能直接赋值，则需要先创建对象
        for (String str : columDataArr) {
            Object subObj = genericsSubClass.getConstructor().newInstance();
            // 调用子类的赋值方法
            subSetterMethod.invoke(subObj, str);
            collection.add(subObj);
        }
        invokeTask.execute();
    }

    @FunctionalInterface
    interface InvokeTask {

        void execute();

    }

}
