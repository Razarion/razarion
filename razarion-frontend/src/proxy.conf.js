// A second checkout runs its own backend on another port, set as RAZ_SERVER_PORT in
// razarion-server/docker/.env (see razarion-server/docker/README.md). Without that file it is 8080.
function backendPort() {
  try {
    const env = require('fs').readFileSync(require('path').join(__dirname, '../../razarion-server/docker/.env'), 'utf8');
    const match = env.match(/^\s*RAZ_SERVER_PORT\s*=\s*(\d+)/m);
    return match ? match[1] : '8080';
  } catch (e) {
    return '8080';
  }
}

const PROXY_CONFIG = [
  {
    context: [
      "/teavm-client",
      "/teavm-worker",
      "/rest",
      // Health probe polled by the server-restart overlay to detect when the backend is back.
      // Must be proxied, otherwise the dev server answers index.html with 200 and the page
      // reloads immediately.
      "/actuator",
      "/editor",
      "/gz",
      "/systemconnection",
      "/gameconnection",
      // The boot splash and the loading cover paint these. They live in the server's homepage
      // folder, not in the frontend's public/, so without these entries the dev server answers
      // index.html with 200 and both screens come up without a background.
      "/razarion-bg.webp",
      "/razarion-bg-portrait.webp"
    ],
    target: `http://127.0.0.1:${backendPort()}`,
    secure: false,
    ws: true,
    onProxyRes: function (proxyRes) {
      proxyRes.headers["Cross-Origin-Opener-Policy"] = "same-origin";
      proxyRes.headers["Cross-Origin-Embedder-Policy"] = "credentialless";
    }
  },
  {
    context: [
      "/gwt-mock",
    ],
    target: "http://localhost:9090",
    secure: false,
    ws: true
  }
]

module.exports = PROXY_CONFIG;
