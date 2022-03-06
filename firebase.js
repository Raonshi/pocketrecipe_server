// Import the functions you need from the SDKs you need

//Firebase App
const { initializeApp, cert } = require('firebase-admin/app');

//Firebase Firestore
const {getFirestore, Timestamp, FieldValue} = require("firebase-admin/firestore");

//Firebase Storage
const {getStorage} = require("firebase-admin/storage");
const {ref, uploadBytes} = require("firebase/storage");
const multer = require("multer");
const stream = require("stream");

//Firebase Analytics
const { getAnalytics } = require("firebase/analytics");

const serviceAccount = require("./service-account.json");

// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries
class Firebase {
    constructor() {
       initializeApp({
           credential: cert(serviceAccount),
           storageBucket: "gs://pocket-recipe-7b01f.appspot.com",
        });
       this.db = getFirestore();
       console.log("<<==== Firebase Start ====>>")
    }

    
    recipeAdd = async (recipe) => {
        const recipeRef = this.db.collection("recipe").doc(recipe['recipe_name']);

        //레시피 기본 정보 입력
        await recipeRef.set({
            'RCP_NM': recipe['recipe_name'],
            'RCP_PARTS': recipe['recipe_parts'],
            'INFO_ENG': recipe['recipe_energy'],
            'INFO_NA': recipe['recipe_nat'],
            'INFO_CAR': recipe['recipe_cal'],
            'INFO_PRO': recipe['recipe_pro'],
            'INFO_FAT': recipe['recipe_fat'],
            'RCP_AUTHOR': recipe['recipe_author'],
            'MANUALS': recipe['recipe_manual'],
        });
    }

    searchRecipe = async (keyword) => {
        const recipeRef = this.db.collection("recipe");
        const snapshot = await recipeRef.orderBy('RCP_NM').startAt(keyword).endAt(keyword + '\uf8ff').get();

        if(snapshot.empty) {
            console.log('<<==== No Firebase Recipe ====>>')
            return [''];
        }

        var json = snapshot.docs.map(doc => doc.data());
        return json
    }
}

exports.firebase = new Firebase()