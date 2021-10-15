package com.raondev.pocketrecipe_server.restapi;

import com.raondev.pocketrecipe_server.dbconnect.APIService;
import com.raondev.pocketrecipe_server.dbconnect.DBConnector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.*;

@RestController
public class API {
    private static API instance = null;

    public API Init(){
        if(instance == null){
            instance = new API();
        }
        return this.instance;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/searchRecipe")
    JSONArray searchRecipe(@RequestParam String keyword){
        JSONArray array = new JSONArray();
        JSONObject json = new JSONObject();
        json.put("request_type","search");
        array.add(json);

        DBConnector conn = new DBConnector(APIService.SELECT);
        conn.start();

        return array;
    }

    @RequestMapping(method = RequestMethod.DELETE, path="deleteRecipe")
    JSONObject deleteRecipe(@RequestParam JSONObject recipe){
        JSONObject json = new JSONObject();
        json.put("request_type","delete");

        DBConnector conn = new DBConnector(APIService.DELETE);
        conn.start();


        return json;
    }

    @RequestMapping(method = RequestMethod.POST, path="insertRecipe")
    JSONObject insertRecipe(@RequestParam JSONObject recipe){
        JSONObject json = new JSONObject();
        json.put("request_type","insert");

        DBConnector conn = new DBConnector(APIService.INSERT);
        conn.start();


        return json;
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
