package com.jihedapps.notesapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class NotesAppBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotesAppBackendApplication.class, args);
    }
}
