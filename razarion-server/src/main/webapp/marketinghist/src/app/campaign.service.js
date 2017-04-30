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
var http_1 = require("@angular/http");
var core_1 = require("@angular/core");
require("rxjs/add/operator/toPromise");
var CampaignService = (function () {
    function CampaignService(http) {
        this.http = http;
        // private campaignUrl = 'http://localhost:8080/rest/marketing/history';  // URL to web api
        this.campaignUrl = '/rest/marketing/history';
    }
    CampaignService.prototype.getCampaigns = function () {
        return this.http.get(this.campaignUrl)
            .toPromise()
            .then(function (response) {
            return response.json() /*.data*/; // Add .data for in-memory
        })
            .catch(this.handleError);
    };
    CampaignService.prototype.handleError = function (error) {
        console.error('An error occurred', error);
        return Promise.reject(error.message || error);
    };
    return CampaignService;
}());
CampaignService = __decorate([
    core_1.Injectable(),
    __metadata("design:paramtypes", [http_1.Http])
], CampaignService);
exports.CampaignService = CampaignService;
//# sourceMappingURL=campaign.service.js.map