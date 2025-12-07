package com.mujin.commons.web.request;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.IteratorEnumeration;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.mujin.commons.lang.RegexUtils;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.apache.tomcat.util.http.FastHttpDateFormat;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 请求对象的封装
 *
 * @author chenglin.wu
 * @date 2025/11/23
 */
public class HttpRequestWrapper extends HttpServletRequestWrapper {
    /**
     * 请求体
     */
    private final byte[] body;
    /**
     * 请求头
     */
    private final Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public HttpRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        body = StreamUtils.copyToByteArray(request.getInputStream());
        this.initHeader(request);

    }

    /**
     * 创建对象的时候读取 header
     *
     * @param request the request
     * @date 2025/11/23
     */
    private void initHeader(HttpServletRequest request) {
        // 读取 header
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> requestHeaders = request.getHeaders(headerName);
            List<String> headerValues = this.headers.get(headerName);
            if (CollectionUtil.isEmpty(headerValues)) {
                headerValues = new ArrayList<>();
                this.headers.put(headerName, headerValues);
            }
            // 添加 header
            while (requestHeaders.hasMoreElements()) {
                String requestHeader = requestHeaders.nextElement();
                headerValues.add(requestHeader);
            }
        }
    }

    /**
     * 获取 body 字符串形式
     *
     * @return String
     * @date 2025/11/23
     */
    public String getBodyJsonStr() {
        return new String(this.body, StandardCharsets.UTF_8);
    }

    /**
     * 添加 header
     *
     * @param name   header 名
     * @param values header 值集合
     * @date 2025/11/23
     */
    public void addHeader(String name, String... values) {
        if (ArrayUtil.isEmpty(values)) {
            return;
        }
        List<String> headerValues = this.headers.get(name);
        if (CollectionUtil.isEmpty(headerValues)) {
            headerValues = new ArrayList<>();
            this.headers.put(name, headerValues);
        }
        headerValues.addAll(CollectionUtil.newArrayList(values));

    }


    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> headerValues = this.headers.get(name);
        if (CollectionUtil.isEmpty(headerValues)) {
            return null;
        }
        return new IteratorEnumeration<>(headerValues.iterator());
    }

    @Override
    public String getHeader(String name) {
        List<String> headerValues = this.headers.get(name);
        if (Objects.isNull(headerValues)) {
            return super.getHeader(name);
        }
        if (CollectionUtil.isEmpty(headerValues)) {
            return StrUtil.EMPTY;
        }
        return headerValues.getFirst();
    }

    @Override
    public int getIntHeader(String name) {
        String value = this.getHeader(name);
        if (Objects.isNull(value)) {
            return -1;
        }
        if (!RegexUtils.isNumber(value)) {
            return -1;
        }
        return Integer.parseInt(value);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Iterator<String> iterator = this.headers.keySet().stream().filter(Objects::nonNull).iterator();
        return new IteratorEnumeration<>(iterator);
    }

    @Override
    public long getDateHeader(String name) {
        String value = getHeader(name);
        if (value == null) {
            return -1L;
        }
        // Attempt to convert the date header in a variety of formats
        long result = FastHttpDateFormat.parseDate(value);
        if (result != (-1L)) {
            return result;
        }
        throw new IllegalArgumentException(value);
    }

    /**
     * 获取请求内容
     *
     * @return BufferedReader
     * @throws IOException I/O 异常
     */
    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    /**
     * 返回请求内容字节流
     *
     * @return ServletInputStream
     */
    @Override
    public ServletInputStream getInputStream() {

        return new ServletInputStream() {

            private ByteArrayInputStream buffer = new ByteArrayInputStream(body);


            @Override
            public int read() throws IOException {
                return this.buffer.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener arg0) {

            }

            @Override
            public void close() throws IOException {
                super.close();
                this.buffer.close();
                this.buffer = null;
            }

        };

    }
}
