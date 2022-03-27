import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { EditorPanel } from "../editor-model";
import { GwtAngularService } from "../../gwtangular/GwtAngularService";
import {
  ObjectNameId,
  RendererEditorService,
  RenderTaskRunnerControl
} from "../../gwtangular/GwtAngularFacade";
import * as Stats from 'stats.js';
import { environment } from 'src/environments/environment';
import { GameMockService } from 'src/app/game/renderer/game-mock.service';
import { Loader } from 'three/editor/js/Loader';
import { ThreeJsRendererServiceImpl } from 'src/app/game/renderer/three-js-renderer-service.impl';
import { Sidebar } from 'three/editor/js/Sidebar';
import { Editor } from 'three/editor/js/Editor';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'render-engine',
  templateUrl: './render-engine.component.html'
})
export class RenderEngineComponent extends EditorPanel implements OnInit, OnDestroy, AfterViewInit {
  @ViewChild('threeJsScene')
  threeJsScene!: ElementRef;
  @ViewChild('selectedDiv')
  selectedDiv!: ElementRef;
  threeJsModels: any[] = [];
  selectedThreeJsName: string | null = null;
  selectedThreeJsType: string | null = null;
  rendererEditorService: RendererEditorService;
  selectedThreeJsModel: any = null;
  renderTaskRunnerControls: RenderTaskRunnerControl[];

  constructor(private gwtAngularService: GwtAngularService,
    private threeJsRendererServiceImpl: ThreeJsRendererServiceImpl,
    gameMockService: GameMockService,
    private messageService: MessageService) {
    super();
    if (environment.gwtMock) {
      this.rendererEditorService = gameMockService.setupRendererEditorService();
      this.renderTaskRunnerControls = [];
      this.threeJsModels = gameMockService.threeJsModels;
      this.threeJsModels = [new class implements ObjectNameId {
        id = 1;
        internalName = "3D Model Palm Tree";
        toString(): string {
          return "3D Model Palm Tree (1)"
        }
      }, new class implements ObjectNameId {
        id = 2;
        internalName = "Rock Pack 3D";
        toString(): string {
          return "Rock Pack 3D (2)"
        }
      }];
    } else {
      this.rendererEditorService = gwtAngularService.gwtAngularFacade.editorFrontendProvider.getCameraFrontendService();
      this.renderTaskRunnerControls = this.rendererEditorService.getRenderTaskRunnerControls();
      gwtAngularService.gwtAngularFacade.editorFrontendProvider.getGenericEditorFrontendProvider().requestObjectNameIds("THREE_JS_MODEL")
        .then(value => this.threeJsModels = value,
          reason => {
            this.messageService.add({
              severity: 'error',
              summary: `Can not load THREE_JS_MODEL configs`,
              detail: reason,
              sticky: true
            });
            console.error(reason);
          });
    }
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.gwtAngularService.gwtAngularFacade.statusProvider.setStats(null);
  }

  ngAfterViewInit(): void {
    if (this.gwtAngularService.gwtAngularFacade.statusProvider.getStats() === undefined
      || this.gwtAngularService.gwtAngularFacade.statusProvider.getStats() === null) {
      this.gwtAngularService.gwtAngularFacade.statusProvider.setStats(new Stats());
    }
    // this.gwtAngularService.gwtAngularFacade.statusProvider.getStats().dom.style.cssText = '';
    // (<HTMLDivElement>this.statsContainer.nativeElement).appendChild(this.gwtAngularService.gwtAngularFacade.statusProvider.getStats().dom)
    // this.gwtAngularService.gwtAngularFacade.statusProvider.getStats().showPanel(0);

    // ----- setup scene property editor -----
    (<any>Number.prototype).format = function () {
      return this.toString().replace(/(\d)(?=(\d{3})+(?!\d))/g, '$1,');
    };

    let editor = new Editor();
    editor.scene = this.threeJsRendererServiceImpl.scene;
    editor.camera = this.threeJsRendererServiceImpl.camera;
    let sidebar = Sidebar(editor);
    this.threeJsScene.nativeElement.appendChild(sidebar.dom);

    let _this = this;
    editor.signals.objectSelected.add(function (selection: any) {
      _this.selectedThreeJsName = selection.name;
      _this.selectedThreeJsType = selection.type;
      let json = selection.toJSON();
      console.info(selection);
    });
  }

  uploadThreejsModel(event: any) {
    let self = this;
    let callback = {
      execute(arg: any) {
        self.threeJsRendererServiceImpl.addToSceneEditor(arg.object);
      }
    }

    let loader = new Loader(callback);
    loader.loadFiles(event.files);
  }

  onUpload() {
    console.log("onUpload()");
  }
}
