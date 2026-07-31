package com.riverflow.admin.infra.http;

import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * HTTP 请求执行器工厂
 * 支持直连和代理两种策略
 */
@Component
public class HttpRequestExecutorFactory {

    @Autowired
    private CloseableHttpClient httpClient;

    /**
     * 创建 GET 执行器
     */
    public GetRequestExecutor createGetExecutor(boolean useProxy, String proxyHost, int proxyPort) {
        return new GetRequestExecutor(httpClient, createStrategy(useProxy, proxyHost, proxyPort));
    }

    /**
     * 创建 JSON POST 执行器
     */
    public JsonPostRequestExecutor createJsonPostExecutor(boolean useProxy, String proxyHost, int proxyPort) {
        return new JsonPostRequestExecutor(httpClient, createStrategy(useProxy, proxyHost, proxyPort));
    }

    /**
     * 创建 Form POST 执行器
     */
    public FormPostRequestExecutor createFormPostExecutor(boolean useProxy, String proxyHost, int proxyPort) {
        return new FormPostRequestExecutor(httpClient, createStrategy(useProxy, proxyHost, proxyPort));
    }

    /**
     * 创建 XML POST 执行器
     */
    public XmlPostRequestExecutor createXmlPostExecutor(boolean useProxy, String proxyHost, int proxyPort) {
        return new XmlPostRequestExecutor(httpClient, createStrategy(useProxy, proxyHost, proxyPort));
    }

    /**
     * 创建纯文本 POST 执行器
     */
    public TextPostRequestExecutor createTextPostExecutor(boolean useProxy, String proxyHost, int proxyPort) {
        return new TextPostRequestExecutor(httpClient, createStrategy(useProxy, proxyHost, proxyPort));
    }

    private RequestExecutionStrategy createStrategy(boolean useProxy, String proxyHost, int proxyPort) {
        if (useProxy && proxyHost != null && !proxyHost.isEmpty()) {
            return new HttpProxyStrategy(proxyHost, proxyPort);
        }
        return new DirectExecutionStrategy();
    }
}
