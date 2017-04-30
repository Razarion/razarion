import {Component, Input, OnInit, ViewEncapsulation, ViewChild} from "@angular/core";
import {Campaign} from "./campaign";
import * as d3 from "d3";

@Component({
    selector: 'campaign-detail',
    templateUrl: './campaign-detail.component.html',
    styleUrls: ['./campaign-detail.component.css'],
    encapsulation: ViewEncapsulation.None
})

export class CampaignDetail implements OnInit {
    @Input() campaign: Campaign;
    @ViewChild('svgelement') svgElement : any;
    ngOnInit(): void {
        if (this.campaign.clicksPerHour == null) {
            return;
        }

        let BAR_WIDTH: number = 15;
        let HEIGHT: number = 200;

        let startDate = d3.min(this.campaign.clicksPerHour, function (d) {
            return d.date;
        });
        let endDate = d3.max(this.campaign.clicksPerHour, function (d) {
            return d.date;
        });
        let hourCount = d3.time.hour.range(startDate, endDate);

        let margin = {top: 5, right: 0, bottom: 30, left: 30};
        let width: number = hourCount.length * BAR_WIDTH;
        let height: number = HEIGHT - margin.top - margin.bottom;


        let svg = d3.select(this.svgElement.nativeElement).attr("width", width + margin.left + BAR_WIDTH).attr("height", HEIGHT);

        let g = svg.append("g").attr("transform", "translate(" + margin.left + "," + margin.top + ")");

        let x = d3.time.scale().range([0, width]).domain([startDate, endDate]);

        let y = d3.scale.linear().rangeRound([height, 0]).domain([0, d3.max(this.campaign.clicksPerHour, function (d) {
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
    }
}
