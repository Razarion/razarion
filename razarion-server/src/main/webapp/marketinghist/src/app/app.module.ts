import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {HttpModule} from "@angular/http";
import {AppComponent} from "./app.component";
import {CampaignList} from "./campaign-list.component";
import {CampaignDetail} from "./campaign-detail.component";
import {CampaignService} from "./campaign.service";

@NgModule({
    imports: [
        BrowserModule,
        FormsModule,
        HttpModule,
        // InMemoryWebApiModule.forRoot(InMemoryDataService, {apiBase: "rest/marketing/"})
    ],
    declarations: [
        AppComponent,
        CampaignList,
        CampaignDetail
    ],
    providers: [CampaignService],
    bootstrap: [AppComponent]
})
export class AppModule {
}
