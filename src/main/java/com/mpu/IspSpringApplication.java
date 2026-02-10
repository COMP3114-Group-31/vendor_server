package com.mpu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.mpu.vendor.mapper")
public class IspSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(IspSpringApplication.class, args);
    }

}
