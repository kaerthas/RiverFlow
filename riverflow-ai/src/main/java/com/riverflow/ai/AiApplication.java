package com.riverflow.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RiverFlow AI 智能助手服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.riverflow")
public class AiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiApplication.class, args);
    }
}
