// Routes studio's dev server to the two mock servers under
// razarion-frontend/mock-server/. Run them via:
//   npm run studio:fake-server   # razarion-fake-server on :8080
//   npm run studio:gwt-mock      # gwt-mock-server      on :9090
//
// Together they let production renderer services (BabylonModelService,
// BabylonRenderServiceAccessImpl, BabylonWaterRenderService.setup, ...)
// load NodeMaterials, heightmaps, ground configs, GLTF, particle systems
// without running Spring Boot.
const PROXY_CONFIG = [
  {
    context: [
      "/rest",
      "/editor",
      "/gz",
      "/razarion-bg.webp"
    ],
    target: "http://127.0.0.1:8080",
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
