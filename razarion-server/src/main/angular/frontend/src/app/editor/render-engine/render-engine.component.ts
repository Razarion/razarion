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
import { GLTFExporter } from 'three/examples/jsm/exporters/GLTFExporter';
import { Loader } from 'three/editor/js/Loader';
import { ThreeJsRendererServiceImpl } from 'src/app/game/renderer/three-js-renderer-service.impl';
import { Sidebar } from 'three/editor/js/Sidebar';
import { Editor } from 'three/editor/js/Editor';
import { MessageService, TreeNode } from 'primeng/api';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { URL_THREE_JS_MODEL_EDITOR } from 'src/app/common';
import { Object3D } from 'three';

@Component({
  selector: 'render-engine',
  templateUrl: './render-engine.component.html'
})
export class RenderEngineComponent extends EditorPanel implements OnInit, OnDestroy, AfterViewInit {
  renderEngineDisplayTree: TreeNode[] = [];
  @ViewChild('threeJsScene')
  threeJsScene!: ElementRef;
  @ViewChild('selectedDiv')
  selectedDiv!: ElementRef;
  threeJsModels: any[] = [];
  selectedThreeJsModel: any = null;
  selectedThreeJsName: string | null = null;
  selectedThreeJsType: string | null = null;
  selectedThreeJsObject: any | null = null;
  rendererEditorService: RendererEditorService;
  renderTaskRunnerControls: RenderTaskRunnerControl[];

  constructor(private gwtAngularService: GwtAngularService,
    private threeJsRendererServiceImpl: ThreeJsRendererServiceImpl,
    gameMockService: GameMockService,
    private messageService: MessageService,
    private http: HttpClient) {
    super();
    if (environment.gwtMock) {
      this.rendererEditorService = gameMockService.setupRendererEditorService();
      this.renderTaskRunnerControls = [];
      this.threeJsModels = gameMockService.threeJsModels;
    } else {
      this.rendererEditorService = gwtAngularService.gwtAngularFacade.editorFrontendProvider.getCameraFrontendService();
      this.renderTaskRunnerControls = this.rendererEditorService.getRenderTaskRunnerControls();
      gwtAngularService.gwtAngularFacade.editorFrontendProvider.getGenericEditorFrontendProvider().requestObjectNameIds("Three.js Model")
        .then((value: any) => this.threeJsModels = value,
          (reason: any) => {
            console.error(reason);
            this.messageService.add({
              severity: 'error',
              summary: `Can not load THREE_JS_MODEL configs`,
              detail: reason,
              sticky: true
            });
          });
    }
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.gwtAngularService.gwtAngularFacade.statusProvider.setStats(null);
  }

  ngAfterViewInit(): void {
    this.initRenderEngineDisplayTree();

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
      _this.selectedThreeJsObject = selection;
      _this.selectedThreeJsName = selection.name;
      _this.selectedThreeJsType = selection.type;
    });
  }

  private initRenderEngineDisplayTree() {
    let camera =  this.threeJsRendererServiceImpl.camera.name;
    this.renderEngineDisplayTree.push(new class implements TreeNode {
      label = camera;
      icon = 'pi pi-video';
    });
    this.renderEngineDisplayTree.push(this.recursivelyAddTreeNodes(this.threeJsRendererServiceImpl.scene));
  }

  private recursivelyAddTreeNodes(object3D: Object3D): TreeNode {
    let children:TreeNode[] = [];

    for (let i = 0, l = object3D.children.length; i < l; i++) {
        const child = object3D.children[i];
        children.push(this.recursivelyAddTreeNodes(child));
    }

    let name = object3D.name;
    let treeNode = new class implements TreeNode {
      label = name;
      icon = 'pi pi-globe';
      children = children;
    };
    return treeNode;
}

  onImport(event: any) {
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
    let _this = this;
    const exporter = new GLTFExporter();
    try {
      const exporterAny: any = exporter;
      exporterAny.parse(this.selectedThreeJsObject,
        function (gltf: any) {
          console.log(gltf);
          const httpOptions = {
            headers: new HttpHeaders({
              'Content-Type': 'application/octet-stream'
            })
          };
          _this.http.put(`${URL_THREE_JS_MODEL_EDITOR}/upload/${_this.selectedThreeJsModel.id}`, new Blob([gltf]), httpOptions).subscribe();
        },
        function (error: any) {
          console.warn(error);
          _this.messageService.add({
            severity: 'error',
            summary: `Can not export GLTF`,
            detail: String(error),
            sticky: true
          });
        },
        { binary: true });
    } catch (error) {
      console.warn(error);
      this.messageService.add({
        severity: 'error',
        summary: `Can not export GLTF`,
        detail: String(error),
        sticky: true
      });
    }
  }
}
