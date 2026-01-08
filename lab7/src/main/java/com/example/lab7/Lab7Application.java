package com.example.lab7;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableKafka
@EnableWebMvc
public class Lab7Application {

    public static void main(String[] args) {
        SpringApplication.run(Lab7Application.class, args);
    }

}
