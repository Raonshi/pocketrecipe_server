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

/**
 * <p>REST API 싱글톤 객체</p>
 * <p>서버의 각 기능을 url로 맵핑하여 클라이언트가 이용할 수 있도록 한다.</p>
 */
@RestController
public class APIController {
    private static APIController instance = null;

    /**
     * 싱글톤 생성 메소드
     * @return 객체가 존재하면 기존 객체를 반환, 존재하지 않으면 새로운 객체 생성
     */
    public APIController Init(){
        if(instance == null){
            instance = new APIController();
        }
        return this.instance;
    }


    /**
     * 레시피조회 API
     * @param keyword 데이터베이스에서 조회하려는 레시피의 이름
     * @return 성공하면 조회된 레시피 리스트를 response, 실패하면 실패 json을 response
     * @throws InterruptedException
     */
    @RequestMapping(method = RequestMethod.GET, path = "searchRecipe")
    JSONArray searchRecipe(@RequestParam String keyword) throws InterruptedException {

        DBConnector conn = new DBConnector(APIService.SELECT);
        conn.setKeyword(keyword);

        conn.start();

        Thread.sleep(1500);

        if(!conn.isComplete){
            System.out.println("레시피 조회 실패");
            JSONArray array = new JSONArray();
            JSONObject obj = new JSONObject();
            obj.put("message", "Failed");
            return array;
        }
        else{
            System.out.println("레시피 조회 성공");
            return conn.resultList;
        }
    }


    /**
     * 나의 레시피조회 API
     * @param keyword 데이터베이스에서 조회하려는 레시피의 이름
     * @return 성공하면 조회된 레시피 리스트를 response, 실패하면 실패 json을 response
     * @throws InterruptedException
     */
    @RequestMapping(method = RequestMethod.GET, path = "searchMyRecipe")
    JSONArray searchMyRecipe(@RequestParam String keyword, @RequestParam String author) throws InterruptedException {

        DBConnector conn = new DBConnector(APIService.SELECT);
        conn.setKeyword(keyword);
        conn.setAuthor(author);

        conn.start();

        Thread.sleep(1500);

        if(!conn.isComplete){
            System.out.println("레시피 조회 실패");
            JSONArray array = new JSONArray();
            JSONObject obj = new JSONObject();
            obj.put("message", "Failed");
            return array;
        }
        else{
            System.out.println("레시피 조회 성공");
            return conn.resultList;
        }
    }



    /**
     * 레시피 삭제 API
     * @param deleteList 데이터베이스에서 삭제하려는 레시피 데이터
     * @return Success는 성공, Failed는 실패
     * @throws InterruptedException
     */
    @RequestMapping(method = RequestMethod.DELETE, path="deleteRecipe")
    String deleteRecipe(@RequestBody JSONObject deleteList) throws InterruptedException {
        DBConnector conn = new DBConnector(APIService.DELETE);
        conn.setDeleteList(deleteList);
        conn.start();

        Thread.sleep(1500);

        if(!conn.isComplete){
            System.out.println("레시피 삭제 실패");
            return "Failed";
        }
        else{
            System.out.println("레시피 삭제 성공");
            return "Success";
        }
    }


    /**
     * 레시피 등록 API
     * @param recipe 데이터베이스에 추가할 레시피 데이터
     * @return Success는 성공, Failed는 실패
     * @throws InterruptedException
     */
    @RequestMapping(method = RequestMethod.POST, value="insertRecipe")
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
            System.out.println("레시피 등록 실패");
            return "Fail";
        }
        System.out.println("레시피 등록 성공");
        return "Success";
    }


    /**
     * 레시피 수정 API
     * @param recipe 데이터베이스에 갱신할 레시피 데이터
     * @return Success는 성공, Failed는 실패
     * @throws InterruptedException
     */
    @RequestMapping(method = RequestMethod.PUT, path="updateRecipe")
    JSONObject updateRecipe(@RequestParam JSONObject recipe) throws InterruptedException{
        JSONObject json = new JSONObject();
        json.put("request_type","update");

        DBConnector conn = new DBConnector(APIService.UPDATE);
        conn.start();

        return json;
    }


    /**
     * 이미지 조회 API
     * @param filePath 조회하려는 이미지의 경로
     * @return 이미지의 byte배열을 반환
     */
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
