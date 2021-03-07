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
    target: "http://localhost:32778",
    secure: false,
    ws: true
  }
]

module.exports = PROXY_CONFIG;
