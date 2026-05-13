package com.riverflow.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * RiverFlow 管理后台启动类
 */
@EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.riverflow"})
public class AdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
        System.out.println(
            "\n" +
            "╔══════════════════════════════════════════════════╗\n" +
            "║                                                  ║\n" +
            "║     RiverFlow · 河狸流程编排平台                  ║\n" +
            "║     服务启动成功                                  ║\n" +
            "║     接口文档: http://localhost:8080/doc.html     ║\n" +
            "║                                                  ║\n" +
            "╚══════════════════════════════════════════════════╝\n"
        );
    }
}
