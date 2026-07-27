package com.riverflow.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RiverFlow AI 智能助手服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.riverflow")
@MapperScan({
        "com.riverflow.ai.mapper",
        "com.riverflow.ai.knowledge.mapper",
        "com.riverflow.ai.audit.mapper",
        "com.riverflow.ai.prompt.mapper",
        "com.riverflow.ai.model.mapper"
})
public class AiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiApplication.class, args);
    }
}
