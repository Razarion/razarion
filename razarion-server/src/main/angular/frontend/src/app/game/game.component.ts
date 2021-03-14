import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {FrontendService} from "../service/frontend.service";
import {NavigationStart, Router} from "@angular/router";
import {GwtAngularService} from "../gwtangular/GwtAngularService";
import {EditorModel} from "../editor/editor-model";


@Component({
  templateUrl: 'game.component.html',
  styleUrls: ['game.component.scss']
})
export class GameComponent implements OnInit {
  @ViewChild('canvas', {static: true})
  canvas?: ElementRef<HTMLCanvasElement>;
  // TODO @ViewChild('loadingCover', {static: true})
  // TODO loadingCover?: OverlayPanel;
  editorDialog: boolean = false;
  editorModels: EditorModel[] = [];

  constructor(private frontendService: FrontendService, private router: Router, private gwtAngularService: GwtAngularService) {
  }

  ngOnInit(): void {
    if (this.canvas) {
      this.gwtAngularService.gwtAngularFacade.canvasElement = this.canvas.nativeElement;
    }
    // Prevent running game in the background if someone press the browser history navigation button
    // Proper solution is to stop the game
    this.router.events.subscribe(event => {
      if (event instanceof NavigationStart) {
        window.location.href = event.url;
      }
    });
    // TODO if (!this.frontendService.isCookieAllowed()) {
    //   this.router.navigate(['/nocookies']);
    //   return;
    // }
    this.frontendService.autoLogin().then(loggedIn => {
      this.startGame();
    });
    // TODO if(this.loadingCover) {
    //    this.loadingCover.show(null, this.canvas);
    // }
  }

  private startGame(): void {
    GameComponent.insertGameScript('window.RAZ_startTime = new Date().getTime();');
    GameComponent.insertMeta('gwt:property', "locale=" + this.frontendService.getLanguage());
    GameComponent.insertGameScript('erraiBusRemoteCommunicationEnabled = false;');
    GameComponent.insertGameScript('erraiJaxRsJacksonMarshallingActive = true;');
    GameComponent.loadGameScriptUrl('/NativeRazarion.js');
    GameComponent.loadGameScriptUrl('/razarion_client/razarion_client.nocache.js');
  }

  private static loadGameScriptUrl(url: string) {
    // Check if exits
    let scriptsElements = document.getElementsByTagName('script');
    for (let i = scriptsElements.length; i--;) {
      if (scriptsElements[i].src.startsWith(url)) {
        return;
      }
    }
    // Add
    let scriptObject = document.createElement('script');
    // scriptObject.src = 'http://localhost:8080' + url;
    scriptObject.src = url + '?t=' + new Date().getTime();
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

  showEditorDialog() {
    this.editorDialog = true;
  }

  insertEditorPanel(editorModel: EditorModel) {
    this.editorModels.push(editorModel);
  }
}
