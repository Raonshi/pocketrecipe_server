package com.raondev.pocketrecipe_server.dbconnect;

import com.raondev.pocketrecipe_server.ImageDecoder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.sql.*;

public class DBConnector extends Thread{
    boolean isDev = true;

    String url;
    String user;
    String pw;

    String keyword;
    public void setKeyword(String value){this.keyword = value;}
    public String getKeyword(){return this.keyword;}

    JSONObject recipe;
    public void setRecipe(JSONObject value){this.recipe = value;}
    public JSONObject getRecipe(){return this.recipe;}

    public boolean isComplete;
    public String recipeImagePath;

    APIService function;
    Connection conn;

    String SQL = "";

    public JSONArray resultList = new JSONArray();


    public DBConnector(APIService function){
        this.function = function;

        this.url = (isDev) ? "jdbc:mysql://220.86.224.184:12008/pocket_recipe?characterEncoding=utf8" : "jdbc:mysql://127.0.0.1:3306/pocket_recipe";
        this.user = "root";
        this.pw = "tnsdnjs2@";

        getConnection();
    }

    void getConnection(){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, user, pw);

            System.out.println("DB Connector Created!");
        }
        catch(ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        try{
            String SQL = "";
            PreparedStatement pstmt;
            ResultSet rs;

            resultList.clear();

            switch(function){
                case SELECT:
                    SQL = "SELECT * FROM recipe_list WHERE recipe_nm LIKE ?";
                    pstmt = conn.prepareStatement(SQL);
                    pstmt.setString(1, "%" + keyword + "%");

                    rs = pstmt.executeQuery();

                    while(rs.next()){
                        JSONObject json = new JSONObject();

                        json.put("recipe_name", rs.getString("recipe_nm"));
                        json.put("recipe_image", rs.getString("recipe_img"));
                        json.put("recipe_author", rs.getString("recipe_author"));
                        json.put("recipe_eng", rs.getInt("recipe_eng"));
                        json.put("recipe_nat", rs.getInt("recipe_na"));
                        json.put("recipe_cal", rs.getInt("recipe_cal"));
                        json.put("recipe_pro", rs.getInt("recipe_pro"));
                        json.put("recipe_fat", rs.getInt("recipe_fat"));

                        resultList.add(json);
                    }
                    break;
                case UPDATE:
                    SQL = "";
                    pstmt = conn.prepareStatement(SQL);

                    break;
                case INSERT:
                    System.out.println("INSERT!!!");

                    //레시피 완성 이미지 저장 및 경로 문자열 지정
                    ImageDecoder decoder = new ImageDecoder(recipe);
                    recipeImagePath = decoder.decode(recipe.get("recipe_image").toString(), recipe.get("recipe_name").toString() + "_complete.jpg");

                    //recipe_list테이블에 데이터 삽입
                    SQL = "INSERT INTO recipe_list(recipe_nm, recipe_img, recipe_eng, recipe_cal, recipe_pro, recipe_fat, recipe_na, recipe_author) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    pstmt = conn.prepareStatement(SQL);

                    pstmt.setString(1, recipe.get("recipe_name").toString() == null ? "" : recipe.get("recipe_name").toString());
                    pstmt.setString(2, recipeImagePath);
                    pstmt.setInt(3, recipe.get("recipe_energy").toString() == null ? 0 : Integer.parseInt(recipe.get("recipe_energy").toString()));
                    pstmt.setInt(4, recipe.get("recipe_cal").toString() == null ? 0 : Integer.parseInt(recipe.get("recipe_cal").toString()));
                    pstmt.setInt(5, recipe.get("recipe_pro").toString() == null ? 0 : Integer.parseInt(recipe.get("recipe_pro").toString()));
                    pstmt.setInt(6, recipe.get("recipe_fat").toString() == null ? 0 : Integer.parseInt(recipe.get("recipe_fat").toString()));
                    pstmt.setInt(7, recipe.get("recipe_nat").toString() == null ? 0 : Integer.parseInt(recipe.get("recipe_nat").toString()));
                    pstmt.setString(8, recipe.get("recipe_author").toString() == null ? "guest" : recipe.get("recipe_author").toString());

                    pstmt.executeUpdate();

                    //recipe_manual테이블에 UPDATE쿼리를 통해 데이터 삽입
                    //1. 메뉴얼 등록
                    //1-1. 배열로 메뉴얼 정보를 불러온다.
                    //1-2. 배열의 길이만큼 for문을 돌린다.
                    //1-3. for문 안에서 pstmt.setString()을 통해 SQL에 값을 넣어준다.
                    //1-4. 쿼리를 실행한다.

                    //2. 메뉴얼 이미지 등록
                    //2-1. 메뉴얼 등록과 동일한 방법을 거치지만 아래의 주의사항에 유념한다.
                    //2-2. 메뉴얼 이미지가 등록되지 않은 메뉴얼은 해당 인덱스의 값이 null이 되어야한다.

                    isComplete = true;

                    break;
                case DELETE:
                    SQL = "";
                    pstmt = conn.prepareStatement(SQL);

                    break;
            }
        }
        catch(SQLException e) {
            e.printStackTrace();
            isComplete = false;
        }
    }
}
