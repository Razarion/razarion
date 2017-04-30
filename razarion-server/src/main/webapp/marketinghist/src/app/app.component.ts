import {Component} from "@angular/core";

@Component({
    selector: 'my-app',
    template: `
    <h1>{{title}}</h1>
    <campaign-list></campaign-list>
  `,
    styleUrls: ['./app.component.css']
})
export class AppComponent {
    title = 'Facebook Campaigns';
}
