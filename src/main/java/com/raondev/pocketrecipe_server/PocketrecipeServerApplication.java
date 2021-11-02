package com.raondev.pocketrecipe_server;

import com.raondev.pocketrecipe_server.restapi.APIController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.web.bind.annotation.RestController;


@RestController
@SpringBootApplication
public class PocketrecipeServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(PocketrecipeServerApplication.class, args);
        APIController api = new APIController().Init();
    }
}
