import {Component} from "@angular/core";

@Component({
    selector: 'my-app',
    template: `
    <h1>{{title}}</h1>
    <nav>
      <a routerLink="/heroes" routerLinkActive="active">Heroes</a>
      <a routerLink="/dashboard" routerLinkActive="active">Dashboard</a>
    </nav>
    <router-outlet></router-outlet>
  `,
    styleUrls: ['./app.component.css']
})
export class AppComponent {
    title = 'Facebook Campaigns';
}
