package com.raondev.pocketrecipe_server.restapi;

import com.raondev.pocketrecipe_server.dbconnect.APIService;
import com.raondev.pocketrecipe_server.dbconnect.DBConnector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class API {
    private static API instance = null;

    public API Init(){
        if(instance == null){
            instance = new API();
        }
        return this.instance;
    }

    @RequestMapping(method = RequestMethod.GET, path = "searchRecipe")
    JSONArray searchRecipe(@RequestParam String keyword){

        DBConnector conn = new DBConnector(APIService.SELECT);
        conn.setKeyword(keyword);

        conn.start();

        return conn.resultList;
    }

    @RequestMapping(method = RequestMethod.DELETE, path="deleteRecipe")
    JSONObject deleteRecipe(@RequestParam JSONObject recipe) throws InterruptedException {
        JSONObject json = new JSONObject();
        json.put("request_type","delete");

        DBConnector conn = new DBConnector(APIService.DELETE);
        conn.start();

        Thread.sleep(1000);

        return json;
    }

    @RequestMapping(method = RequestMethod.POST, value="/insertRecipe")
    String insertRecipe(@RequestBody JSONObject recipe) throws InterruptedException {
        DBConnector conn = new DBConnector(APIService.INSERT);
        conn.setRecipe(recipe);
        conn.start();

        Thread.sleep(1500);

        if(!conn.isComplete){
            File folder = new File(conn.recipeImagePath);
            try {
                while(folder.exists()) {
                    File[] folder_list = folder.listFiles(); //파일리스트 얻어오기

                    for (int j = 0; j < folder_list.length; j++) {
                        folder_list[j].delete(); //파일 삭제
                        System.out.println("파일이 삭제되었습니다.");
                    }

                    if(folder_list.length == 0 && folder.isDirectory()){
                        folder.delete(); //대상폴더 삭제
                        System.out.println("폴더가 삭제되었습니다.");
                    }
                }
            } catch (Exception e) {
                e.getStackTrace();
            }

            return "Fail";
        }

        return "Success";
    }

    @RequestMapping(method = RequestMethod.PUT, path="updateRecipe")
    JSONObject updateRecipe(@RequestParam JSONObject recipe){
        JSONObject json = new JSONObject();
        json.put("request_type","update");

        DBConnector conn = new DBConnector(APIService.UPDATE);
        conn.start();

        return json;
    }
}
