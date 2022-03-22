const express = require("express");
const { firestore } = require("firebase-admin");
const fire = require("./firebase").firebase;
const op = require("./openapi").openapi;
const app = express();

app.use(express.json());

app.listen(8080, () => {
    console.log("<<==== Server Start : PORT(8080)====>>")
})

app.get("/search-recipe", async (req, res) => {
    console.log(req.query.keyword)
    const openDataRecipe = await op.searchRecipe(req.query.keyword)
    // const response = await op.searchRecipe(req.query.keyword)

    const firestoreRecipe = await fire.searchRecipe(req.query.keyword)

    const response = {"op_recipe" : openDataRecipe, "fs_recipe": firestoreRecipe}
    console.log(response);
    res.json(response)
})


app.put("/insert-recipe", async (req, res) => {
    console.log("<<==== Recipe Post ====>>")
    console.log(req.body);

    fire.recipeAdd(req.body);
})

app.delete("/delete-recipe", async (req, res) => {
    console.log("<<==== Recipe Delete ====>>")
    console.log(req.body);
    res.sendStatus(200).send({'Response' : 'OK'});
})

app.post("/update-recipe", async (req, res) => {
    console.log("<<==== Recipe Update ====>>");
    console.log(req.body);
    res.sendStatus(200).send({'Response' : 'OK'});
})