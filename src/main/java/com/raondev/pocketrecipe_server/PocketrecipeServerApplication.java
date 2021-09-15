package com.raondev.pocketrecipe_server;

import com.raondev.pocketrecipe_server.dbconnect.DBConnector;
import com.raondev.pocketrecipe_server.dbconnect.Function;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PocketrecipeServerApplication {


    public static void main(String[] args) {
        SpringApplication.run(PocketrecipeServerApplication.class, args);

        DBConnector connector = new DBConnector(Function.INSERT);
    }

}
