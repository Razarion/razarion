"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var InMemoryDataService = (function () {
    function InMemoryDataService() {
    }
    InMemoryDataService.prototype.createDb = function () {
        var history = [
            {
                dateStart: 0, dateStop: 1493390997072, clicks: 12, impressions: 4000, spent: 5.32, title: "Title", body: "Body", clicksPerHourJsons: [
                    { "date": 1493390997072, "clicks": 10 }, { "date": 1493390997072, "clicks": 14 }
                ]
            },
            {
                dateStart: 0, dateStop: 1493390997072, clicks: 12, impressions: 4000, spent: 5.32, title: "Title", body: "Body", clicksPerHourJsons: [
                    { "date": 1493390997072, "clicks": 10 }, { "date": 1493390997072, "clicks": 14 }
                ]
            }
        ];
        return { history: history };
    };
    return InMemoryDataService;
}());
exports.InMemoryDataService = InMemoryDataService;
//# sourceMappingURL=in-memory-data.service.js.map