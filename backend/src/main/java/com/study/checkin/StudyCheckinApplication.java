package com.study.checkin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.study.checkin.mapper")
public class StudyCheckinApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyCheckinApplication.class, args);
    }
}
