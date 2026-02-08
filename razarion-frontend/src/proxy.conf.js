const PROXY_CONFIG = [
  {
    context: [
      "/teavm-client",
      "/teavm-worker",
      "/rest",
      "/gz",
      "/systemconnection",
      "/gameconnection",
      "/razarion-bg.webp"
    ],
    target: "http://127.0.0.1:8080",
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
