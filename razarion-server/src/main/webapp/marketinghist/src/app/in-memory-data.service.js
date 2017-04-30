"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var InMemoryDataService = (function () {
    function InMemoryDataService() {
    }
    InMemoryDataService.prototype.createDb = function () {
        var history = [{
                "adId": "6065557113021",
                "dateStart": 1489795200000,
                "dateStop": 1490034133000,
                "clicks": 20,
                "impressions": 5830,
                "spent": 5.67,
                "title": "Echtzeit Strategiespiel",
                "body": "Razarion vereint packende Echtzeit-Schlachten mit komplexer Strategie",
                "urlTagParam": "0001",
                "adInterests": [{ "id": "6003057392644", "name": "Gaming" }, { "id": "6003253267911", "name": "Command & Conquer" }, { "id": "6003066189670", "name": "Trump" }, {
                        "id": "6003582500438",
                        "name": "Strategy games"
                    }],
                "clicksPerHour": [{ "date": 1489870800000, "clicks": 1 }, { "date": 1489906800000, "clicks": 1 }, { "date": 1489860000000, "clicks": 2 }, {
                        "date": 1489950000000,
                        "clicks": 1
                    }, { "date": 1490000400000, "clicks": 1 }, { "date": 1489986000000, "clicks": 1 }, { "date": 1490011200000, "clicks": 1 }, { "date": 1489795200000, "clicks": 0 }, {
                        "date": 1489910400000,
                        "clicks": 1
                    }, { "date": 1489935600000, "clicks": 3 }, { "date": 1489914000000, "clicks": 1 }, { "date": 1489838400000, "clicks": 1 }, { "date": 1490029200000, "clicks": 1 }, {
                        "date": 1489874400000,
                        "clicks": 1
                    }, { "date": 1489899600000, "clicks": 1 }, { "date": 1490014800000, "clicks": 1 }, { "date": 1489842000000, "clicks": 4 }, { "date": 1489878000000, "clicks": 1 }, {
                        "date": 1489953600000,
                        "clicks": 1
                    }, { "date": 1489827600000, "clicks": 3 }]
            }, {
                "adId": "6065806660821",
                "dateStart": 1490145518000,
                "dateStop": 1490267990000,
                "clicks": 2,
                "impressions": 3976,
                "spent": 2.8,
                "title": "Echtzeit Strategiespiel",
                "body": "Razarion vereint packende Echtzeit-Schlachten mit komplexer Strategie",
                "urlTagParam": "0002",
                "adInterests": [{ "id": "6003704710113", "name": "Real-time strategy" }, { "id": "6003271038593", "name": "Strategy video game" }, {
                        "id": "6003746660346",
                        "name": "StarCraft"
                    }, { "id": "6003099222715", "name": "Characters of StarCraft" }, { "id": "6003176678152", "name": "Automobiles" }, { "id": "6003146718552", "name": "Auto racing" }, {
                        "id": "6003170851134",
                        "name": "Angela Merkel"
                    }],
                "clicksPerHour": [{ "date": 1490252400000, "clicks": 1 }, { "date": 1490144400000, "clicks": 0 }, { "date": 1490176800000, "clicks": 3 }]
            }, {
                "adId": "6066198224221",
                "dateStart": 1490655367000,
                "dateStop": 1490788149000,
                "clicks": 6,
                "impressions": 12674,
                "spent": 2.71,
                "title": "Echtzeit Strategiespiel",
                "body": "Razarion vereint packende Echtzeit-Schlachten mit komplexer Strategie",
                "urlTagParam": "0003",
                "adInterests": [{ "id": "6003070856229", "name": "Games" }, { "id": "6003153672865", "name": "Online games" }, { "id": "6002971095994", "name": "Action games" }, {
                        "id": "6003176101552",
                        "name": "Massively multiplayer online games"
                    }, { "id": "6003280200651", "name": "Play Free Online Games" }, { "id": "6003434373937", "name": "Browser games" }, { "id": "6003114865117", "name": "Browser Game" }, {
                        "id": "6003988007410",
                        "name": "Conquer Online - Best F2P Mmorpg Browser Game"
                    }, { "id": "6003582500438", "name": "Strategy games" }, { "id": "6003163979615", "name": "Abstract strategy game" }, {
                        "id": "6011239361328",
                        "name": "Wargame, the Real-time Strategy Game"
                    }, { "id": "6003143602126", "name": "WAR2 Web Strategy Games" }, { "id": "6003345953274", "name": "Pirates Constructible Strategy Game" }, {
                        "id": "6003704710113",
                        "name": "Real-time strategy"
                    }, { "id": "6003271038593", "name": "Strategy video game" }, { "id": "6003350096301", "name": "Grand strategy wargame" }, {
                        "id": "6003292134610",
                        "name": "Spell-caster (gaming)"
                    }, { "id": "6003746660346", "name": "StarCraft" }, { "id": "6003395009671", "name": "StarCraft II: Wings of Liberty" }, {
                        "id": "6003099222715",
                        "name": "Characters of StarCraft"
                    }, { "id": "6003443545197", "name": "Races of StarCraft" }, { "id": "6010537499192", "name": "StarCraft II: Legacy of the Void" }, {
                        "id": "6003338591187",
                        "name": "starcraft ii wings liberty"
                    }, { "id": "6003153994584", "name": "HuskyStarcraft" }, { "id": "6003365051335", "name": "Starcraft 2 Wings of Liberty" }, {
                        "id": "6003271761380",
                        "name": "StarCraft II: Wings of Liberty (PC)"
                    }],
                "clicksPerHour": [{ "date": 1490695200000, "clicks": 3 }, { "date": 1490778000000, "clicks": 1 }, { "date": 1490702400000, "clicks": 1 }, {
                        "date": 1490652000000,
                        "clicks": 0
                    }, { "date": 1490720400000, "clicks": 1 }, { "date": 1490706000000, "clicks": 1 }]
            }, {
                "adId": "6067562012421",
                "dateStart": 1492107957000,
                "dateStop": 1492257684000,
                "clicks": 4,
                "impressions": 4166,
                "spent": 3.55,
                "title": "Echtzeit Strategiespiel",
                "body": "Razarion vereint packende Echtzeit-Schlachten mit komplexer Strategie und MultiplayerspaÃŸ",
                "urlTagParam": "0004",
                "adInterests": [{ "id": "6003057392644", "name": "Gaming" }, { "id": "6003253267911", "name": "Command & Conquer" }, { "id": "6003210792176", "name": "Donald Trump" }, {
                        "id": "6003582500438",
                        "name": "Strategy games"
                    }],
                "clicksPerHour": [{ "date": 1492149600000, "clicks": 4 }, { "date": 1492214400000, "clicks": 1 }, { "date": 1492106400000, "clicks": 0 }, {
                        "date": 1492200000000,
                        "clicks": 3
                    }, { "date": 1492178400000, "clicks": 1 }]
            }];
        return { history: history };
    };
    return InMemoryDataService;
}());
exports.InMemoryDataService = InMemoryDataService;
//# sourceMappingURL=in-memory-data.service.js.map