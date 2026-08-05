package com.jianghu.ling;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.jianghu.ling.**.mapper")
@EnableScheduling
public class JianghuLingApplication {

    public static void main(String[] args) {
        SpringApplication.run(JianghuLingApplication.class, args);
    }
}
