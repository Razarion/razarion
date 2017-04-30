import {Campaign} from "./campaign";
import {Http} from "@angular/http";
import {Injectable} from "@angular/core";
import 'rxjs/add/operator/toPromise';

@Injectable()
export class CampaignService {
    // private campaignUrl = 'http://localhost:8080/rest/marketing/history';  // URL to web api
    private campaignUrl = '/rest/marketing/history';

    constructor(private http: Http) {
    }

    getCampaigns(): Promise<Campaign[]> {
        return this.http.get(this.campaignUrl)
            .toPromise()
            .then(response => {
                return response.json()/*.data*/; // Add .data for in-memory
            })
            .catch(this.handleError);
    }

    private handleError(error: any): Promise<any> {
        console.error('An error occurred', error);
        return Promise.reject(error.message || error);
    }
}
