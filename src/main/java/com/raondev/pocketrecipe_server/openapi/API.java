package com.raondev.pocketrecipe_server.openapi;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;

public class API {
    String keyId = "26ad987100ed4b05baf0";
    String serviceId = "COOKRCP01";
    String dataType = "xml";
    String startIdx = "1";
    String endIdx = "5";

    //레시피 리스트
    public ArrayList<Recipe> recipeList = new ArrayList<>();


    /**
     * recipeName이 포함되는 모든 음식의 레시피를 조회한다.
     * @param recipeName 찾으려고 하는 레시피 이름
     */
    public void getFromRecipeName(String recipeName){
        recipeList.clear();

        String url = "http://openapi.foodsafetykorea.go.kr/api" +
                "/" + keyId + "/" + serviceId + "/" + dataType + "/" + startIdx + "/" + endIdx +
                "/RCP_NM=" + recipeName;

        try{
            NodeList list = getData(url);

            for(int i = 0; i < list.getLength(); i++){
                Element element = (Element) list.item(i);
                Recipe recipe = new Recipe();

                //레시피 요리 제목 추출
                recipe.setName(getValue("RCP_NM", element));

                //레시피 완성 이미지 추출
                recipe.setRecipeImg(getValue("ATT_FILE_NO_MAIN", element));

                //레시피 영양정보 추출
                recipe.addNutralInfoList(0,getValue("INFO_ENG", element));//열량
                recipe.addNutralInfoList(0,getValue("INFO_CAR", element));//탄수화물
                recipe.addNutralInfoList(0,getValue("INFO_PRO", element));//단백질
                recipe.addNutralInfoList(0,getValue("INFO_FAT", element));//지방
                recipe.addNutralInfoList(0,getValue("INFO_NA", element));//나트륨

                //레시피 재료 추출
                recipe.setPart(getValue("RCP_PARTS_DTLS", element));

                //레시피 설명 추출
                for(int j = 1; j <= 20; j++){
                    if(getValue(String.format("MANUAL%02d", j), element) != null){
                        recipe.addManual(getValue(String.format("MANUAL%02d", j), element));
                    }
                    else{
                        recipe.addManual("No Manual");
                    }
                }

                //레시피 이미지 url 추출
                for(int j = 1; j <= 20; j++){
                    if(getValue(String.format("MANUAL_IMG%02d", j), element) != null){
                        recipe.addManualImg(getValue(String.format("MANUAL_IMG%02d", j), element));
                    }
                    else{
                        recipe.addManualImg("No Image");
                    }
                }


                recipeList.add(recipe);
            }

        }
        catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public NodeList getData(String url) throws ParserConfigurationException, IOException, SAXException {
        Document document = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(url);

        document.getDocumentElement().normalize();

        return document.getElementsByTagName("row");
    }

    public static String getValue(String tag, Element element){
        NodeList list = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node)list.item(0);

        if(node == null){
            return null;
        }
        return node.getNodeValue();
    }
}
