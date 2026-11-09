package com.psc.witkey;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WitkeyApplication {

    public static void main(String[] args) {
        SpringApplication.run(WitkeyApplication.class, args);
    }
}
