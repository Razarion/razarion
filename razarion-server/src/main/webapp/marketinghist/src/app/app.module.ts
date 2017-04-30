import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {HttpModule} from "@angular/http";
import {AppRoutingModule} from "./app-routing.module";
// Imports for loading & configuring the in-memory web api
import {InMemoryWebApiModule} from "angular-in-memory-web-api";
import {InMemoryDataService} from "./in-memory-data.service";
import {AppComponent} from "./app.component";
import {DashboardComponent} from "./dashboard.component";
import {HeroesComponent} from "./heroes.component";
import {HeroDetailComponent} from "./hero-detail.component";
import {HeroService} from "./hero.service";
import {HeroSearchComponent} from "./hero-search.component";
import {CampaignList} from "./campaign-list.component";
import {CampaignDetail} from "./campaign-detail.component";
import {CampaignService} from "./campaign.service";

@NgModule({
    imports: [
        BrowserModule,
        FormsModule,
        HttpModule,
        // InMemoryWebApiModule.forRoot(InMemoryDataService, {apiBase: "rest/marketing/"}),
        AppRoutingModule
    ],
    declarations: [
        AppComponent,
        CampaignList,
        CampaignDetail,
        DashboardComponent,
        HeroDetailComponent,
        HeroesComponent,
        HeroSearchComponent
    ],
    providers: [HeroService, CampaignService],
    bootstrap: [AppComponent]
})
export class AppModule {
}
