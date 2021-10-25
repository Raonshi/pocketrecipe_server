package com.raondev.pocketrecipe_server.dbconnect;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.lang.Nullable;

import java.sql.*;

public class DBConnector extends Thread{
    boolean isDev = true;

    String url;
    String user;
    String pw;

    String keyword;
    public void setKeyword(String value){this.keyword = value;}
    public String getKeyword(){return this.keyword;}

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
                    SQL = "INSERT INTO recipe_list (recipe_nm, recipe_img, recipe_eng, recipe_cal, recipe_pro, recipe_fat, recipe_na, recipe_author) VALUES ('recipe_nm 0001', 'recipe_img 0001', 0001, 0001, 0001, 0001, 0001, 'recipe_author 0001')";
                    pstmt = conn.prepareStatement(SQL);

                    break;
                case DELETE:
                    SQL = "";
                    pstmt = conn.prepareStatement(SQL);

                    break;
            }
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
    }


}
