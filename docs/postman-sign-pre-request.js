// Postman 6.0+ Pre-request Script
// RiverFlow 开放接口签名：AppKey + AppSecret + HmacSHA256
// 签名字符串：appKey={ak}&nonce={nonce}&timestamp={ts}&body={rawBody}
//
// 使用说明：
// 1. 在 Postman 环境变量中设置 riverflow_appKey、riverflow_appSecret
// 2. 把本脚本贴到 Collection/Folder/Request 的 Pre-request Script 标签页
// 3. 请求发送前会自动注入 X-AppKey / X-Timestamp / X-Nonce / X-Signature

var appKey = pm.environment.get("riverflow_appKey");
var appSecret = pm.environment.get("riverflow_appSecret");

if (!appKey || !appSecret) {
    throw new Error("请在 Postman 环境中设置 riverflow_appKey 和 riverflow_appSecret");
}

var timestamp = Math.floor(Date.now() / 1000).toString();
var nonce = guid();

// 获取请求体原始字符串
// 注意：Postman 6.x 的 pm.request.body 可能为 undefined 或受只读限制，
// 对于 raw 模式通常可用；form-data/urlencoded 需要额外处理
var bodyString = getRequestBodyString();

var stringToSign = "appKey=" + appKey
    + "&nonce=" + nonce
    + "&timestamp=" + timestamp
    + "&body=" + bodyString;

var signature = CryptoJS.HmacSHA256(stringToSign, appSecret).toString(CryptoJS.enc.Hex);

pm.request.headers.add({ key: "X-AppKey", value: appKey });
pm.request.headers.add({ key: "X-Timestamp", value: timestamp });
pm.request.headers.add({ key: "X-Nonce", value: nonce });
pm.request.headers.add({ key: "X-Signature", value: signature });

pm.environment.set("riverflow_timestamp", timestamp);
pm.environment.set("riverflow_nonce", nonce);
pm.environment.set("riverflow_signature", signature);
pm.environment.set("riverflow_stringToSign", stringToSign);

function getRequestBodyString() {
    try {
        var body = pm.request.body;
        if (!body) {
            return "";
        }

        // raw / graphql / file 模式
        if (body.mode === "raw" || body.mode === "graphql") {
            return body.raw ? body.raw : "";
        }

        // urlencoded 模式
        if (body.mode === "urlencoded") {
            return buildUrlEncodedString(body.urlencoded);
        }

        // formdata 模式（仅支持 text 类型的 value，file 类型无法读取二进制内容，按空串处理）
        if (body.mode === "formdata") {
            return buildFormDataString(body.formdata);
        }

        return "";
    } catch (e) {
        console.log("读取请求体失败，使用空字符串: " + e.message);
        return "";
    }
}

function buildUrlEncodedString(urlencoded) {
    if (!urlencoded || !urlencoded.each) {
        return "";
    }
    var params = [];
    urlencoded.each(function (item) {
        if (item && item.key) {
            params.push(encodeURIComponent(item.key) + "=" + encodeURIComponent(item.value || ""));
        }
    });
    return params.sort().join("&");
}

function buildFormDataString(formdata) {
    if (!formdata || !formdata.each) {
        return "";
    }
    var params = [];
    formdata.each(function (item) {
        if (item && item.key && item.type !== "file") {
            params.push(encodeURIComponent(item.key) + "=" + encodeURIComponent(item.value || ""));
        }
    });
    return params.sort().join("&");
}

function guid() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        var r = Math.random() * 16 | 0;
        var v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}
