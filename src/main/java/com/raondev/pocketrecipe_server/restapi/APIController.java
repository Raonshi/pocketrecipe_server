package com.raondev.pocketrecipe_server.restapi;

import com.raondev.pocketrecipe_server.dbconnect.APIService;
import com.raondev.pocketrecipe_server.dbconnect.DBConnector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@RestController
public class APIController {
    private static APIController instance = null;

    public APIController Init(){
        if(instance == null){
            instance = new APIController();
        }
        return this.instance;
    }

    @RequestMapping(method = RequestMethod.GET, path = "searchRecipe")
    JSONArray searchRecipe(@RequestParam String keyword) throws InterruptedException {

        DBConnector conn = new DBConnector(APIService.SELECT);
        conn.setKeyword(keyword);

        conn.start();

        Thread.sleep(1500);

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

        System.out.println("RECIPE : " + recipe.toJSONString());

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


    @RequestMapping(method = RequestMethod.GET, path = "image/view", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getImage(@RequestParam String filePath){
        FileInputStream fis = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String fileDir = filePath; // 파일경로

        try{
            fis = new FileInputStream(fileDir);
        }
        catch(IOException e){
            e.printStackTrace();
        }

        int readCount = 0;
        byte[] buffer = new byte[1024];
        byte[] fileArray = null;

        try{
            while((readCount = fis.read(buffer)) != -1){
                baos.write(buffer, 0, readCount);
            }
            fileArray = baos.toByteArray();
            fis.close();
            baos.close();
        } catch(IOException e){
            throw new RuntimeException("File Error");
        }
        return fileArray;
    }
}
