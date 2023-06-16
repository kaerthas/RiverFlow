package com.inspur.workinfo.config;


import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyStore;

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory factory){
        return new RestTemplate(factory);
    }
    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory(){
//        new SimpleClientHttpRequestFactory();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory(){
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                try {
                    if (connection instanceof HttpsURLConnection) {// https协议，修改协议版本
                        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                        // 信任任何链接,忽略对证书的校验
                        TrustStrategy anyTrustStrategy = (x509Certificates, s) -> true;
                        //自定义SSLContext
                        SSLContext ctx = SSLContexts.custom().loadTrustMaterial(trustStore, anyTrustStrategy).build();
                        // ssl问题
                        ((HttpsURLConnection) connection).setSSLSocketFactory(ctx.getSocketFactory());
                        //解决No subject alternative names matching IP address xxx.xxx.xxx.xxx found问题
                        ((HttpsURLConnection) connection).setHostnameVerifier((s, sslSession) -> true);
                        HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
                        super.prepareConnection(httpsConnection, httpMethod);
                    } else { // http协议
                        super.prepareConnection(connection, httpMethod);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        factory.setConnectTimeout(90000);
        factory.setReadTimeout(90000);
        return factory;
    }
}
