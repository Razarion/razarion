const bodyParser = require('body-parser');

const express = require('express');

const app = express();

var ServerMock = require("mock-http-server");
var fs = require('fs');
const path = require("path");
const JSZip = require("jszip");
const zlib = require('zlib');

const PORT = 8080;
let zip = null;

fs.readFile("./resources/BabylonMaterials.zip",
  function (err, data) {
    if (err) throw err;
    JSZip.loadAsync(data).then(function (z) {
      zip = z;
    });
  }
);


app.use(bodyParser.json({limit: '500mb'}));
app.use(bodyParser.urlencoded({limit: '500mb', extended: true}));
app.use(bodyParser.raw({limit: '500mb', type: 'application/octet-stream'}));
app.use(bodyParser.text({limit: '500mb'}));

let server = new ServerMock({host: "127.0.0.1", port: PORT});

server.on({
  method: 'POST',
  path: '/rest/user/auth',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBhZG1pbi5jb20iLCJuYW1lIjoiTWF4IE11c3Rlcm1hbm4iLCJzY29wZSI6IlJPTEVfQURNSU4iLCJleHAiOjE3NTQ4MjAwMDV9.FhaGNR6_VflcNj7CYB7C4EJ5uhqe7JTfcix-xLWqpjE' // Admin
    // body: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6Ik1heCBNdXNlciIsImlhdCI6MTUxNjIzOTAyMn0.DJvLjHkAT44YpiGLAFV1YFxU4kxNOw7M0bN0BMHcQ2s'
  }
});

server.on({
  method: 'GET',
  path: '/rest/user/checkToken',
  reply: {
    status: 200,
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

server.on({
  method: 'POST',
  path: '/rest/remote_logging/angularJsonLogger',
  filter: function (req) {
    console.warn("---- LOG to /rest/remote_logging/angularJsonLogger ---");
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
    headers: {"content-type": "application/json"},
    body: loadThreeJsModel
  }
});

server.on({
  method: 'GET',
  path: '/rest/image/minimap/117',
  reply: {
    status: 200,
    headers: {"content-type": "image/png"},
    body: () => {
      return fs.readFileSync("./resources/minimap-117.png");
    }
  }
});

server.on({
  method: 'PUT',
  path: '/rest/editor/three-js-model/upload/1',
  reply: {
    status: 200,
    headers: {"content-type": "image/png"},
    body: '"OK"'
  }
});

server.on({
  method: 'POST',
  path: '/rest/editor/three-js-model-pack-editor/findByThreeJsModelId/12',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: '[{"id":12,"internalName":"Fern 1 [Tropical Vegetation 1]","threeJsModelId":12,"namePath":["__root__","Sketchfab_model","Vegetation.FBX","RootNode","fern","fern_fern_0"],"position":{"x":0.0,"y":-1.2,"z":0.0},"scale":{"x":0.025,"y":0.025,"z":-0.025},"rotation":{"x":1.5708,"y":0.0,"z":0.0}}]'
  }
});

server.on({
  method: 'POST',
  path: '/rest/editor/three-js-model-pack-editor/create',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: '[{"id":13,"internalName":"Fern 1 [Tropical Vegetation 1]","threeJsModelId":12,"namePath":[]}]'
  }
});

server.on({
  method: 'GET',
  path: '/rest/editor/driveway/objectNameIds',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: '[{"id":1,"internalName":"Driveway 1"},{"id":2,"internalName":"Driveway 2"}]'
  }
});

const serverGameEngineJson = require("./resources/server-game-engine.json");
server.on({
  method: 'GET',
  path: '/rest/editor/server-game-engine/read/3',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: JSON.stringify(serverGameEngineJson)
  }
});

const baseItemTypeJson = require("./resources/base_item_type.json");
server.on({
  method: 'GET',
  path: '/rest/editor/base_item_type/objectNameIds',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: JSON.stringify(baseItemTypeJson)
  }

});
const levelJson = require("./resources/level.json");
server.on({
  method: 'GET',
  path: '/rest/editor/level/objectNameIds',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: JSON.stringify(levelJson.objectNameIds)
  }
});

server.on({
  method: 'GET',
  path: '/rest/editor/level/read',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: JSON.stringify(levelJson.all)
  }
});

const resourceJson = require("./resources/resource_item_type.json");
server.on({
  method: 'GET',
  path: '/rest/editor/resource_item_type/objectNameIds',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: JSON.stringify(resourceJson)
  }
});

const groundsJson = require("./resources/grounds.json");
server.on({
  method: 'GET',
  path: '/rest/editor/ground/objectNameIds',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: JSON.stringify(groundsJson.objectNameIds)
  }
});

const waterJson = require("./resources/waters.json");

server.on({
  method: 'GET',
  path: '/rest/editor/water/read/10',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: JSON.stringify(waterJson._10)
  }
});

server.on({
  method: 'GET',
  path: '/rest/editor/water/objectNameIds',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: JSON.stringify(waterJson.objectNameIds)
  }
});

const particleSystem = require("./resources/particle-system.json");

server.on({
  method: 'GET',
  path: '/rest/editor/particle-system/read/6',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: JSON.stringify(particleSystem._6)
  }
});

server.on({
  method: 'GET',
  path: '/rest/editor/particle-system/objectNameIds',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: JSON.stringify(particleSystem.objectNameIds)
  }
});


server.on({
  method: 'GET',
  path: '/rest/editor/particle-system/data/6',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: function () {
      try {
        const filePath = path.join(__dirname, 'resources', 'nodeParticleSystemSet.json');
        return fs.readFileSync(filePath, 'utf8');
      } catch (err) {
        console.error('Fehler beim Lesen von nodeParticleSystemSet.json:', err);
        return JSON.stringify({error: 'Datei konnte nicht gelesen werden'});
      }
    }
  }
});


server.on({
  method: 'PUT',
  path: '/rest/editor/particle-system/upload/6',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: function (req) {
      console.log("Save particle system: " + req.headers['content-length'] + " content-type: " + req.headers['content-type']);

      try {
        const data = typeof req.body === 'string' ? req.body : JSON.stringify(req.body, null, 2);

        const snipped = JSON.parse(data);
        snipped.jsonPayload = snipped.payload
        snipped.payload = undefined;

        const filePath = path.join(__dirname, 'resources', 'nodeParticleSystemSet.json');
        fs.writeFileSync(filePath, JSON.stringify(snipped), 'utf8');

        console.log(`Particle system JSON wurde gespeichert unter: ${filePath}`);
      } catch (err) {
        console.error('Fehler beim Speichern des Particle-Systems:', err);
      }
      return null;
    }
  }
});


server.on({
  method: 'GET',
  path: '/rest/editor/three-js-model/objectNameIds',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: "[]"
  }
});

let saveTerrainShapeBuffer;

server.on({
  method: 'post',
  path: '/rest/editor/save-terrain-shape',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
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

server.on({
  method: 'get',
  path: '*',
  filter: function (req) {
    return req.url.startsWith("/rest/terrainHeightMap/")
  },
  reply: {
    status: 200,
    headers: {
      "content-type": "application/octet-stream",
      "content-encoding": "gzip"
    },
    body: function loadAssetConfig(req) {
      return fs.readFileSync("./resources/CompressedHeightMap.bin");
    }
  }
});

const uiConfigCollection = require("./resources/ui-config-collection.json");

server.on({
  method: 'get',
  path: '*',
  filter: function (req) {
    return req.url.startsWith("/rest/ui-config-collection/get")
  },
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: JSON.stringify(uiConfigCollection)
  }
});

const myOpenQuests = require("./resources/myOpenQuests.json");

server.on({
  method: 'GET',
  path: '/rest/quest-controller/readMyOpenQuests',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: JSON.stringify(myOpenQuests)
  }
});

server.on({
  method: 'get',
  path: '*',
  filter: function (req) {
    return req.url.startsWith("/rest/editor/brush/read")
  },
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: JSON.stringify([
      {
        "id": 1,
        "internalName": "Water",
        "brushJson": "{\"height\":-0.5,\"diameter\":10,\"maxSlopeWidth\":10,\"slope\":6.97,\"random\":0.53,\"internalName\":\"Water\",\"id\":1}"
      },
      {
        "id": 2,
        "internalName": "Mountain 4m",
        "brushJson": "{\"height\":4,\"diameter\":10,\"maxSlopeWidth\":10,\"slope\":50.61,\"random\":1.16,\"internalName\":\"Mountain 4m\",\"id\":2}"
      }
    ])
  }
});

const gltf = require("./resources/gltf.json");


server.on({
  method: 'GET',
  path: '/rest/gltf/objectNameIds',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: JSON.stringify(gltf.objectNameIds)
  }
});

server.on({
  method: 'GET',
  path: '/rest/gltf/read/1',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: JSON.stringify(gltf._1)
  }
});

server.on({
  method: 'POST',
  path: '/rest/gltf/update',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: function loadAssetConfig(req) {
      console.log("/rest/gltf/update: " + req.headers['content-length'] + " content-type: " + req.headers['content-type']);
    }
  }
});

server.on({
  method: 'GET',
  path: '/rest/gltf/glb/1',
  reply: {
    status: 200,
    headers: {
      "content-type": "application/octet-stream"
    },
    body: function loadAssetConfig(req) {
      return fs.readFileSync("./resources/razarion.glb");
    }
  }
});

server.on({
  method: 'PUT',
  path: '/rest/gltf/upload-glb/1',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: function loadAssetConfig(req) {
      console.log("/rest/gltf/upload-glb/1: " + req.headers['content-length'] + " content-type: " + req.headers['content-type']);
    }
  }
});

const model3D = require("./resources/model3D.json");

server.on({
  method: 'GET',
  path: '/rest/editor/model-3d/getModel3DsByGltf/1',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: JSON.stringify(model3D.model3DsByGltf_1)
  }
});

server.on({
  method: 'POST',
  path: '/rest/editor/model-3d/create',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: JSON.stringify(model3D._1)
  }
});

const terrainObject = require("./resources/terrainObject.json");

server.on({
  method: 'GET',
  path: '/rest/editor/terrain-object/objectNameIds',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: JSON.stringify(terrainObject.objectNameIds)
  }
});

server.on({
  method: 'GET',
  path: '/rest/editor/terrain-object/read/1',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: JSON.stringify(terrainObject._1)
  }
});

server.on({
  method: 'GET',
  path: '/rest/editor/terrain-object-generator/objectNameIds',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: "[]"
  }
});

const babylonMaterial = require("./resources/babylonMaterial.json");

server.on({
  method: 'GET',
  path: '/rest/babylon-material/objectNameIds',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: JSON.stringify(babylonMaterial.objectNameIds)
  }
});

server.on({
  method: 'GET',
  path: '/rest/babylon-material/read/1',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: JSON.stringify(babylonMaterial._1)
  }
});

server.on({
  method: 'GET',
  path: '/rest/inventory-controller/loadInventory',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: JSON.stringify({"crystals": 0, "inventoryItemIds": [2, 2, 2], "inventoryArtifactIds": []})
  }
});

server.on({
  method: 'GET',
  path: '*',
  filter: function (req) {
    return req.url.startsWith("/rest/babylon-material/data/")
  },
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: loadBabylonMaterial
  }
});

server.on({
  method: 'GET',
  path: '/rest/chat-controller/getall',
  reply: {
    status: 200,
    headers: {"content-type": "application/json"},
    body: "[]"
  }
});

function loadBabylonMaterial(req) {
  let babylonMaterialIdToLoad = req.url.substring("/rest/babylon-material/data/".length, req.url.length);

  let zipObject = zip.file(`id_${babylonMaterialIdToLoad}`)
  if (!zipObject || !zipObject._data || !zipObject._data.compressedContent) {
    throw Error(`loadThreeJsModel not found ${req.url}`);
  }

  return zipObject._data.compressedContent;
}

server.start(function () {
  console.info("Razarion fake server is running on port: " + PORT);
});
