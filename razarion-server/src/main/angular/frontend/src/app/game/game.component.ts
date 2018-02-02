import {Component, OnInit} from '@angular/core';
import {FrontendService} from "../service/frontend.service";
import {Router} from "@angular/router";


@Component({
  templateUrl: 'game.component.html'
})

// TODO avoid add multiple times (meta, scripts)

export class GameComponent implements OnInit {
  constructor(private frontendService: FrontendService, private router: Router) {
  }

  ngOnInit(): void {
    if (!this.frontendService.isCookieAllowed()) {
      this.router.navigate(['/nocookies']);
      return;
    }
    this.frontendService.autoLogin().then(loggedIn => {
      this.startGame();
    });
  }

  private startGame(): void {
    GameComponent.insertGameScript('window.RAZ_startTime = new Date().getTime();');
    GameComponent.insertMeta('gwt:property', this.frontendService.getLanguage());
    GameComponent.insertGameScript('erraiBusRemoteCommunicationEnabled = false;');
    GameComponent.insertGameScript('erraiJaxRsJacksonMarshallingActive = true;;');
    GameComponent.loadGameScriptUrl('/NativeRazarion.js');
    GameComponent.loadGameScriptUrl('/razarion_client/razarion_client.nocache.js');
  }

  private static loadGameScriptUrl(url: string) {
    let scriptObject = document.createElement('script');
    // scriptObject.src = 'http://localhost:8080' + url;
    scriptObject.src = url;
    scriptObject.type = 'text/javascript';
    scriptObject.charset = 'utf-8';
    document.getElementsByTagName('head')[0].appendChild(scriptObject);
  }

  private static insertGameScript(script: string) {
    let scriptObject = document.createElement('script');
    scriptObject.text = script;
    scriptObject.type = 'text/javascript';
    scriptObject.charset = 'utf-8';
    document.getElementsByTagName('head')[0].appendChild(scriptObject);
  }

  private static insertMeta(name: string, content: string) {
    let meta = document.createElement('meta');
    meta.name = name;
    meta.content = content;
    document.getElementsByTagName('head')[0].appendChild(meta);
  }
}
