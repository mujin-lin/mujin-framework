package com.mujin.commons.csv;


import cn.hutool.core.util.StrUtil;
import com.mujin.commons.csv.config.BoolSupplierConfig;
import com.mujin.commons.csv.config.CsvHandlerConfig;
import com.mujin.commons.csv.constants.CsvHandlerConstants;
import com.mujin.commons.csv.factory.CsvHandlerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 对应操作util
 *
 * @author chenglin.wu
 */
@SuppressWarnings("unused")
public final class CsvOperateUtil {
    /**
     * 私有构造
     */
    private CsvOperateUtil() {
    }

    /**
     * 默认配置
     */
    private static final CsvHandlerConfig DEFAULT_CSV_READ_OBJ_CONFIG = new CsvHandlerConfig();
    /**
     * 默认通过 字符串转boolean的函数方法
     */
    private static final Function<String, Boolean> BOOLEAN_FUNCTION = (boolStr) -> StrUtil.equals(CsvHandlerConstants.BOOLEAN_TRUE_NUM, boolStr);


    /**
     * 当获取出来的数据为boolean时默认的处理数据
     */
    private static final Supplier<BoolSupplierConfig> DEFAULT_SUPPLIER = () -> new BoolSupplierConfig(CsvHandlerConstants.BOOLEAN_TRUE_NUM, CsvHandlerConstants.BOOLEAN_FALSE_NUM, CsvHandlerConstants.BOOLEAN_FALSE_NUM);

    static {
        DEFAULT_CSV_READ_OBJ_CONFIG.setCharset(StandardCharsets.UTF_8);
        DEFAULT_CSV_READ_OBJ_CONFIG.setDelimiter(StrUtil.COMMA);
        DEFAULT_CSV_READ_OBJ_CONFIG.setHeaderLine(1);
        DEFAULT_CSV_READ_OBJ_CONFIG.setDataStartLine(2);
    }

    /**
     * 获取csv 文件字符串分割格式
     *
     * @param objs 对象集合
     * @return String
     * @date 2025/11/23
     */
    public static <T> String writeObj2String(Collection<T> objs) throws NoSuchMethodException {
        return CsvHandlerFactory.writeCsvStr(objs, DEFAULT_SUPPLIER);
    }

    /**
     * 获取csv 文件字符串分割格式
     *
     * @param objs         对象集合
     * @param boolSupplier 获取boolean转换Str的配置
     * @return String
     * @date 2025/11/23
     */
    public static <T> String writeObj2String(Collection<T> objs, Supplier<BoolSupplierConfig> boolSupplier) throws NoSuchMethodException {
        return CsvHandlerFactory.writeCsvStr(objs, boolSupplier);
    }

    //region 集合传递解析成文件

    /**
     * 将类对象解析成 csv 文件，通过输出流输出
     *
     * @param outputStream 输出流
     * @param objs         解析对象数据
     * @date 2025/11/23
     */
    public static <T> void write2File(OutputStream outputStream, Collection<T> objs) throws IOException, NoSuchMethodException {
        CsvHandlerFactory.write2File(outputStream, StandardCharsets.UTF_8, DEFAULT_SUPPLIER, objs);
    }

    /**
     * 将类对象解析成 csv 文件，通过输出流输出
     *
     * @param outputStream 输出流
     * @param objs         解析对象数据
     * @date 2025/11/23
     */
    public static <T> void write2File(OutputStream outputStream, Collection<T> objs, Supplier<BoolSupplierConfig> boolTranStrSupplier) throws IOException, NoSuchMethodException {
        CsvHandlerFactory.write2File(outputStream, StandardCharsets.UTF_8, boolTranStrSupplier, objs);
    }

    /**
     * 将类对象解析成 csv 文件，通过输出流输出
     *
     * @param writer 输出流
     * @param objs   解析对象数据
     * @date 2025/11/23
     */
    public static <T> void write2File(Writer writer, Collection<T> objs) throws IOException, NoSuchMethodException {
        CsvHandlerFactory.write2File(writer, DEFAULT_SUPPLIER, objs);
    }

    /**
     * 将类对象解析成 csv 文件，通过输出流输出
     *
     * @param writer 输出流
     * @param objs   解析对象数据
     * @date 2025/11/23
     */
    public static <T> void write2File(Writer writer, Collection<T> objs, Supplier<BoolSupplierConfig> boolTranStrSupplier) throws IOException, NoSuchMethodException {
        CsvHandlerFactory.write2File(writer, boolTranStrSupplier, objs);
    }

    /**
     * 将对象数据转换成对应的csv文件
     *
     * @param pathName 文件路径加文件名
     * @param objs     转换的对象数据
     * @date 2025/11/23
     */
    public static <T> void write2File(String pathName, Collection<T> objs) throws IOException, NoSuchMethodException {
        try (OutputStream outputStream = Files.newOutputStream(Paths.get(pathName))) {
            write2File(outputStream, objs);
        }
    }

    /**
     * 将对象数据转换成对应的csv文件
     *
     * @param pathName 文件路径加文件名
     * @param objs     转换的对象数据
     * @date 2025/11/23
     */
    public static <T> void write2File(String pathName, Collection<T> objs, Supplier<BoolSupplierConfig> boolTranStrSupplier) throws IOException, NoSuchMethodException {
        try (OutputStream outputStream = Files.newOutputStream(Paths.get(pathName))) {
            write2File(outputStream, objs, boolTranStrSupplier);
        }
    }


    /**
     * 将对象数据转换成对应的csv文件
     *
     * @param path     文件路径
     * @param fileName 文件名
     * @param objs     转换的对象数据
     * @date 2025/11/23
     */
    public static <T> void write2File(String path, String fileName, Collection<T> objs) throws IOException, NoSuchMethodException {
        try (OutputStream outputStream = Files.newOutputStream(Paths.get(path + fileName))) {
            write2File(outputStream, objs);
        }
    }

    /**
     * 将对象数据转换成对应的csv文件
     *
     * @param path     文件路径
     * @param fileName 文件名
     * @param objs     转换的对象数据
     * @date 2025/11/23
     */
    public static <T> void write2File(String path, String fileName, Collection<T> objs, Supplier<BoolSupplierConfig> boolTranStrSupplier) throws IOException, NoSuchMethodException {
        try (OutputStream outputStream = Files.newOutputStream(Paths.get(path + fileName))) {
            write2File(outputStream, objs, boolTranStrSupplier);
        }
    }
    // endregion


    /**
     * 将 csv 格式的字符串读取成对应的类的集合
     *
     * @param csvStr csv 格式字符串
     * @param tClass class数据
     * @return List<T>
     * @date 2025/11/23
     */
    public static <T> List<T> readStr2Obj(String csvStr, Class<T> tClass) throws Exception {
        return CsvHandlerFactory.readStr(csvStr, tClass, DEFAULT_CSV_READ_OBJ_CONFIG, BOOLEAN_FUNCTION);
    }


    /**
     * 将 csv 格式的字符串读取成对应的类的集合
     *
     * @param inputStream 输入流
     * @param tClass      class数据
     * @return List<T>
     * @date 2025/11/23
     */
    public static <T> List<T> readStr2Obj(InputStream inputStream, Class<T> tClass) throws Exception {
        return CsvHandlerFactory.readStream(inputStream, tClass, DEFAULT_CSV_READ_OBJ_CONFIG, BOOLEAN_FUNCTION);
    }

    /**
     * 将 csv 格式的字符串转换成对应的类的集合
     *
     * @param csvStr        csv 字符串
     * @param tClass        类数据
     * @param csvReadConfig 配置
     * @return List<T>
     * @date 2025/11/23
     */
    public static <T> List<T> readStr2Obj(String csvStr, Class<T> tClass, CsvHandlerConfig csvReadConfig) throws Exception {
        return CsvHandlerFactory.readStr(csvStr, tClass, csvReadConfig, BOOLEAN_FUNCTION);
    }

    /**
     * 通过输入流读取成对应的目标类集合
     *
     * @param inputStream 输入流
     * @param tClass      目标类
     * @return List<T>
     * @date 2025/11/23
     */
    public static <T> List<T> readInputStream2Obj(InputStream inputStream, Class<T> tClass) throws Exception {
        return readInputStream2Obj(inputStream, tClass, DEFAULT_CSV_READ_OBJ_CONFIG, BOOLEAN_FUNCTION);
    }

    /**
     * 将 csv 格式的字符串转换成对应的类的集合
     *
     * @param inputStream   输入流
     * @param tClass        类数据
     * @param csvReadConfig 配置
     * @return List<T>
     * @date 2025/11/23
     */
    public static <T> List<T> readInputStream2Obj(InputStream inputStream, Class<T> tClass, CsvHandlerConfig csvReadConfig) throws Exception {
        return CsvHandlerFactory.readStream(inputStream, tClass, csvReadConfig, BOOLEAN_FUNCTION);
    }

    /**
     * 将 csv 格式的字符串转换成对应的类的集合
     *
     * @param inputStream  输入流
     * @param tClass       类数据
     * @param boolFunction boolean 类型替换
     * @return List<T>
     * @date 2025/11/23
     */
    public static <T> List<T> readInputStream2Obj(InputStream inputStream, Class<T> tClass, Function<String, Boolean> boolFunction) throws Exception {
        return CsvHandlerFactory.readStream(inputStream, tClass, DEFAULT_CSV_READ_OBJ_CONFIG, boolFunction);
    }

    /**
     * 将 csv 格式的字符串转换成对应的类的集合
     *
     * @param inputStream   输入流
     * @param tClass        类数据
     * @param csvReadConfig 配置
     * @param boolFunction  boolean 类型替换
     * @return List<T>
     * @date 2025/11/23
     */
    public static <T> List<T> readInputStream2Obj(InputStream inputStream, Class<T> tClass, CsvHandlerConfig csvReadConfig, Function<String, Boolean> boolFunction) throws Exception {
        return CsvHandlerFactory.readStream(inputStream, tClass, csvReadConfig, boolFunction);
    }


    /**
     * 通过csv 文件读取成目标类
     *
     * @param csvFile csv 文件
     * @param tClass  目标类
     * @return List<T>
     * @date 2025/11/23
     */
    public static <T> List<T> readFile2Obj(File csvFile, Class<T> tClass) throws Exception {
        try (
                InputStream fileInputStream = Files.newInputStream(csvFile.toPath())
        ) {
            return readInputStream2Obj(fileInputStream, tClass, DEFAULT_CSV_READ_OBJ_CONFIG, BOOLEAN_FUNCTION);
        }
    }

    /**
     * 通过csv 文件读取成目标类
     *
     * @param csvFile          csv 文件
     * @param tClass           目标类
     * @param csvHandlerConfig reader 配置
     * @return List<T>
     * @date 2025/11/23
     */
    public static <T> List<T> readFile2Obj(File csvFile, Class<T> tClass, CsvHandlerConfig csvHandlerConfig) throws Exception {
        try (
                InputStream fileInputStream = Files.newInputStream(csvFile.toPath())
        ) {
            return readInputStream2Obj(fileInputStream, tClass, csvHandlerConfig, BOOLEAN_FUNCTION);
        }
    }

    /**
     * 通过csv 文件读取成目标类
     *
     * @param csvFile      csv 文件
     * @param tClass       目标类
     * @param boolFunction boolean转换function
     * @return List<T>
     * @date 2025/11/23
     */
    public static <T> List<T> readFile2Obj(File csvFile, Class<T> tClass, Function<String, Boolean> boolFunction) throws Exception {
        try (
                InputStream fileInputStream = Files.newInputStream(csvFile.toPath())
        ) {
            return readInputStream2Obj(fileInputStream, tClass, boolFunction);
        }
    }

    /**
     * 通过csv 文件读取成目标类
     *
     * @param csvFile          csv 文件
     * @param tClass           目标类
     * @param csvHandlerConfig reader 配置
     * @param boolFunction     boolean转换function
     * @return List<T>
     * @date 2025/11/23
     */
    public static <T> List<T> readFile2Obj(File csvFile, Class<T> tClass, CsvHandlerConfig csvHandlerConfig, Function<String, Boolean> boolFunction) throws Exception {
        try (
                InputStream fileInputStream = Files.newInputStream(csvFile.toPath())
        ) {
            return readInputStream2Obj(fileInputStream, tClass, csvHandlerConfig, boolFunction);
        }
    }


}
