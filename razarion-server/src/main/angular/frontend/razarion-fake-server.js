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

function loadModel() {
    return fs.readFileSync(path.join("C:\\dev\\projects\\razarion\\code\\razarion\\razarion-server\\src\\main\\angular\\frontend\\", "threejs-scene.json"));
}

server.on({
    method: 'GET',
    path: '/rest/model',
    reply: {
        status: 200,
        headers: { "content-type": "application/json" },
        body: loadModel
    }
});

function loadImage() {
    return fs.readFileSync(path.join("C:\\dev\\projects\\razarion\\razarion-media\\gimp\\helpers\\", "TextureHelpers512.png"));
}

server.on({
    method: 'GET',
    path: '*',
    filter: function (req) {
        return req.url.startsWith("/image/")
    },
    reply: {
        status: 200,
        headers: { "content-type": "image/png" },
        body: loadImage
    }
});

server.start(function () {
    console.info("Razarion fake server is running on port: " + PORT);
});

