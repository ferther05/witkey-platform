// WitkeyApplication —— 应用启动类：开启定时任务支持并启动 Spring Boot
package com.psc.witkey;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 应用启动类：开启定时任务支持并启动 Spring Boot。
 */
@SpringBootApplication
@EnableScheduling
public class WitkeyApplication {

    // 启动入口
    public static void main(String[] args) {
        SpringApplication.run(WitkeyApplication.class, args);
    }
}
