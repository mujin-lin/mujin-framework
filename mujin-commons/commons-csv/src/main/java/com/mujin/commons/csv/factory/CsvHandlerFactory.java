package com.mujin.commons.csv.factory;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import com.mujin.commons.csv.annontations.CsvIgnore;
import com.mujin.commons.csv.config.BoolSupplierConfig;
import com.mujin.commons.csv.config.CsvHandlerConfig;
import com.mujin.commons.csv.constants.CsvHandlerConstants;
import com.mujin.commons.csv.entry.FieldCacheEntry;
import com.mujin.commons.csv.enums.CsvHandlerEnum;
import com.mujin.commons.csv.exception.CsvException;
import com.mujin.commons.csv.exception.CsvWriteException;
import com.mujin.commons.csv.handler.CsvReadHandler;
import com.mujin.commons.csv.handler.CsvWriteHandler;
import com.mujin.commons.csv.handler.read.*;
import com.mujin.commons.csv.handler.write.*;
import com.mujin.commons.lang.RegexUtils;
import com.mujin.commons.lang.constants.IntConstants;
import com.mujin.commons.lang.model.DeclaredAndSuperClass;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * 处理csv数据时的factory
 *
 * @author chenglin.wu
 */
@Slf4j
@SuppressWarnings("unused")
public class CsvHandlerFactory {

    /**
     * 写出数据为 csv 文件或字符串的handler
     */
    private static final Map<CsvHandlerEnum, CsvWriteHandler> WRITE_HANDLER = new HashMap<>();
    /**
     * 用户自定义写出的handler
     */
    private static final Map<CsvHandlerEnum, CsvWriteHandler> CUSTOMER_WRITE_HANDLER = new HashMap<>();

    /**
     * 读取数据为类的handler
     */
    private static final Map<CsvHandlerEnum, CsvReadHandler> READ_HANDLER = new HashMap<>();
    /**
     * 用户自定义读取的handler
     */
    private static final Map<CsvHandlerEnum, CsvReadHandler> CUSTOMER_READ_HANDLER = new HashMap<>();


    /**
     * 对象class 与 csv 文件头的缓存
     */
    private static final Map<Class<?>, String> HEADERS_CACHE = new ConcurrentHashMap<>();

    /**
     * 对象class 与 csv 文件展示的数据获取方式缓存
     */
    private static final Map<Class<?>, List<FieldCacheEntry>> FIELD_CACHE = new ConcurrentHashMap<>();
    /**
     * 对象class 所有 header 名，加filed属性缓存
     */
    private static final Map<Class<?>, Map<String, FieldCacheEntry>> FIELD_CACHE_HEADER_ENTRY = new ConcurrentHashMap<>();

    static {
        initWriterMapping();
        initReaderMapping();
    }

    // region 供给外部接口，方便自定义handler

    /**
     * 设置用户自定义的映射
     *
     * @param handlerEnum  the handlerEnum
     * @param writeHandler the writeHandler
     * @date 2025/11/23
     */
    public static void putValueMapping(CsvHandlerEnum handlerEnum, CsvWriteHandler writeHandler) {
        CUSTOMER_WRITE_HANDLER.put(handlerEnum, writeHandler);
    }

    /**
     * 设置用户自定义的映射
     *
     * @param writeHandlerMap 添加处理的mapping
     * @date 2025/11/23
     */
    public static void putAllValueMapping(Map<CsvHandlerEnum, CsvWriteHandler> writeHandlerMap) {
        CUSTOMER_WRITE_HANDLER.putAll(writeHandlerMap);
    }


    /**
     * 设置用户自定义的映射
     *
     * @param handlerEnum    the handlerEnum
     * @param csvReadHandler the csvReadHandler
     * @date 2025/11/23
     */
    public static void putReaderMapping(CsvHandlerEnum handlerEnum, CsvReadHandler csvReadHandler) {
        CUSTOMER_READ_HANDLER.put(handlerEnum, csvReadHandler);
    }

    /**
     * 设置用户自定义的映射
     *
     * @param readHandlerMap 添加处理的mapping
     * @date 2025/11/23
     */
    public static void putAllReaderMapping(Map<CsvHandlerEnum, CsvReadHandler> readHandlerMap) {
        CUSTOMER_READ_HANDLER.putAll(readHandlerMap);
    }
    // endregion


    // region 通过集合写出数据相关方法

    /**
     * 将集合数据转换成csv文件，通过传入 stream进行写出，没有关闭流
     *
     * @param outputStream        输出流
     * @param charset             字符编码集
     * @param boolTranStrSupplier 如果为boolean 应该怎么处理
     * @param objs                数据集合
     * @date 2025/11/23
     */
    public static <T> void write2File(OutputStream outputStream, Charset charset, Supplier<BoolSupplierConfig> boolTranStrSupplier, Collection<T> objs) throws NoSuchMethodException, IOException {
        String csvStrInfo = writeCsvStr(objs, boolTranStrSupplier);
        if (StrUtil.isBlank(csvStrInfo)) {
            return;
        }
        outputStream.write(csvStrInfo.getBytes(charset));
    }

    /**
     * 将集合数据转换成csv文件，通过传入 Writer进行写出，没有关闭流
     *
     * @param writer              输出流
     * @param boolTranStrSupplier 如果为boolean 应该怎么处理
     * @param objs                数据集合
     * @date 2025/11/23
     */
    public static <T> void write2File(Writer writer, Supplier<BoolSupplierConfig> boolTranStrSupplier, Collection<T> objs) throws NoSuchMethodException, IOException {
        String csvStrInfo = writeCsvStr(objs, boolTranStrSupplier);
        if (StrUtil.isBlank(csvStrInfo)) {
            return;
        }
        char[] charArr = new char[csvStrInfo.length()];
        csvStrInfo.getChars(0, csvStrInfo.length(), charArr, 0);
        writer.write(charArr);
    }


    /**
     * 获取csv 文件字符串分割格式
     *
     * @param objs 对象集合
     * @return String
     * @throws NoSuchMethodException 调用方法未找到
     * @date 2025/11/23
     */
    public static <T> String writeCsvStr(Collection<T> objs, Supplier<BoolSupplierConfig> boolTranStrSupplier) throws NoSuchMethodException {
        if (CollectionUtil.isEmpty(objs)) {
            return StrUtil.EMPTY;
        }

        T t = objs.stream().findFirst().orElse(null);

        if (Objects.isNull(t)) {
            return StrUtil.EMPTY;
        }

        // 返回的数据
        StringBuilder sb = new StringBuilder();
        // headers
        List<FieldCacheEntry> cacheEntries = headers(t.getClass(), sb);
        // 如果为null
        if (Objects.isNull(boolTranStrSupplier)) {
            throw new CsvException("boolean trans cannot be empty");
        }

        // values
        values(objs, cacheEntries, sb, boolTranStrSupplier);

        return sb.toString();
    }

    // endregion

    // region 读取成目标对象集合

    /**
     * 将csv 格式的字符串读取成指定 类型的集合
     *
     * @param csvStr        csv 格式字符串
     * @param tClass        指定类的class对象
     * @param csvReadConfig 配置
     * @return List<T>
     * @date 2025/11/23
     */
    public static <T> List<T> readStr(String csvStr, Class<T> tClass, CsvHandlerConfig csvReadConfig, Function<String, Boolean> boolFunction) throws NoSuchMethodException {
        List<FieldCacheEntry> cacheEntries = getAndSetCacheEntry(tClass, csvReadConfig);
        // 再次判断是否为空
        if (CollectionUtil.isEmpty(cacheEntries)) {
            return CollectionUtil.newArrayList();
        }
        // 获取 header alias和对应的field entry信息
        Map<String, FieldCacheEntry> headerAliasEntry = FIELD_CACHE_HEADER_ENTRY.get(tClass);
        return readStr2List(csvStr, tClass, csvReadConfig, headerAliasEntry, boolFunction);
    }

    /**
     * 传入reader的犯事获取数据
     *
     * @param bufferedReader buffer reader
     * @param tClass         目标对象
     * @param csvReadConfig  读取配置
     * @return List<T>
     * @date 2025/11/23
     */
    public static <T> List<T> readStrBufferReader(BufferedReader bufferedReader, Class<T> tClass, CsvHandlerConfig csvReadConfig, Function<String, Boolean> boolFunction) throws NoSuchMethodException, IOException, InvocationTargetException, InstantiationException, IllegalAccessException {
        List<FieldCacheEntry> cacheEntries = getAndSetCacheEntry(tClass, csvReadConfig);
        // 再次判断是否为空
        if (CollectionUtil.isEmpty(cacheEntries)) {
            return CollectionUtil.newArrayList();
        }
        // 获取 header alias和对应的field entry信息
        Map<String, FieldCacheEntry> headerAliasEntry = FIELD_CACHE_HEADER_ENTRY.get(tClass);
        return readData(bufferedReader, tClass, csvReadConfig, headerAliasEntry, boolFunction);
    }

    /**
     * 通过 inputStream读取成目标集合
     *
     * @param inputStream   输入流
     * @param tClass        目标对象字节码
     * @param csvReadConfig 配置
     * @return List<T>
     * @date 2025/11/23
     */
    public static <T> List<T> readStream(InputStream inputStream, Class<T> tClass, CsvHandlerConfig csvReadConfig, Function<String, Boolean> boolFunction) throws NoSuchMethodException, IOException, InvocationTargetException, InstantiationException, IllegalAccessException {
        try (
                Reader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
        ) {
            return readStrBufferReader(bufferedReader, tClass, csvReadConfig, boolFunction);
        }
    }

    /**
     * 将csv 格式的字符串读取成指定 类型的集合
     *
     * @param csvStr           csv 格式字符串
     * @param tClass           指定类的class对象
     * @param handlerConfig    配置
     * @param headerAliasEntry header别名和缓存映射
     * @param boolFunction     boolean转换function
     * @return List<T>
     * @date 2025/11/23
     */
    public static <T> List<T> readStr2List(String csvStr, Class<T> tClass, CsvHandlerConfig handlerConfig, Map<String, FieldCacheEntry> headerAliasEntry, Function<String, Boolean> boolFunction) {
        try (InputStream byteArray = new ByteArrayInputStream(csvStr.getBytes(handlerConfig.getCharset()));
             InputStreamReader isr = new InputStreamReader(byteArray);
             BufferedReader bufferedReader = new BufferedReader(isr)
        ) {
            return readData(bufferedReader, tClass, handlerConfig, headerAliasEntry, boolFunction);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    /**
     * 从 bufferReader中获取对应的数据目标类
     *
     * @param bufferedReader   reader
     * @param tClass           目标类
     * @param handlerConfig    读取配置
     * @param headerAliasEntry header 别名和entry
     * @param boolFunction     boolean值转换的 function
     * @return List<T>
     * @date 2025/11/23
     */
    public static <T> List<T> readData(BufferedReader bufferedReader, Class<T> tClass, CsvHandlerConfig handlerConfig, Map<String, FieldCacheEntry> headerAliasEntry, Function<String, Boolean> boolFunction) throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        List<T> result = CollectionUtil.newArrayList();
        int rowNum = 1;
        Map<Integer, String> rowNumHeaderMap = MapUtil.newHashMap();
        List<String[]> beforeHeaderData = new ArrayList<>();
        // 读取数据
        while (bufferedReader.ready()) {
            String line = bufferedReader.readLine();
            // 如果是当前读取出来的数据为空，则说明已经读取行数完毕，停止解析
            if (StrUtil.isBlank(line)) {
                break;
            }

            // 判断是否是header 行
            if (handlerConfig.getHeaderLine() == rowNum) {
                // 处理行数据
                String[] splitArr = line.split(handlerConfig.getDelimiter());
                for (int i = 0; i < splitArr.length; i++) {
                    rowNumHeaderMap.put(i, splitArr[i]);
                }
                rowNum++;
                continue;
            }
            // 数据行处理
            if (rowNum >= handlerConfig.getDataStartLine()) {
                // 查找被引号引用起来的数据和json 数据
                String preprocessLine = preprocessRowData(line);
                String[] splitArr = preprocessLine.split(handlerConfig.getDelimiter());
                // 如果当前header数据还没有，则先添加进待处理，后续处理
                if (CollectionUtil.isEmpty(rowNumHeaderMap)) {
                    beforeHeaderData.add(splitArr);
                    rowNum++;
                    continue;
                }
                // 实际处理数据的方法
                T t = read2Obj(tClass, splitArr, rowNumHeaderMap, headerAliasEntry, boolFunction);
                result.add(t);
            }

            rowNum++;
        }
        // 如果数据行起始在header行之前，那么此集合中会有没到header行的数据
        if (CollectionUtil.isEmpty(beforeHeaderData)) {
            return result;
        }
        // 存在数据，则继续解析
        for (String[] beforeHeaderDatum : beforeHeaderData) {
            T t = read2Obj(tClass, beforeHeaderDatum, rowNumHeaderMap, headerAliasEntry, boolFunction);
            result.add(t);
        }
        return result;
    }

    /**
     * 读取数据行为目标类
     *
     * @param tClass           目标类
     * @param dataArray        当前行的数据数组
     * @param rowNumHeaderMap  列和header 别名的map映射
     * @param headerAliasEntry header 别名和entry
     * @param boolFunction     boolean值转换的 function
     * @return T 目标类的实例化对象
     * @date 2025/11/23
     */
    private static <T> T read2Obj(Class<T> tClass, String[] dataArray, Map<Integer, String> rowNumHeaderMap, Map<String, FieldCacheEntry> headerAliasEntry, Function<String, Boolean> boolFunction) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        T t = tClass.getConstructor().newInstance();
        // 处理数据
        for (int i = 0; i < dataArray.length; i++) {
            String header = rowNumHeaderMap.get(i);
            FieldCacheEntry fieldCacheEntry = headerAliasEntry.get(header);
            if (Objects.isNull(fieldCacheEntry)) {
                log.warn("没有找到当前header ：{} 对应的解析方法，请检查", header);
                continue;
            }
            // 当前列数据
            String columData = dataArray[i];
            // 获取类型枚举对象
            CsvHandlerEnum csvHandlerEnum = CsvHandlerEnum.getEnum(fieldCacheEntry.isBaseDataType(), fieldCacheEntry.isDate(), fieldCacheEntry.isCollection(), fieldCacheEntry.isFormat2Json());
            CsvReadHandler csvHandler;
            if (CUSTOMER_READ_HANDLER.containsKey(csvHandlerEnum)) {
                csvHandler = CUSTOMER_READ_HANDLER.get(csvHandlerEnum);
            } else {
                csvHandler = READ_HANDLER.get(csvHandlerEnum);
            }
            csvHandler.readCsvData(csvHandlerEnum, fieldCacheEntry, columData, t, boolFunction);
        }
        return t;
    }

    /**
     * 预处理行数据
     *
     * @param rowData the rowData
     * @return String
     * @date 2025/11/23
     */
    private static String preprocessRowData(String rowData) {
        // 替换数据行的consumer
        MoreParamConsumer consumer = (matcher, operateStr, startStr, endStr, jsonFlag) -> {
            while (matcher.find()) {
                String realInnerStr = matcher.group(IntConstants.INT_1);
                // 提前把当前正则表达式中获取到的字符串中的 逗号 替换为其他 特殊字符
                String replaceComma = realInnerStr.replace(StrUtil.COMMA, CsvHandlerConstants.CSV_STR_REPLACE_COMMA);
                if (jsonFlag) {
                    // 如果是json的情况下替换引号为 @
                    String replaceInnerStr = replaceComma.replaceAll(CsvHandlerConstants.DOUBLE_QUOTE_STR, CsvHandlerConstants.CSV_STR_REPLACE_QUOTE);
                    operateStr = operateStr.replace(startStr + realInnerStr + endStr, StrPool.DELIM_START + replaceInnerStr + StrPool.DELIM_END);
                } else {
                    operateStr = operateStr.replace(startStr + realInnerStr + endStr, replaceComma);
                }
            }
            return operateStr;
        };
        // 查找row data中的json 集合并替换
        Matcher matcher = RegexUtils.jsonListMatcher(rowData);
        rowData = consumer.replace(matcher, rowData, CsvHandlerConstants.JSON_LIST_START, CsvHandlerConstants.JSON_LIST_END, Boolean.TRUE);

        // 查找row data中的json 对象并替换
        Matcher jsonObjMatcher = RegexUtils.jsonObjMatcher(rowData);
        rowData = consumer.replace(jsonObjMatcher, rowData, CsvHandlerConstants.JSON_OBJ_START, CsvHandlerConstants.JSON_OBJ_END, Boolean.TRUE);

        // 查找row data中的双引号 并替换
        Matcher doubleQuoteMatcher = RegexUtils.doubleQuoteMatcher(rowData);
        rowData = consumer.replace(doubleQuoteMatcher, rowData, CsvHandlerConstants.DOUBLE_QUOTE_STR, CsvHandlerConstants.DOUBLE_QUOTE_STR, Boolean.FALSE);

        return rowData;
    }

    // endregion

    // region 初始化数据

    /**
     * 初始化写数据
     *
     * @date 2025/11/23
     */
    private static void initWriterMapping() {
        // 初始化数据
        CsvWriteHandler basicCsvWrite = new BasicCsvWrite();
        CsvWriteHandler collectionCsvWrite = new CollectionCsvWrite();
        CsvWriteHandler dateCsvWrite = new DateCsvWrite();
        CsvWriteHandler jsonFormatterCsvWrite = new JsonFormatterCsvWrite();
        CsvWriteHandler otherCsvWrite = new OtherCsvWrite();
        // 加入缓存
        WRITE_HANDLER.put(basicCsvWrite.getHandlerType(), basicCsvWrite);
        WRITE_HANDLER.put(collectionCsvWrite.getHandlerType(), collectionCsvWrite);
        WRITE_HANDLER.put(dateCsvWrite.getHandlerType(), dateCsvWrite);
        WRITE_HANDLER.put(jsonFormatterCsvWrite.getHandlerType(), jsonFormatterCsvWrite);
        WRITE_HANDLER.put(otherCsvWrite.getHandlerType(), otherCsvWrite);
    }

    /**
     * 初始化读数据
     *
     * @date 2025/11/23
     */
    private static void initReaderMapping() {
        CsvReadHandler basicReader = new BasicCsvReader();
        CsvReadHandler collectionCsvReader = new CollectionCsvReader();
        CsvReadHandler dateCsvReader = new DateCsvReader();
        CsvReadHandler jsonFormatterCsvReader = new JsonFormatterCsvReader();
        CsvReadHandler otherCsvReader = new OtherCsvReader();

        READ_HANDLER.put(basicReader.getHandlerType(), basicReader);
        READ_HANDLER.put(collectionCsvReader.getHandlerType(), collectionCsvReader);
        READ_HANDLER.put(dateCsvReader.getHandlerType(), dateCsvReader);
        READ_HANDLER.put(jsonFormatterCsvReader.getHandlerType(), jsonFormatterCsvReader);
        READ_HANDLER.put(otherCsvReader.getHandlerType(), otherCsvReader);
    }

    // endregion

    /**
     * 获取 csv 文件中的值
     *
     * @param objs         对象集合
     * @param cacheEntries 缓存的数据
     * @param sb           返回的csv文件字符串
     * @date 2025/11/23
     */
    private static <T> void values(Iterable<T> objs, List<FieldCacheEntry> cacheEntries, StringBuilder sb, Supplier<BoolSupplierConfig> boolTranStrSupplier) {
        objs.forEach(obj -> buildValues(cacheEntries, sb, obj, boolTranStrSupplier));
    }

    /**
     * 构建value的方法
     *
     * @param cacheEntries 缓存的属性
     * @param sb           字符串
     * @param obj          当前对象
     * @date 2025/11/23
     */
    private static <T> void buildValues(List<FieldCacheEntry> cacheEntries, StringBuilder sb, T obj, Supplier<BoolSupplierConfig> boolTranStrSupplier) {
        for (FieldCacheEntry cacheEntry : cacheEntries) {
            try {
                buildCsvStrData(cacheEntry, sb, obj, boolTranStrSupplier);
            } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        sb.replace(sb.length() - 1, sb.length(), StrUtil.CRLF);
    }


    /**
     * headers 构建
     *
     * @param clazz 当前转换的类的Class对象
     * @return String
     * @throws NoSuchMethodException 调用方法未找到
     * @date 2025/11/23
     */
    private static List<FieldCacheEntry> headers(Class<?> clazz, StringBuilder stringBuilder) throws NoSuchMethodException {
        if (HEADERS_CACHE.containsKey(clazz)) {
            stringBuilder.append(HEADERS_CACHE.get(clazz));
            return FIELD_CACHE.get(clazz);
        }
        return getHeaders(clazz, stringBuilder);
    }

    /**
     * 获取所有header 别名
     *
     * @param clazz class 对象
     * @return StringBuilder
     * @throws NoSuchMethodException 调用方法未找到
     * @date 2025/11/23
     */
    private static List<FieldCacheEntry> getHeaders(Class<?> clazz, StringBuilder stringBuilder) throws NoSuchMethodException {
        DeclaredAndSuperClass declaredAndSuperClass = new DeclaredAndSuperClass(clazz);
        List<Field> allFields = declaredAndSuperClass.getAllFields();

        List<FieldCacheEntry> orderedList = new ArrayList<>();

        // 当前类和父类的所有属性
        for (Field field : allFields) {
            FieldCacheEntry entry = fieldCacheEntry(field);
            if (Objects.nonNull(entry)) {
                String name = field.getName();
                String getterMethodName = StrUtil.genGetter(name);
                String setterMethodName = StrUtil.genSetter(name);
                if (entry.isBoolean()) {
                    String suffixName = StrUtil.startWith(name, CsvHandlerConstants.IS_PREFIX) ? name.replace(CsvHandlerConstants.IS_PREFIX, StrUtil.EMPTY) : name;
                    if (Boolean.class.equals(field.getType())) {
                        getterMethodName = StrUtil.genGetter(suffixName);
                    } else {
                        getterMethodName = StrUtil.upperFirstAndAddPre(suffixName, CsvHandlerConstants.IS_PREFIX);
                    }
                    setterMethodName = StrUtil.genSetter(suffixName);
                }
                entry.setFieldGetter(declaredAndSuperClass.getMethod(getterMethodName));
                entry.setFieldSetter(declaredAndSuperClass.getMethod(setterMethodName, entry.getFieldClass()));
                orderedList.add(entry);
            }
        }
        // 需要转换成CSV文件的属性
        List<FieldCacheEntry> result = orderedList.stream()
                .collect(Collectors.toMap(FieldCacheEntry::getHeaderAlias, Function.identity(), (oldVal, newVal) -> newVal, LinkedHashMap::new))
                .values().stream().sorted(Comparator.comparing(FieldCacheEntry::getOrder)).collect(Collectors.toList());
        // header 别名
        String allHeaderAlias = result.stream().map(FieldCacheEntry::getHeaderAlias).collect(Collectors.joining(StrUtil.COMMA));
        // 所有header 加上换行符
        stringBuilder.append(allHeaderAlias);
        stringBuilder.append(StrUtil.CRLF);
        // 加入缓存
        String allHeaderAliasCrlf = stringBuilder.toString();
        HEADERS_CACHE.put(clazz, allHeaderAliasCrlf);
        putFieldCache(clazz, result);
        return result;
    }

    /**
     * 设置class对应的缓存数据
     *
     * @param clazz        class对象
     * @param cacheEntries 缓存数据
     * @date 2025/11/23
     */
    private static void putFieldCache(Class<?> clazz, List<FieldCacheEntry> cacheEntries) {
        FIELD_CACHE.put(clazz, cacheEntries);
        if (cacheEntries.isEmpty()) {
            FIELD_CACHE_HEADER_ENTRY.put(clazz, MapUtil.newHashMap());
            return;
        }
        Map<String, FieldCacheEntry> headerAliasMapping = cacheEntries.stream().collect(Collectors.toMap(FieldCacheEntry::getHeaderAlias, Function.identity(), (oldVal, newVal) -> newVal));
        FIELD_CACHE_HEADER_ENTRY.put(clazz, headerAliasMapping);
    }

    /**
     * 获取当前类需要的所有属性header 值等
     *
     * @param field 属性
     * @return FieldCacheEntry
     * @throws NoSuchMethodException 调用方法未找到
     * @date 2025/11/23
     */
    private static FieldCacheEntry fieldCacheEntry(Field field) throws NoSuchMethodException {
        CsvIgnore ignore = field.getAnnotation(CsvIgnore.class);
        if (!Objects.isNull(ignore)) {
            return null;
        }
        return new FieldCacheEntry(field);
    }

    /**
     * 读取csv数据成对象时添加对应的cache
     *
     * @param tClass        目标对象calss
     * @param csvReadConfig 读取配置
     * @return List<FieldCacheEntry>
     * @date 2025/11/23
     */
    private static <T> List<FieldCacheEntry> getAndSetCacheEntry(Class<T> tClass, CsvHandlerConfig csvReadConfig) throws NoSuchMethodException {
        if (Objects.isNull(csvReadConfig)) {
            throw new CsvException("read csv property config cannot be null!");
        }
        // 判断缓存中是否有数据
        List<FieldCacheEntry> cacheEntries = FIELD_CACHE.get(tClass);
        // 如果缓存中没有数据，则构建缓存
        if (CollectionUtil.isEmpty(cacheEntries)) {
            cacheEntries = getHeaders(tClass, new StringBuilder());
        }
        return cacheEntries;
    }

    /**
     * 构建通过注解，将对象数据转换成 csv 文件格式字符串的方法
     *
     * @param cacheEntry          缓存数据
     * @param stringBuilder       构建 csv 文件的东西
     * @param fieldValue          当前属性通过getter获取到的额值
     * @param boolTranStrSupplier boolean值的默认值
     * @date 2025/11/23
     */
    private static void buildCsvStrData(FieldCacheEntry cacheEntry, StringBuilder stringBuilder,
                                        Object fieldValue, Supplier<BoolSupplierConfig> boolTranStrSupplier)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {

        Object classFieldValue = cacheEntry.getFieldGetter().invoke(fieldValue);
        // 如果为空
        if (Objects.isNull(classFieldValue)) {
            stringBuilder.append(StrUtil.EMPTY).append(StrUtil.COMMA);
            return;
        }
        CsvHandlerEnum handlerEnum = CsvHandlerEnum.getEnum(cacheEntry.isBaseDataType(), cacheEntry.isDate(), cacheEntry.isCollection(), cacheEntry.isFormat2Json());
        // 获取对应的处理类
        CsvWriteHandler abstractCsvCreateValue;
        if (CUSTOMER_WRITE_HANDLER.containsKey(handlerEnum)) {
            abstractCsvCreateValue = CUSTOMER_WRITE_HANDLER.get(handlerEnum);
        } else {
            abstractCsvCreateValue = WRITE_HANDLER.get(handlerEnum);
        }

        if (Objects.isNull(abstractCsvCreateValue)) {
            throw new CsvWriteException("deal value instance is null");
        }
        abstractCsvCreateValue.writeData(handlerEnum, cacheEntry, stringBuilder, classFieldValue, boolTranStrSupplier);
    }

    /**
     * 自定consumer用于提前处理获取到的字符串信息
     */
    interface MoreParamConsumer {
        /**
         * 替换字符串
         *
         * @param matcher    被检测字符串的matcher
         * @param operateStr 操作的字符串
         * @param startStr   替换字符串的开头
         * @param endStr     替换字符串的结尾
         * @param jsonFlag   是否是json
         * @return String 替换后的字符串
         * @date 2025/11/23
         */
        String replace(Matcher matcher, String operateStr, String startStr, String endStr, boolean jsonFlag);
    }
}
