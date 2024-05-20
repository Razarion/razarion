const bodyParser = require('body-parser');

// Importieren Sie die Express-Bibliothek
const express = require('express');

// Erstellen Sie eine neue Express-Anwendung
const app = express();

var ServerMock = require("mock-http-server");
var fs = require('fs');
const path = require("path");
const JSZip = require("jszip");
const zlib = require('zlib');

const PORT = 8080;
let zip = null;

fs.readFile("C:\\dev\\projects\\razarion\\code\\razarion\\razarion-frontend\\threejs-models\\BabylonJsModels.zip",
  function (err, data) {
    if (err) throw err;
    JSZip.loadAsync(data).then(function (z) {
      zip = z;
    });
  }
);

app.use(bodyParser.json({ limit: '500mb' }));
app.use(bodyParser.urlencoded({ limit: '500mb', extended: true }));
app.use(bodyParser.raw({ limit: '500mb', type: 'application/octet-stream' }));
app.use(bodyParser.text({ limit: '500mb' }));

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

  let zipObject = zip.file(`id_${threeJsModelToLoad}`)
  if (!zipObject || !zipObject._data || !zipObject._data.compressedContent) {
    throw Error(`loadThreeJsModel not found ${req.url}`);
  }

  return zipObject._data.compressedContent;
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
    case '9993':
      imageToLoad = "GroundSplatting.png";
      break;
    case '9994':
      imageToLoad = "WaterCloudReflection.png";
      break;
    case '9995':
      imageToLoad = "WaterNorm.png";
      break;
    case '9996':
      imageToLoad = "Foam.png";
      break;
    case '9997':
      imageToLoad = "FoamDistortion.png";
      break;
    case '9998':
      imageToLoad = "WaterStencil.png";
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

server.on({
  method: 'GET',
  path: '/rest/editor/Svelte-jsoneditor/read/-99999',
  reply: {
    status: 200,
    headers: { "content-type": "application/json" },
    body: '{"value" : -99999}'
  }
});

server.on({
  method: 'POST',
  path: '/rest/editor/Svelte-jsoneditor/update',
  reply: {
    status: 200,
    headers: { "content-type": "application/json" },
    body: '{"value" : -99999}'
  }
});

server.on({
  method: 'POST',
  path: '/rest/editor/three-js-model-pack-editor/findByThreeJsModelId/12',
  reply: {
    status: 200,
    headers: { "content-type": "application/json" },
    body: '[{"id":12,"internalName":"Fern 1 [Tropical Vegetation 1]","threeJsModelId":12,"namePath":["__root__","Sketchfab_model","Vegetation.FBX","RootNode","fern","fern_fern_0"],"position":{"x":0.0,"y":-1.2,"z":0.0},"scale":{"x":0.025,"y":0.025,"z":-0.025},"rotation":{"x":1.5708,"y":0.0,"z":0.0}}]'
  }
});

server.on({
  method: 'POST',
  path: '/rest/editor/three-js-model-pack-editor/create',
  reply: {
    status: 200,
    headers: { "content-type": "application/json" },
    body: '[{"id":13,"internalName":"Fern 1 [Tropical Vegetation 1]","threeJsModelId":12,"namePath":[]}]'
  }
});

server.on({
  method: 'GET',
  path: '/rest/planeteditor/readTerrainSlopePositions/1',
  reply: {
    status: 200,
    headers: { "content-type": "application/json" },
    body: '[{"id":1,"slopeConfigId":1,"inverted":false,"editorParentIdIfCreated":null, "polygon": [{"position": {"x": 20, "y": 20}, "slopeDrivewayId": 1},{"position": {"x": 50, "y": 20}, "slopeDrivewayId": 1},{"position": {"x": 50, "y": 50}, "slopeDrivewayId": null},{"position": {"x": 20, "y": 50}, "slopeDrivewayId": null}]}]'
  }
});

server.on({
  method: 'GET',
  path: '/rest/editor/slope/objectNameIds',
  reply: {
    status: 200,
    headers: { "content-type": "application/json" },
    body: '[{"id":1,"internalName":"Beach"},{"id":2,"internalName":"Razar Industries"}]'
  }
});

server.on({
  method: 'GET',
  path: '/rest/editor/driveway/objectNameIds',
  reply: {
    status: 200,
    headers: { "content-type": "application/json" },
    body: '[{"id":1,"internalName":"Driveway 1"},{"id":2,"internalName":"Driveway 2"}]'
  }
});

const serverGameEngineJson = require("./server-game-engine.json");
server.on({
  method: 'GET',
  path: '/rest/editor/server-game-engine/read/3',
  reply: {
    status: 200,
    headers: { "content-type": "application/json" },
    body: JSON.stringify(serverGameEngineJson)
  }
});

const baseItemTypeJson = require("./base_item_type.json");
server.on({
  method: 'GET',
  path: '/rest/editor/base_item_type/objectNameIds',
  reply: {
    status: 200,
    headers: { "content-type": "application/json" },
    body: JSON.stringify(baseItemTypeJson)
  }

});
const levelJson = require("./level.json");
server.on({
  method: 'GET',
  path: '/rest/editor/level/objectNameIds',
  reply: {
    status: 200,
    headers: { "content-type": "application/json" },
    body: JSON.stringify(levelJson)
  }
});

const resourceJson = require("./resource_item_type.json");
server.on({
  method: 'GET',
  path: '/rest/editor/resource_item_type/objectNameIds',
  reply: {
    status: 200,
    headers: { "content-type": "application/json" },
    body: JSON.stringify(resourceJson)
  }
});

const slopesJson = require("./slopes.json");
server.on({
  method: 'GET',
  path: '/rest/editor/slope/objectNameIds',
  reply: {
    status: 200,
    headers: { "content-type": "application/json" },
    body: JSON.stringify(slopesJson.objectNameIds)
  }
});

server.on({
  method: 'GET',
  path: '/rest/editor/slope/read/1',
  reply: {
    status: 200,
    headers: { "content-type": "application/json" },
    body: JSON.stringify(slopesJson._1)
  }
});

server.on({
  method: 'GET',
  path: '/rest/editor/slope/read/22',
  reply: {
    status: 200,
    headers: { "content-type": "application/json" },
    body: JSON.stringify(slopesJson._22)
  }
});

const groundsJson = require("./grounds.json");
server.on({
  method: 'GET',
  path: '/rest/editor/ground/objectNameIds',
  reply: {
    status: 200,
    headers: { "content-type": "application/json" },
    body: JSON.stringify(groundsJson.objectNameIds)
  }
});

const waterJson = require("./waters.json");

server.on({
  method: 'GET',
  path: '/rest/editor/water/read/10',
  reply: {
    status: 200,
    headers: { "content-type": "application/json" },
    body: JSON.stringify(waterJson._10)
  }
});

server.on({
  method: 'GET',
  path: '/rest/editor/water/objectNameIds',
  reply: {
    status: 200,
    headers: { "content-type": "application/json" },
    body: JSON.stringify(waterJson.objectNameIds)
  }
});

server.on({
  method: 'GET',
  path: '/rest/editor/three-js-model/objectNameIds',
  reply: {
    status: 200,
    headers: { "content-type": "application/json" },
    body: "[]"
  }
});

let saveTerrainShapeBuffer;

server.on({
  method: 'post',
  path: '/rest/editor/save-terrain-shape',
  reply: {
    status: 200,
    headers: { "content-type": "application/json" },
    body: function loadAssetConfig(req) {
      console.log("Save terrain shape content length: " + req.headers['content-length'] + " content-type: " + req.headers['content-type']);
      for (let i = 0; i < req.body.length; i++) {
        console.log(req.body.charCodeAt(i));
      }


      console.log(req.body);
      let decoded = decodeURIComponent(req.body);
      let encoder = new TextEncoder();
      let uint8Array = encoder.encode(decoded);
      console.log(uint8Array);
      zlib.gunzip(req.body, (err, result) => {
        if (err) {
          console.error(err);
        } else {
          console.log(result);
          saveTerrainShapeBuffer = result;
        }
      });
      return "OK";
    }
  }
});

let size = 160 * 160 * 2 * 4;
let terrainShapeBuffer = new Uint8Array(size);
for (let i = 0; i < size; i++) {
  terrainShapeBuffer[i] = 0;
}
let zipTerrainShapeBuffer;
zlib.gzip(terrainShapeBuffer, (err, result) => {
  if (err) {
    console.error(err);
  } else {
    zipTerrainShapeBuffer = result;
  }
});

server.on({
  method: 'get',
  path: '/rest/terrain-shape',
  reply: {
    status: 200,
    headers: {
      "content-type": "application/octet-stream",
      "content-encoding": "gzip"
    },
    body: function loadAssetConfig(req) {
      if (saveTerrainShapeBuffer) {
        return saveTerrainShapeBuffer;
      } else {
        return zipTerrainShapeBuffer;
      }
    }
  }
});


server.start(function () {
  console.info("Razarion fake server is running on port: " + PORT);
});
