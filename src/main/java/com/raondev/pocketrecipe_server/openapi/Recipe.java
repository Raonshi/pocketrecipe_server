package com.raondev.pocketrecipe_server.openapi;


import java.util.ArrayList;

public class Recipe {
    //레시피 이름
    private String name;

    //레시피 완성 이미지
    private String recipeImg;

    //레시피 영양정보
    //index 0 : 칼로리
    //index 1 : 탄수화물
    //index 2 : 단백질
    //index 3 : 지방
    //index 4 : 나트륨
    private ArrayList<String> nutralInfoList = new ArrayList<>();

    //레시피 재료
    private String part;

    //레시피 매뉴얼
    private ArrayList<String> manualList = new ArrayList<>();

    //레시피 매뉴얼 이미지
    private ArrayList<String> manualImgList = new ArrayList<>();


    public void setName(String value){this.name = value;}
    public String getName(){return this.name;}

    public void setRecipeImg(String value){this.recipeImg = value;}
    public String getRecipeImg(){return this.recipeImg;}

    public void addNutralInfoList(int index, String value){this.nutralInfoList.add(index, value);}
    public String getNutralInfoList(int index){return this.nutralInfoList.get(index);}

    public void setPart(String value){this.part = value;}
    public String getPart(){return this.part;}

    public void addManual(String value){manualList.add(value);}
    public String getManual(int index){return manualList.get(index);}

    public void addManualImg(String value){manualImgList.add(value);}
    public String getManualImg(int index){return manualImgList.get(index);}
}
