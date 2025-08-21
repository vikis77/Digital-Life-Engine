package com.randb.digitaldemo1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class Digitaldemo1Application {

    public static void main(String[] args) {
        SpringApplication.run(Digitaldemo1Application.class, args);
    }

}
