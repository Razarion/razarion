// Routes studio's dev server to the backend: the real razarion-server or the fake server under
// razarion-frontend/mock-server/. Run the mock servers via:
//   npm run studio:fake-server   # razarion-fake-server on :8080
//   npm run studio:gwt-mock      # gwt-mock-server      on :9090
//
// Together they let production renderer services (BabylonModelService,
// BabylonRenderServiceAccessImpl, BabylonWaterRenderService.setup, ...)
// load NodeMaterials, heightmaps, ground configs, GLTF, particle systems
// without running Spring Boot. The terrain lab wants the real planet, so the real server.

// A second checkout runs its own backend on another port, set as RAZ_SERVER_PORT in
// razarion-server/docker/.env (as src/proxy.conf.js does). Without that file it is 8080.
function backendPort() {
  try {
    const env = require('fs').readFileSync(require('path').join(__dirname, '../../../../razarion-server/docker/.env'), 'utf8');
    const match = env.match(/^\s*RAZ_SERVER_PORT\s*=\s*(\d+)/m);
    return match ? match[1] : '8080';
  } catch (e) {
    return '8080';
  }
}

const PROXY_CONFIG = [
  {
    context: [
      "/rest",
      "/editor",
      "/gz",
      "/razarion-bg.webp"
    ],
    target: "http://127.0.0.1:" + backendPort(),
    secure: false,
    ws: true
  },
  {
    context: [
      "/gwt-mock"
    ],
    target: "http://localhost:9090",
    secure: false,
    ws: true
  }
];

module.exports = PROXY_CONFIG;
