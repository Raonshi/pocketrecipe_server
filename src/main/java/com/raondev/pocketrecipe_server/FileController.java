package com.raondev.pocketrecipe_server;

import org.json.simple.JSONObject;

import java.io.File;

public class FileController {
    public String getPath(JSONObject recipe){
        String author = recipe.get("recipe_author").toString();
        String recipeName = recipe.get("recipe_name").toString();
        String path = "image_data/" + author + "/" + recipeName;
        System.out.println(path);
        return path;
    }

    public void delete(String path){
        File folder = new File(path);

        if(folder.exists()){
            File[] ls = folder.listFiles();

            for(int i = 0; i < ls.length; i++){
                if(ls[i].isFile()){
                    ls[i].delete();
                }
                else{
                    delete(ls[i].getPath());
                }
                ls[i].delete();
            }
            folder.delete();
            System.out.println("대상을 삭제하였습니다.");
            return;
        }
        System.out.println("대상이 존재하지 않습니다.");
    }
}
