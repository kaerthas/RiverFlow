package com.riverflow.admin.infra.openapi;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 可重复读取请求体的 HttpServletRequest 包装器
 * <p>
 * 认证过滤器中需要读取 body 计算签名，同时 Controller 也可能再次读取 body 或调用 getParameter，
 * 因此需要在包装器中将 body 缓存下来，并支持对 application/x-www-form-urlencoded 参数的解析。
 */
public class RepeatableReadRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] body;
    private final String contentType;
    private final boolean formEncoded;
    private final Map<String, List<String>> formParams = new LinkedHashMap<>();

    public RepeatableReadRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        this.contentType = normalizeContentType(request.getContentType());
        this.formEncoded = isFormEncoded(this.contentType);
        this.body = readBytes(request);
        if (this.formEncoded) {
            parseFormParams();
        }
    }

    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return bais.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // not used
            }

            @Override
            public int read() {
                return bais.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), getCharset()));
    }

    /**
     * 获取请求体原始字节数组
     */
    public byte[] getBody() {
        return body;
    }

    /**
     * 获取请求体字符串
     */
    public String getBodyString() {
        return new String(body, getCharset());
    }

    @Override
    public String getParameter(String name) {
        if (!formEncoded) {
            return super.getParameter(name);
        }
        List<String> values = formParams.get(name);
        if (values == null || values.isEmpty()) {
            return super.getParameter(name);
        }
        return values.get(0);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        if (!formEncoded) {
            return super.getParameterMap();
        }
        Map<String, String[]> result = new LinkedHashMap<>();
        // 先放入 query 参数
        result.putAll(super.getParameterMap());
        // body 参数覆盖 query 参数
        for (Map.Entry<String, List<String>> entry : formParams.entrySet()) {
            result.put(entry.getKey(), entry.getValue().toArray(new String[0]));
        }
        return result;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        if (!formEncoded) {
            return super.getParameterNames();
        }
        Map<String, String[]> map = getParameterMap();
        return Collections.enumeration(map.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        if (!formEncoded) {
            return super.getParameterValues(name);
        }
        List<String> values = formParams.get(name);
        if (values == null || values.isEmpty()) {
            return super.getParameterValues(name);
        }
        return values.toArray(new String[0]);
    }

    private byte[] readBytes(HttpServletRequest request) throws IOException {
        int contentLength = request.getContentLength();
        if (contentLength > 0) {
            byte[] buffer = new byte[contentLength];
            int total = 0;
            while (total < contentLength) {
                int read = request.getInputStream().read(buffer, total, contentLength - total);
                if (read == -1) {
                    break;
                }
                total += read;
            }
            if (total < contentLength) {
                byte[] actual = new byte[total];
                System.arraycopy(buffer, 0, actual, 0, total);
                return actual;
            }
            return buffer;
        }

        // Content-Length <= 0（如 chunked）时，循环读取全部内容
        try (java.io.InputStream is = request.getInputStream();
             java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        }
    }

    private void parseFormParams() {
        if (body.length == 0) {
            return;
        }
        String bodyStr = getBodyString();
        String[] pairs = bodyStr.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            String key;
            String value;
            if (idx > 0) {
                key = decode(pair.substring(0, idx));
                value = decode(pair.substring(idx + 1));
            } else if (idx == 0) {
                key = "";
                value = decode(pair.substring(1));
            } else {
                key = decode(pair);
                value = "";
            }
            formParams.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
    }

    private String decode(String value) {
        try {
            return URLDecoder.decode(value, getCharset().name());
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    private boolean isFormEncoded(String contentType) {
        return contentType != null && contentType.contains("application/x-www-form-urlencoded");
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return null;
        }
        int semi = contentType.indexOf(';');
        return semi > 0 ? contentType.substring(0, semi).trim().toLowerCase()
                : contentType.trim().toLowerCase();
    }

    private java.nio.charset.Charset getCharset() {
        String charset = getCharacterEncoding();
        if (charset != null) {
            try {
                return java.nio.charset.Charset.forName(charset);
            } catch (Exception ignored) {
            }
        }
        return StandardCharsets.UTF_8;
    }
}
