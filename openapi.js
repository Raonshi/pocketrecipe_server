const request = require("request")
const urlencode = require("urlencode")

class OpenApi {
    constructor() {
        this.requesturl = "http://openapi.foodsafetykorea.go.kr/api/26ad987100ed4b05baf0/COOKRCP01/json/1/5/RCP_NM=";
    }

    searchRecipe = async (keyword) => {
        const uri = this.requesturl + urlencode(keyword);
        console.log(uri);
        const options = {uri: uri,};
        var data = new Promise((resolve, reject) => {
            request(options, (err, response, body) => {
                if(!err && response.statusCode == 200){
                    console.log("<<==== Search Recipe : Success ==== >>")
                    resolve(body)
                }
                else{
                    console.log("<<==== Search Recipe : Fail ==== >>")
                    reject(err)
                }
            });
        }).then((value) => JSON.parse(value)).then((json) => {
            if(json['COOKRCP01']['row'] == null){
                return ['']
            }
            return json['COOKRCP01']['row']
        });

        
        
        return data;
    }
}

exports.openapi = new OpenApi()