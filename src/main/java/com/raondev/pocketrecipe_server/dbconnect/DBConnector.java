package com.raondev.pocketrecipe_server.dbconnect;

import com.raondev.pocketrecipe_server.FileController;
import com.raondev.pocketrecipe_server.ImageCoder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DBConnector extends Thread{
    boolean isDev = false;

    String url;
    String user;
    String pw;

    String keyword;
    public void setKeyword(String value){this.keyword = value;}
    public String getKeyword(){return this.keyword;}

    String author;
    public void setAuthor(String value){this.author = value;}
    public String getAuthor(){return this.author;}

    JSONObject recipe;
    public void setRecipe(JSONObject value){this.recipe = value;}
    public JSONObject getRecipe(){return this.recipe;}

    JSONObject deleteList;
    public void setDeleteList(JSONObject value){this.deleteList = value;}
    public JSONObject getDeleteList(){return deleteList;}

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
        catch(SQLException | InterruptedException e) {
            e.printStackTrace();
            isComplete = false;
        }
    }


    /**
     * SELECT 쿼리 메소드
     * @throws SQLException
     */
    void selectSql() throws SQLException {
        String SQL;
        PreparedStatement pstmt;
        if(!keyword.equals("SHOW_MY_RECIPE")){
            SQL = "SELECT * FROM recipe_list WHERE recipe_nm LIKE ?";
            pstmt = conn.prepareStatement(SQL);
            pstmt.setString(1, "%" + keyword + "%");
        }
        else{
            SQL = "SELECT * FROM recipe_list WHERE recipe_author LIKE ?";
            pstmt = conn.prepareStatement(SQL);
            pstmt.setString(1,  author);
        }
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

            ArrayList<String> tmpManualList = new ArrayList();
            ArrayList<String> tmpImageList = new ArrayList();
            for(int i = 1; i <= 20; i++){
                tmpManualList.add(rs.getString("manual_" + i));
                tmpImageList.add(rs.getString("image_" + i));
            }

            json.put("recipe_manualList", tmpManualList);
            json.put("recipe_imageList", tmpImageList);

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
        String recipeImage;
        if(recipe.get("recipe_image").toString().equals("Unknown")){
            recipeImage = recipe.get("recipe_image").toString();
        }
        else{
            recipeImage = decoder.decode(recipe.get("recipe_image").toString(), recipe.get("recipe_name").toString() + "_complete.jpg");
        }

        //레시피 매뉴얼리스트, 이미지리스트 선언
        ArrayList<String> manualList = (ArrayList<String>) recipe.get("recipe_manual");
        ArrayList<String> imageList = new ArrayList<>();

        //매뉴얼 이미지 저장 경로 구하기
        ArrayList<String> tmpImageList = (ArrayList<String>) recipe.get("recipe_manual_image");
        System.out.println("tmpImageList length : " + tmpImageList.size());
        for(int i = 0; i < 20; i++){
            String data = tmpImageList.get(i);
            String image;

            if(data.equals("Unknown")){
                image = data;
            }
            else{
                image = decoder.decode(data, recipe.get("recipe_name").toString() + "_" + i + ".jpg");
            }
            imageList.add(image);
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
        pstmt.setString(2, recipeImage);
        pstmt.setInt(3, recipe.get("recipe_energy").toString() == null ? 0 : Integer.parseInt(recipe.get("recipe_energy").toString()));
        pstmt.setInt(4, recipe.get("recipe_cal").toString() == null ? 0 : Integer.parseInt(recipe.get("recipe_cal").toString()));
        pstmt.setInt(5, recipe.get("recipe_pro").toString() == null ? 0 : Integer.parseInt(recipe.get("recipe_pro").toString()));
        pstmt.setInt(6, recipe.get("recipe_fat").toString() == null ? 0 : Integer.parseInt(recipe.get("recipe_fat").toString()));
        pstmt.setInt(7, recipe.get("recipe_nat").toString() == null ? 0 : Integer.parseInt(recipe.get("recipe_nat").toString()));
        pstmt.setString(8, recipe.get("recipe_author").toString() == null ? "guest" : recipe.get("recipe_author").toString());

        for(int i = 0; i < 20; i++){
            pstmt.setString(i+9, imageList.get(i));
            pstmt.setString(i+29, manualList.get(i));
        }

        pstmt.executeUpdate();
    }


    /**
     * DELETE 쿼리 메소드
     * @throws SQLException
     */
    void deleteSql() throws SQLException, InterruptedException {
        try{
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(deleteList.toJSONString());
            JSONArray list = (JSONArray) json.get("recipeList");

            int count = (int) deleteList.get("count");

            for(int i = 0; i < count; i++){
                JSONObject recipe = (JSONObject) list.get(i);

                FileController fcon = new FileController();

                //레시피 이미지폴더 삭제
                String path = fcon.getPath(recipe);
                fcon.delete(path);

                String SQL = "DELETE FROM recipe_list WHERE recipe_nm = ? AND recipe_author = ?";

                PreparedStatement pstmt = conn.prepareStatement(SQL);
                pstmt.setString(1, recipe.get("recipe_name").toString());
                pstmt.setString(2, recipe.get("recipe_author").toString());

                pstmt.executeUpdate();

                Thread.sleep(100);
            }
        }
        catch (ParseException e){
            e.printStackTrace();
        }
    }


    /**
     * UPDATE 쿼리 메소드
     * @throws SQLException
     */
    void updateSql() throws SQLException {
        String SQL = "";
        PreparedStatement pstmt;


        //1. 레시피 조회
        keyword = "SHOW_MY_RECIPE";
        selectSql();

        //2. 사용자가 보낸 레시피 데이터와 동일한 레시피를 찾는다.
        String clientRecipeName = recipe.get("recipe_name").toString();
        String clientRecipeAuthor = recipe.get("recipe_author").toString();


        for(int i = 0; i < resultList.size(); i++){
            JSONObject compareRecipe = (JSONObject) resultList.get(i);
            String compareRecipeName = compareRecipe.get("recipe_name").toString();
            String compareRecipeAuthor = compareRecipe.get("recipe_author").toString();

            //사용자 이름과 사용자가 작성한 레시피 이름을 비교
            if(clientRecipeName.equals(compareRecipeName) && clientRecipeAuthor.equals(compareRecipeAuthor)){
                //일치하면 레코드 전체를 업데이트

                //레시피 완성 이미지 저장 및 경로 문자열 지정
                ImageCoder decoder = new ImageCoder();
                decoder.recipe = recipe;
                String recipeImage;
                if(recipe.get("recipe_image").toString().equals("Unknown")){
                    recipeImage = recipe.get("recipe_image").toString();
                }
                else{
                    recipeImage = decoder.decode(recipe.get("recipe_image").toString(), recipe.get("recipe_name").toString() + "_complete.jpg");
                }

                //레시피 매뉴얼리스트, 이미지리스트 선언
                ArrayList<String> manualList = (ArrayList<String>) recipe.get("recipe_manual");
                ArrayList<String> imageList = new ArrayList<>();

                //매뉴얼 이미지 저장 경로 구하기
                ArrayList<String> tmpImageList = (ArrayList<String>) recipe.get("recipe_manual_image");
                System.out.println("tmpImageList length : " + tmpImageList.size());
                for(int j = 0; j < 20; j++){
                    String data = tmpImageList.get(j);
                    String image;

                    if(data.equals("Unknown")){
                        image = data;
                    }
                    else{
                        image = decoder.decode(data, recipe.get("recipe_name").toString() + "_" + j + ".jpg");
                    }
                    imageList.add(image);
                }


                SQL = "UPDATE recipe_list SET " +
                        "recipe_nm = ?, recipe_img = ?, recipe_eng = ?, recipe_cal = ?, recipe_pro = ?, recipe_fat = ?, recipe_na = ?, recipe_author = ?, " +
                        "image_1 = ?, image_2 = ?, image_3 = ?, image_4 = ?, image_5 = ?, image_6 = ?, image_7 = ?, image_8 = ?, image_9 = ?, image_10 = ?, " +
                        "image_11 = ?, image_12 = ?, image_13 = ?, image_14 = ?, image_15 = ?, image_16 = ?, image_17 = ?, image_18 = ?, image_19 = ?, image_20 = ?, " +
                        "manual_1 = ?, manual_2 = ?, manual_3 = ?, manual_4 = ?, manual_5 = ?, manual_6 = ?, manual_7 = ?, manual_8 = ?, manual_9 = ?, manual_10 = ?, " +
                        "manual_11 = ?, manual_12 = ?, manual_13 = ?, manual_14 = ?, manual_15 = ?, manual_16 = ?, manual_17 = ?, manual_18 = ?, manual_19 = ?, manual_20 = ?; ";
                pstmt = conn.prepareStatement(SQL);

                pstmt.setString(1, recipe.get("recipe_name").toString() == null ? "" : recipe.get("recipe_name").toString());
                pstmt.setString(2, recipeImage);
                pstmt.setInt(3, recipe.get("recipe_energy").toString() == null ? 0 : Integer.parseInt(recipe.get("recipe_energy").toString()));
                pstmt.setInt(4, recipe.get("recipe_cal").toString() == null ? 0 : Integer.parseInt(recipe.get("recipe_cal").toString()));
                pstmt.setInt(5, recipe.get("recipe_pro").toString() == null ? 0 : Integer.parseInt(recipe.get("recipe_pro").toString()));
                pstmt.setInt(6, recipe.get("recipe_fat").toString() == null ? 0 : Integer.parseInt(recipe.get("recipe_fat").toString()));
                pstmt.setInt(7, recipe.get("recipe_nat").toString() == null ? 0 : Integer.parseInt(recipe.get("recipe_nat").toString()));
                pstmt.setString(8, recipe.get("recipe_author").toString() == null ? "guest" : recipe.get("recipe_author").toString());

                for(int j = 0; j < 20; j++){
                    pstmt.setString(j+9, imageList.get(j));
                    pstmt.setString(j+29, manualList.get(j));
                }

            }
        }

    }
}
