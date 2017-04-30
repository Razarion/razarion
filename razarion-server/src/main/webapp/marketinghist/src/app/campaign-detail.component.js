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
var campaign_1 = require("./campaign");
var d3 = require("d3");
var CampaignDetail = (function () {
    function CampaignDetail() {
    }
    CampaignDetail.prototype.ngOnInit = function () {
        if (this.campaign.clicksPerHour == null) {
            return;
        }
        var BAR_WIDTH = 15;
        var HEIGHT = 200;
        var startDate = d3.min(this.campaign.clicksPerHour, function (d) {
            return d.date;
        });
        var endDate = d3.max(this.campaign.clicksPerHour, function (d) {
            return d.date;
        });
        var hourCount = d3.time.hour.range(startDate, endDate);
        var margin = { top: 5, right: 0, bottom: 30, left: 30 };
        var width = hourCount.length * BAR_WIDTH;
        var height = HEIGHT - margin.top - margin.bottom;
        var svg = d3.select(this.svgElement.nativeElement).attr("width", width + margin.left + BAR_WIDTH).attr("height", HEIGHT);
        var g = svg.append("g").attr("transform", "translate(" + margin.left + "," + margin.top + ")");
        var x = d3.time.scale().range([0, width]).domain([startDate, endDate]);
        var y = d3.scale.linear().rangeRound([height, 0]).domain([0, d3.max(this.campaign.clicksPerHour, function (d) {
                return d.clicks;
            })]);
        g.append("g")
            .attr("class", "x axis")
            .attr("transform", "translate(0," + height + ")")
            .call(d3.svg.axis().orient("bottom").scale(x).tickFormat(d3.time.format("%H")));
        g.append("g")
            .attr("class", "y axis")
            .call(d3.svg.axis().orient("left").scale(y).ticks(10));
        g.selectAll(".bar")
            .data(this.campaign.clicksPerHour)
            .enter().append("rect")
            .attr("class", "bar")
            .attr("x", function (d) {
            return x(d.date);
        })
            .attr("y", function (d) {
            return y(d.clicks);
        })
            .attr("width", BAR_WIDTH - 1)
            .attr("height", function (d) {
            return height - y(d.clicks);
        });
    };
    return CampaignDetail;
}());
__decorate([
    core_1.Input(),
    __metadata("design:type", campaign_1.Campaign)
], CampaignDetail.prototype, "campaign", void 0);
__decorate([
    core_1.ViewChild('svgelement'),
    __metadata("design:type", Object)
], CampaignDetail.prototype, "svgElement", void 0);
CampaignDetail = __decorate([
    core_1.Component({
        selector: 'campaign-detail',
        templateUrl: './campaign-detail.component.html',
        styleUrls: ['./campaign-detail.component.css'],
        encapsulation: core_1.ViewEncapsulation.None
    })
], CampaignDetail);
exports.CampaignDetail = CampaignDetail;
//# sourceMappingURL=campaign-detail.component.js.map