const PROXY_CONFIG = [
  {
    context: [
      "/com.btxtech.client.RazarionClient",
      "/com.btxtech.worker.RazarionClientWorker",
      "/NativeRazarion.js",
      "/rest",
      "/gz",
      "/systemconnection",
      "/gameconnection"
    ],
    target: "http://localhost:8080",
    secure: false,
    ws: true
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
