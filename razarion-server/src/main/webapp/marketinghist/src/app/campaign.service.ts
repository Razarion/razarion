import {Campaign} from "./campaign";
import {Http} from "@angular/http";
import {Injectable} from "@angular/core";

@Injectable()
export class CampaignService {
    // private campaignUrl = 'http://localhost:8080/rest/marketing/history';  // URL to web api
    private campaignUrl = '/rest/marketing/history';  // URL to web api
    // private campaignUrl = 'api/heroes';  // URL to web api

    constructor(private http: Http) {
    }

    getCampaigns(): Promise<Campaign[]> {
        return this.http.get(this.campaignUrl)
            .toPromise()
            .then(response => {
                return response.json();
            })
            .catch(this.handleError);
    }

    private handleError(error: any): Promise<any> {
        console.error('An error occurred', error);
        return Promise.reject(error.message || error);
    }

    getCampaigns_old(): Campaign[] {
        let campaign: Campaign = new Campaign();
        campaign.title = "title text";
        campaign.body = "body text";
        campaign.clicks = 12;
        campaign.spent = 5.23;
        campaign.impressions = 4052;
        campaign.dateStart = new Date("2017-03-25");
        campaign.dateStop = new Date("2017-03-26");
        campaign.clicksPerHour = [
            {date: new Date(2017, 3, 1, 0, 0, 0), clicks: 5},
            {date: new Date(2017, 3, 1, 1, 0, 0), clicks: 0},
            {date: new Date(2017, 3, 1, 2, 0, 0), clicks: 1},
            {date: new Date(2017, 3, 1, 3, 0, 0), clicks: 3},
            {date: new Date(2017, 3, 1, 4, 0, 0), clicks: 4},
            {date: new Date(2017, 3, 1, 5, 0, 0), clicks: 7},
            {date: new Date(2017, 3, 1, 6, 0, 0), clicks: 2},
            {date: new Date(2017, 3, 1, 7, 0, 0), clicks: 1},
            {date: new Date(2017, 3, 1, 8, 0, 0), clicks: 0},
            {date: new Date(2017, 3, 1, 9, 0, 0), clicks: 8}
        ];

        let campaigns: Campaign[] = [];
        campaigns.push(campaign);
        return campaigns;

    }
}
