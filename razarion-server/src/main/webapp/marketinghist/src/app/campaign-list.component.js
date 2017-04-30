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
var core_1 = require("@angular/core");
var campaign_service_1 = require("./campaign.service");
var CampaignList = (function () {
    function CampaignList(campaignService) {
        this.campaignService = campaignService;
        this.campaigns = [];
        this.selected = [];
    }
    CampaignList.prototype.selectionChanged = function (event, campaign) {
        var checkbox = event.srcElement;
        if (checkbox.checked) {
            this.selected.push(campaign);
        }
        else {
            this.selected = this.selected.filter(function (c) { return c !== campaign; });
        }
    };
    CampaignList.prototype.ngOnInit = function () {
        var _this = this;
        this.campaignService.getCampaigns().then(function (campaigns) {
            _this.campaigns = campaigns;
        });
    };
    return CampaignList;
}());
CampaignList = __decorate([
    core_1.Component({
        selector: 'my-campaign',
        templateUrl: './campaign-list.component.html',
        styleUrls: ['./campaign-list.component.css']
    }),
    __metadata("design:paramtypes", [campaign_service_1.CampaignService])
], CampaignList);
exports.CampaignList = CampaignList;
//# sourceMappingURL=campaign-list.component.js.map