var ServerMock = require("mock-http-server");
var fs = require('fs');
const path = require("path");

const PORT = 9090;

let server = new ServerMock({ host: "localhost", port: PORT });

function loadTerrainTiles() {
  return fs.readFileSync(path.join("C:\\dev\\projects\\razarion\\code\\razarion\\razarion-share\\src\\test\\resources\\com\\btxtech\\shared\\gameengine\\planet\\terrain", "terrain-tiles.json"));
}

server.on({
  method: 'get',
  path: '/gwt-mock/terrain-tiles',
  reply: {
    status: 200,
    headers: { "content-type": "application/json" },
    body: loadTerrainTiles
  }
});

function loadStaticGameConfig() {
  return fs.readFileSync(path.join("C:\\dev\\projects\\razarion\\code\\razarion\\razarion-share\\src\\test\\resources\\com\\btxtech\\shared\\gameengine\\planet\\terrain", "static-game-config.json"));
}

server.on({
  method: 'get',
  path: '/gwt-mock/static-game-config',
  reply: {
    status: 200,
    headers: { "content-type": "application/json" },
    body: loadStaticGameConfig
  }
});

server.start(function () {
  console.info("GWT Mock server is running on port: " + PORT);
});
