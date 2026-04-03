package com.opensprout.lessonai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.opensprout.lessonai.mapper")
public class LessonAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LessonAiApplication.class, args);
    }

}
