import {Component, OnInit} from "@angular/core";
import {Campaign} from "./campaign";
import {CampaignService} from "./campaign.service";

@Component({
    selector: 'campaign-list',
    templateUrl: './campaign-list.component.html',
    styleUrls: ['./campaign-list.component.css']
})
export class CampaignList implements OnInit {
    campaigns: Campaign[];
    selected: Campaign[];

    constructor(private campaignService: CampaignService) {
        this.campaigns = [];
        this.selected = [];
    }

    selectionChanged(event: Event, campaign: Campaign): void {
        let checkbox: HTMLInputElement = <HTMLInputElement> event.srcElement;
        if (checkbox.checked) {
            this.selected.push(campaign)
        } else {
            this.selected = this.selected.filter(c => c !== campaign);
        }
    }

    ngOnInit(): void {
        this.campaignService.getCampaigns().then(campaigns => {
            this.campaigns = campaigns;
        });
    }
}
