const express = require("express")
const op = require("./openapi").openapi
const app = express();

app.use(express.json());

app.listen(8080, () => {
    console.log("<<==== Server Start : PORT(8080)====>>")
})

app.get("/search-recipe", async (req, res) => {
    console.log(req.query.keyword)
    const response = await op.searchRecipe(req.query.keyword)
    res.json(response)
})