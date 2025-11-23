package com.mujin.commons.csv.entry;

import cn.hutool.core.util.StrUtil;
import com.mujin.commons.csv.annontations.CsvDateFormat;
import com.mujin.commons.csv.annontations.CsvGenerics;
import com.mujin.commons.csv.annontations.CsvProperty;
import com.mujin.commons.csv.constants.CsvHandlerConstants;
import com.mujin.commons.csv.exception.CsvException;
import com.mujin.commons.lang.JsonUtil;
import com.mujin.commons.lang.constants.BaseDataTypeConstants;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

/**
 * csv处理缓存信息，对应目标类的每一个属性
 *
 * @author chenglin.wu
 */
@Data
public class FieldCacheEntry {
    /**
     * 当前类需要作为CSV文件header和值的属性
     */
    private final Field field;
    /**
     * header 别名
     */
    private String headerAlias;
    /**
     * 顺序
     */
    private int order;
    /**
     * 属性的class对象
     */
    private final Class<?> fieldClass;
    /**
     * 当前属性获取值的方式
     */
    private Method valueMethod;
    /**
     * 获取当前属性值的方法
     */
    private Method fieldGetter;
    /**
     * 是否是集合
     */
    private final boolean isCollection;
    /**
     * 是否是基本数据类型和String类型
     */
    private final boolean isBaseDataType;
    /**
     * 是否是boolean类型
     */
    private final boolean isBoolean;
    /**
     * 是否是时间类型
     */
    private final boolean isDate;
    /**
     * 时间格式化类型
     */
    private String datePattern = JsonUtil.DATE_TIME_PATTERN;
    /**
     * time zone
     */
    private TimeZone timeZone = TimeZone.getDefault();
    /**
     * 是否是 Date 类型
     */
    private final boolean isOldDateType;

    /**
     * 调用取值的方法名
     */
    private String getterInvokeMethodVal = "toString";
    /**
     * 是否将当前属性格式化成json格式
     */
    private boolean format2Json;
    // ----------------------------
    //           解析相关属性
    // ----------------------------
    /**
     * 调用设置值的方法名
     */
    private String setterInvokeMethodVal = "";
    /**
     * 设置当前属性值的方法
     */
    private Method fieldSetter;
    /**
     * 是否可以直接赋值
     */
    private boolean isAssignment;
    /**
     * 是否是数组
     */
    private boolean isArray;

    /**
     * 泛型中的泛型类型
     */
    private Class<?> genericsSubClass;
    /**
     * 泛型主类设置泛型子类的方法，对应 集合的 add 数组直接赋值
     */
    private Method genericsMainSetterMethod;
    /**
     * 子类数据的赋值方法
     */
    private Method genericsSubSetterMethod;

    private boolean collectionGenericsIsBoolean;


    public FieldCacheEntry(Field field) throws NoSuchMethodException {
        this.fieldClass = field.getType();
        String simpleName = this.fieldClass.getSimpleName();
        this.isBoolean = BaseDataTypeConstants.isBoolean(simpleName);
        this.isBaseDataType = BaseDataTypeConstants.containsType(simpleName) || String.class.equals(this.fieldClass);
        this.isDate = CsvHandlerConstants.isDate(fieldClass);
        this.field = field;
        this.isOldDateType = this.fieldClass.equals(Date.class);
        this.isCollection = this.fieldClass.isArray() || Collection.class.isAssignableFrom(this.fieldClass);
        initData();
        timeDataFormat();
        genericsData();
    }

    /**
     * 初始化数据
     *
     * @throws NoSuchMethodException 未找到当前定义的调用方法
     * @date 2025/11/23
     */
    private void initData() throws NoSuchMethodException {
        CsvProperty property = field.getAnnotation(CsvProperty.class);

        int order = Integer.MIN_VALUE;
        // 配置不为空则取配置的数据
        if (!Objects.isNull(property)) {
            order = property.index();
            String value = property.value();
            // 配置别名不为空，则取当前别名
            if (StrUtil.isNotBlank(value)) {
                this.headerAlias = value;
            }
            this.order = order;
            if (!this.isBaseDataType && !this.isCollection) {
                this.valueMethod = fieldClass.getMethod(property.dataInvokeMethod());
            }
            this.getterInvokeMethodVal = property.dataInvokeMethod();
            this.setterInvokeMethodVal = property.dataSetInvokeMethod();
            this.format2Json = property.formatJson();
            return;
        }
        // 配置别名为空，则属性名
        this.headerAlias = field.getName();
        this.order = order;
        if (!this.isBaseDataType && !this.isCollection) {
            this.valueMethod = fieldClass.getMethod("toString");
        }
    }

    /**
     * 时间格式化参数匹配
     *
     * @date 2025/11/23
     */
    private void timeDataFormat() {
        if (!this.isDate) {
            return;
        }
        CsvDateFormat annotation = field.getAnnotation(CsvDateFormat.class);
        boolean hasFormatAnnotation = Objects.nonNull(annotation);
        boolean hasFormatPattern = hasFormatAnnotation && StrUtil.isNotBlank(annotation.pattern());
        boolean hasFormatTimeZone = Objects.nonNull(annotation) && StrUtil.isNotBlank(annotation.timeZone());
        boolean isTime = this.fieldClass.getSimpleName().toLowerCase().contains("time");

        // 是否包含时分秒 先给默认值
        if (isTime) {
            this.datePattern = JsonUtil.DATE_TIME_PATTERN;
        } else {
            this.datePattern = JsonUtil.DATE_PATTERN;
        }
        if (hasFormatPattern) {
            this.datePattern = annotation.pattern();
        }
        if (hasFormatTimeZone) {
            this.timeZone = TimeZone.getTimeZone(annotation.timeZone());
            return;
        }
        this.timeZone = TimeZone.getDefault();
    }

    /**
     * 泛型相关注解数据初始化
     *
     * @date 2025/11/23
     */
    private void genericsData() throws NoSuchMethodException {
        CsvGenerics generics = this.field.getAnnotation(CsvGenerics.class);
        if (Objects.isNull(generics)) {
            return;
        }
        // 判断主泛型类是否为空
        if (Objects.isNull(generics.elementClass())) {
            throw new CsvException("Field " + this.field.getName() + " has no generics.elementClass");
        }
        this.genericsSubClass = generics.elementClass();
        this.isArray = this.fieldClass.isArray();
        // 处理数组信息
        if (isArray) {
            this.arrayGenericsData(generics);
            return;
        }
        // 是否是集合
        if (Collection.class.isAssignableFrom(this.fieldClass)) {
            this.genericsMainSetterMethod = this.fieldClass.getMethod("add", Object.class);
            if (!this.isAssignment) {
                this.genericsSubSetterMethod = this.genericsSubClass.getMethod(generics.elementSetterMethod(), String.class);
            }
            return;
        }
        // 其他类型 只处理当前主类包含泛型类实例的


    }

    /**
     * 处理 array的初始化数据 数组也是先使用集合再将集合转成数组
     *
     * @param generics 注解信息
     * @date 2025/11/23
     */
    private void arrayGenericsData(CsvGenerics generics) throws NoSuchMethodException {
        // 是否是基本数据类型或者是String 类型
        this.isAssignment = BaseDataTypeConstants.containsType(this.genericsSubClass.getSimpleName()) || CsvHandlerConstants.STRING_CLASS_OBJ.equals(this.genericsSubClass);
        if (!this.isAssignment) {
            this.genericsSubSetterMethod = this.genericsSubClass.getMethod(generics.elementSetterMethod(), String.class);
        }
    }
}

