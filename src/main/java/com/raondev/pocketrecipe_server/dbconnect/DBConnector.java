package com.raondev.pocketrecipe_server.dbconnect;

import java.sql.*;

public class DBConnector extends Thread{
    boolean isDev = true;

    String url;
    String user;
    String pw;

    Function function;
    Connection conn;

    String SQL = "";

    public DBConnector(Function function){
        this.function = function;

        this.url = (isDev) ? "jdbc:mysql://192.168.0.3:3306/pocket_recipe" : "jdbc:mysql://127.0.0.1:3306/pocket_recipe";
        this.user = "root";
        this.pw = "tnsdnjs2@";

        getConnection();

    }

    void getConnection(){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, user, pw);

            //System.out.println("DataBase Connected!");

            //this.start();
        }
        catch(ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        try{
            String SQL = "";
            PreparedStatement pstat;
            ResultSet rs;

            switch(function){
                case SELECT:
                    SQL = "select * from recipe_list";
                    pstat = conn.prepareStatement(SQL);

                    rs = pstat.getResultSet();

                    break;
                case UPDATE:
                    SQL = "";
                    pstat = conn.prepareStatement(SQL);

                    break;
                case INSERT:
                    SQL = "INSERT INTO recipe_list (recipe_nm, recipe_img, recipe_eng, recipe_cal, recipe_pro, recipe_fat, recipe_na, recipe_author) VALUES ('recipe_nm 0001', 'recipe_img 0001', 0001, 0001, 0001, 0001, 0001, 'recipe_author 0001')";
                    pstat = conn.prepareStatement(SQL);

                    break;
                case DELETE:
                    SQL = "";
                    pstat = conn.prepareStatement(SQL);

                    break;
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }

    }



}
