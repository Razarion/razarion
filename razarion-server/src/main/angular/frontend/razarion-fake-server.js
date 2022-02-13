var ServerMock = require("mock-http-server");

const PORT = 8080;

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

// server.on({
//     method: '*',
//     path: '*',
//     filter: function (req) {
//         console.log(req.url);
//         return true;
//     },
// });


server.start(function () {
    console.info("Razarion fake server is running on port: " + PORT);
});

