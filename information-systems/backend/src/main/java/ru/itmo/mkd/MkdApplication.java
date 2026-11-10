package ru.itmo.mkd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MkdApplication {
    public static void main(String[] args) {
        SpringApplication.run(MkdApplication.class, args);
    }
}
