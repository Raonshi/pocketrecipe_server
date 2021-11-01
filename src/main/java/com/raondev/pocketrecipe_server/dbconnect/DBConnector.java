package com.raondev.pocketrecipe_server.dbconnect;

import com.raondev.pocketrecipe_server.ImageCoder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.sql.*;
import java.util.ArrayList;

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


    /**
     * DBConnector 생성자
     * @param function 수행할 쿼리의 종류를 Enum값으로 선택한다.
     */
    public DBConnector(APIService function){
        this.function = function;

        this.url = (isDev) ? "jdbc:mysql://220.86.224.184:12008/pocket_recipe?characterEncoding=utf8" : "jdbc:mysql://127.0.0.1:3306/pocket_recipe";
        this.user = "root";
        this.pw = "tnsdnjs2@";

        getConnection();
    }


    /**
     * JDBC 연결 메소드
     */
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


    /**
     * 쿼리 쓰레드 실행 메소드
     * SELECT, UPDATE, INSERT, DELETE 중 하나를 수행한다.
     */
    @Override
    public void run() {
        try{
            resultList.clear();

            switch(function){
                case SELECT:
                    selectSql();
                    break;
                case UPDATE:
                    updateSql();
                    break;
                case INSERT:
                    insertSql();
                    break;
                case DELETE:
                    deleteSql();
                    break;
            }
            isComplete = true;
        }
        catch(SQLException e) {
            e.printStackTrace();
            isComplete = false;
        }
    }


    /**
     * SELECT 쿼리 메소드
     * @throws SQLException
     */
    void selectSql() throws SQLException {
        PreparedStatement pstmt;
        String SQL = "SELECT * FROM recipe_list WHERE recipe_nm LIKE ?";

        pstmt = conn.prepareStatement(SQL);
        pstmt.setString(1, "%" + keyword + "%");
        ResultSet rs = pstmt.executeQuery();

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
    }


    /**
     * INSERT 쿼리 메소드
     * @throws SQLException
     */
    void insertSql() throws SQLException {
        //레시피 완성 이미지 저장 및 경로 문자열 지정
        ImageCoder decoder = new ImageCoder();
        decoder.recipe = recipe;
        recipeImagePath = decoder.decode(recipe.get("recipe_image").toString(), recipe.get("recipe_name").toString() + "_complete.jpg");

        //레시피 매뉴얼리스트, 이미지리스트 선언
        ArrayList<String> manualList = (ArrayList<String>) recipe.get("recipe_manual");
        ArrayList<String> imageList = new ArrayList<>();

        //매뉴얼 이미지 저장 경로 구하기
        ArrayList<String> tmpImageList = (ArrayList<String>) recipe.get("recipe_manual_image");
        System.out.println("tmpImageList length : " + tmpImageList.size());
        for(int i = 0; i < 20; i++){
            String data = tmpImageList.get(i);
            String imagePath = decoder.decode(data, recipe.get("recipe_name").toString() + "_" + i + ".jpg");
            imageList.add(imagePath);
        }

        //recipe_list테이블에 데이터 삽입
        String SQL = "INSERT INTO recipe_list(" +
                "recipe_nm, recipe_img, recipe_eng, recipe_cal, recipe_pro, recipe_fat, recipe_na, recipe_author, " +
                "image_1, image_2, image_3, image_4, image_5, image_6, image_7, image_8, image_9, image_10, " +
                "image_11, image_12, image_13, image_14, image_15, image_16, image_17, image_18, image_19, image_20," +
                "manual_1, manual_2, manual_3, manual_4, manual_5, manual_6, manual_7, manual_8, manual_9, manual_10," +
                "manual_11, manual_12, manual_13, manual_14, manual_15, manual_16, manual_17, manual_18, manual_19, manual_20) " +
                "VALUES (" +
                "?, ?, ?, ?, ?, ?, ?, ?, " +
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pstmt = conn.prepareStatement(SQL);
        pstmt.setString(1, recipe.get("recipe_name").toString() == null ? "" : recipe.get("recipe_name").toString());
        pstmt.setString(2, recipeImagePath);
        pstmt.setInt(3, recipe.get("recipe_energy").toString() == null ? 0 : Integer.parseInt(recipe.get("recipe_energy").toString()));
        pstmt.setInt(4, recipe.get("recipe_cal").toString() == null ? 0 : Integer.parseInt(recipe.get("recipe_cal").toString()));
        pstmt.setInt(5, recipe.get("recipe_pro").toString() == null ? 0 : Integer.parseInt(recipe.get("recipe_pro").toString()));
        pstmt.setInt(6, recipe.get("recipe_fat").toString() == null ? 0 : Integer.parseInt(recipe.get("recipe_fat").toString()));
        pstmt.setInt(7, recipe.get("recipe_nat").toString() == null ? 0 : Integer.parseInt(recipe.get("recipe_nat").toString()));
        pstmt.setString(8, recipe.get("recipe_author").toString() == null ? "guest" : recipe.get("recipe_author").toString());

        for(int i = 0; i < 20; i++){
            pstmt.setString(i+9, manualList.get(i));
            pstmt.setString(i+29, imageList.get(i));
        }

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
    }


    /**
     * DELETE 쿼리 메소드
     * @throws SQLException
     */
    void deleteSql() throws SQLException {
        //1. 전달된 레시피데이터를 통해 실제로 DB에 존재하는 데이터인지 확인해야한다.

        String SQL = "";
        PreparedStatement pstmt = conn.prepareStatement(SQL);

    }


    /**
     * UPDATE 쿼리 메소드
     * @throws SQLException
     */
    void updateSql() throws SQLException {
        String SQL = "";
        PreparedStatement pstmt = conn.prepareStatement(SQL);
    }
}
