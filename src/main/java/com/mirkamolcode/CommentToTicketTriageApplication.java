package com.mirkamolcode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CommentToTicketTriageApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommentToTicketTriageApplication.class, args);
    }

}
