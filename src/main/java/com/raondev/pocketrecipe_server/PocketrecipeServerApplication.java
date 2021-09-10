package com.raondev.pocketrecipe_server;

import com.raondev.pocketrecipe_server.openapi.API;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PocketrecipeServerApplication {


    public static void main(String[] args) {
        SpringApplication.run(PocketrecipeServerApplication.class, args);

        API api = new API();

        api.getFromRecipeName("된장국");

        System.out.println(api.recipeList.get(0).getManualImg(1));


    }

}
