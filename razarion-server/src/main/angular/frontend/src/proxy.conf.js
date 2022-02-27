const PROXY_CONFIG = [
  {
    context: [
      "/razarion_client",
      "/NativeRazarion.js",
      "/rest",
      "/images",
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
