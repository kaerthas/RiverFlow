package com.inspur.workinfo;

import com.inspur.workinfo.annotation.EnableYunhoSwagger2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author : Jason
 * @date : 2020/6/17 8:37
 * @description :
 */
@EnableYunhoSwagger2
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
