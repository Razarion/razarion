import {Component, ElementRef, HostBinding, OnInit, ViewChild} from '@angular/core';
import {environment} from 'src/environments/environment';
import {CommonModule} from "@angular/common";
import {ScreenCoverComponent} from "./screen-cover/screen-cover.component";
import {GwtAngularService} from "../gwtangular/GwtAngularService";


@Component({
    templateUrl: 'game.component.html',
    imports: [
        CommonModule,
        ScreenCoverComponent
    ],
    styleUrls: ['game.component.scss']
})
export class GameComponent implements OnInit {
    @ViewChild('loadingComponent', {static: true})
    loadingComponent!: ScreenCoverComponent;
    @ViewChild('canvas', {static: true})
    canvas!: ElementRef<HTMLCanvasElement>;
    @HostBinding("style.--cursor")
    cursor: string = '';
    showInventory = false;
    showUnkock = false;


    constructor(private gwtAngularService: GwtAngularService) {
    }

    ngOnInit(): void {
        // TODO this.gwtAngularService.crashListener = () => this.addEditorModel(new EditorModel("Crash Information Panel", CrashPanelComponent));
        // TODO this.gwtAngularService.gwtAngularFacade.modelDialogPresenter = this.modelDialogPresenter;
        // TODO .babylonRenderServiceAccessImpl.setup(this.canvas.nativeElement);
        if (environment.gwtMock) {
            setTimeout(() => {
                this.gwtAngularService.gwtAngularFacade.screenCover.onStartupProgress(25);
            }, 0.25);
            setTimeout(() => {
                this.gwtAngularService.gwtAngularFacade.screenCover.onStartupProgress(50);
            }, 0.5);
            setTimeout(() => {
                this.gwtAngularService.gwtAngularFacade.screenCover.onStartupProgress(100);
            }, 0.75);
            setTimeout(() => {
                this.gwtAngularService.gwtAngularFacade.screenCover.removeLoadingCover();
            }, 1000);
        }

        this.gwtAngularService.gwtAngularFacade.screenCover = this.loadingComponent;

        if (!environment.gwtMock) {
            this.startGame();
        }
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

