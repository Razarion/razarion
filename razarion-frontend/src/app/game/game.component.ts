import {Component, ElementRef, HostBinding, NgZone, OnInit, ViewChild} from '@angular/core';
import {environment} from 'src/environments/environment';
import {ScreenCover,} from "../gwtangular/GwtAngularFacade";
import {CommonModule} from "@angular/common";
import {LoadingComponent} from "./loading/loading.component";


@Component({
    templateUrl: 'game.component.html',
    imports: [
        CommonModule,
        LoadingComponent
    ],
    styleUrls: ['game.component.scss']
})
export class GameComponent implements OnInit, ScreenCover {
    @ViewChild('canvas', {static: true})
    canvas!: ElementRef<HTMLCanvasElement>;
    @HostBinding("style.--cursor")
    cursor: string = '';
    showInventory = false;
    showUnkock = false;

    constructor(private zone: NgZone) {
    }

    ngOnInit(): void {
        // this.loadingCover!.render = true;


        if (environment.gwtMock) {
            let runGwtMock = true;
        } else {
        }
    }

    fadeInLoadingCover(): void {
        throw new Error("Not Implemented fadeInLoadingCover()");
    }

    fadeOutLoadingCover(): void {
        this.zone.run(() => {
            // TODO this.fadeOutCover = true;
        });
    }

    hideStoryCover(): void {
        throw new Error("Not Implemented hideStoryCover()");
    }

    removeLoadingCover(): void {
        this.zone.run(() => {
            // TODO this.removeCover = true;
        });
    }

    showStoryCover(html: string): void {
        throw new Error("Not Implemented showStoryCover()");
    }

    private startGame(): void {
        GameComponent.insertGameScript('window.RAZ_startTime = new Date().getTime();');
        // TODO GameComponent.insertMeta('gwt:property', "locale=" + this.frontendService.getLanguage());
        GameComponent.loadGameScriptUrl('/NativeRazarion.js');
        GameComponent.loadGameScriptUrl('/com.btxtech.client.RazarionClient/com.btxtech.client.RazarionClient.nocache.js');
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

    getGameComponent(): GameComponent {
        return this;
    }

    openInventory() {
        this.showInventory = true;
    }

    openUnlock() {
        this.showUnkock = true;
    }


}

