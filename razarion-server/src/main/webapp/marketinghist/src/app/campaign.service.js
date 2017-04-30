"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
Object.defineProperty(exports, "__esModule", { value: true });
var campaign_1 = require("./campaign");
var http_1 = require("@angular/http");
var core_1 = require("@angular/core");
var CampaignService = (function () {
    // private campaignUrl = 'api/heroes';  // URL to web api
    function CampaignService(http) {
        this.http = http;
        // private campaignUrl = 'http://localhost:8080/rest/marketing/history';  // URL to web api
        this.campaignUrl = '/rest/marketing/history'; // URL to web api
    }
    CampaignService.prototype.getCampaigns = function () {
        return this.http.get(this.campaignUrl)
            .toPromise()
            .then(function (response) {
            return response.json();
        })
            .catch(this.handleError);
    };
    CampaignService.prototype.handleError = function (error) {
        console.error('An error occurred', error);
        return Promise.reject(error.message || error);
    };
    CampaignService.prototype.getCampaigns_old = function () {
        var campaign = new campaign_1.Campaign();
        campaign.title = "title text";
        campaign.body = "body text";
        campaign.clicks = 12;
        campaign.spent = 5.23;
        campaign.impressions = 4052;
        campaign.dateStart = new Date("2017-03-25");
        campaign.dateStop = new Date("2017-03-26");
        campaign.clicksPerHour = [
            { date: new Date(2017, 3, 1, 0, 0, 0), clicks: 5 },
            { date: new Date(2017, 3, 1, 1, 0, 0), clicks: 0 },
            { date: new Date(2017, 3, 1, 2, 0, 0), clicks: 1 },
            { date: new Date(2017, 3, 1, 3, 0, 0), clicks: 3 },
            { date: new Date(2017, 3, 1, 4, 0, 0), clicks: 4 },
            { date: new Date(2017, 3, 1, 5, 0, 0), clicks: 7 },
            { date: new Date(2017, 3, 1, 6, 0, 0), clicks: 2 },
            { date: new Date(2017, 3, 1, 7, 0, 0), clicks: 1 },
            { date: new Date(2017, 3, 1, 8, 0, 0), clicks: 0 },
            { date: new Date(2017, 3, 1, 9, 0, 0), clicks: 8 }
        ];
        var campaigns = [];
        campaigns.push(campaign);
        return campaigns;
    };
    return CampaignService;
}());
CampaignService = __decorate([
    core_1.Injectable(),
    __metadata("design:paramtypes", [http_1.Http])
], CampaignService);
exports.CampaignService = CampaignService;
//# sourceMappingURL=campaign.service.js.map