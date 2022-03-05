// Import the functions you need from the SDKs you need

//Firebase App
const { initializeApp, cert } = require('firebase-admin/app');

//Firebase Firestore
const {getFirestore, Timestamp, FieldValue} = require("firebase-admin/firestore");

//Firebase Analytics
const { getAnalytics } = require("firebase/analytics");

const serviceAccount = require("./service-account.json");

// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries
class Firebase {
    constructor() {
       initializeApp({credential: cert(serviceAccount)});
       this.db = getFirestore();
       console.log("<<==== Firebase Start ====>>")
    }

    
    accountAdd = async () => {
        
    }
}

exports.firebase = new Firebase()