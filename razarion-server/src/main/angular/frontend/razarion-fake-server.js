var ServerMock = require("mock-http-server");
var fs = require('fs');
const path = require("path");

const PORT = 8080;

let server = new ServerMock({ host: "localhost", port: PORT });

server.on({
    method: 'POST',
    path: '/rest/frontend/login',
    reply: {
        status: 200,
        headers: { "content-type": "application/json" },
        body: '"OK"'
    }
});

server.on({
    method: 'POST',
    path: '/rest/frontend/log',
    filter: function (req) {
        console.warn("---- LOG to /rest/frontend/log ---");
        console.warn(req.body);
        return true;
    },
    reply: {
        status: 200
    }
});

function loadThreeJsModel(req) {
    let threeJsModelToLoad = req.url.substring("/rest/gz/three-js-model/".length, req.url.length);
    switch (threeJsModelToLoad) {
        case '8881':
            threeJsModelToLoad = "three-js-model_8881.bin";
            break;
        case '8882':
            threeJsModelToLoad = "three-js-model_8882.bin";
            break;
        case '8883':
            threeJsModelToLoad = "three-js-model_material_ground.gltf";
            break;
        case '8884':
            threeJsModelToLoad = "three_js_model_material_beach.gltf";
            break;
    }
    return fs.readFileSync(path.join("C:\\dev\\projects\\razarion\\code\\razarion\\razarion-server\\src\\main\\angular\\frontend\\threejs-models", threeJsModelToLoad));
}

server.on({
    method: 'GET',
    path: '*',
    filter: function (req) {
        return req.url.startsWith("/rest/gz/three-js-model/")
    },
    reply: {
        status: 200,
        headers: { "content-type": "application/json" },
        body: loadThreeJsModel
    }
});



function loadImage(req) {
    let imageToLoad = req.url.substring("/rest/images/".length - 1, req.url.length);
    switch (imageToLoad) {
        case '9991':
            imageToLoad = "GroundTop.png";
            break;
        case '9992':
            imageToLoad = "GroundTopBm.png";
            break;
    }
    return fs.readFileSync(path.join("C:\\dev\\projects\\razarion\\code\\threejs_razarion\\src\\textures", imageToLoad));
}

server.on({
    method: 'GET',
    path: '*',
    filter: function (req) {
        return req.url.startsWith("/rest/image/")
    },
    reply: {
        status: 200,
        headers: { "content-type": "image/png" },
        body: loadImage
    }
});

server.on({
    method: 'PUT',
    path: '/rest/editor/three-js-model/upload/1',
    reply: {
        status: 200,
        headers: { "content-type": "image/png" },
        body: '"OK"'
    }
});

server.start(function () {
    console.info("Razarion fake server is running on port: " + PORT);
});
