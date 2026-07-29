package com.riverflow.api.plugin;

import java.io.InputStream;

/**
 * 文件流响应对象
 * 用于插件接口返回文件流
 */
public class FileResponse {
    
    private InputStream inputStream;
    private String fileName;
    private String contentType;
    private long contentLength;
    
    public FileResponse() {
    }
    
    public FileResponse(InputStream inputStream, String fileName, String contentType) {
        this.inputStream = inputStream;
        this.fileName = fileName;
        this.contentType = contentType;
    }
    
    public static FileResponse of(InputStream inputStream, String fileName, String contentType) {
        return new FileResponse(inputStream, fileName, contentType);
    }
    
    // Getters and Setters
    public InputStream getInputStream() {
        return inputStream;
    }
    
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public long getContentLength() {
        return contentLength;
    }
    
    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }
}