package com.raondev.pocketrecipe_server;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ImageDecoder {
    JSONObject recipe;

    public ImageDecoder(JSONObject recipe){
        this.recipe = recipe;
    }

    public String decode(String data, String target){
        data = recipe.get("recipe_image").toString();
        byte[] imageBytes = DatatypeConverter.parseBase64Binary(data);
        String path;

        try {
            BufferedImage bufImg = ImageIO.read(new ByteArrayInputStream(imageBytes));

            //작성자의 폴더가 존재하지 않을 경우 폴더 생성
            path = "image_data/" + this.recipe.get("recipe_author").toString();
            File author = new File(path);
            if(!author.exists()){
                author.mkdirs();
            }

            //작성자 폴더 내 레시피 폴더가 없을 경우 폴더 생성
            path +="/" + this.recipe.get("recipe_name").toString();
            File recipe = new File(path);
            if(!recipe.exists()){
                recipe.mkdirs();
            }

            target = path + "/" + target;
            //저장
            ImageIO.write(bufImg, "jpg", new File(target));

        } catch (IOException e) {
            e.printStackTrace();
            return "Unknown";
        }
        return target;
    }
}
