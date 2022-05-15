import {AfterViewInit, Component, OnDestroy} from '@angular/core';
import {EditorPanel} from "../editor-model";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {AngularTreeNodeData, GwtAngularPropertyTable} from "../../gwtangular/GwtAngularFacade";
import * as Stats from 'stats.js';
import {environment} from 'src/environments/environment';
import {GameMockService} from 'src/app/game/renderer/game-mock.service';
import {GLTFExporter} from 'three/examples/jsm/exporters/GLTFExporter';
import {Loader} from 'three/editor/js/Loader';
import {ThreeJsRendererServiceImpl} from 'src/app/game/renderer/three-js-renderer-service.impl';
import {MessageService, TreeNode} from 'primeng/api';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {URL_THREE_JS_MODEL_EDITOR} from 'src/app/common';
import {BufferAttribute, BufferGeometry, Mesh, Object3D, Scene, Vector2} from 'three';
import {ThreeJsPropertyTable} from "./three-js-property-table";
import {ThreeJsTree} from "./three-js-tree";

let _this: any = null;

@Component({
  selector: 'render-engine',
  templateUrl: './render-engine.component.html'
})
export class RenderEngineComponent extends EditorPanel implements OnDestroy, AfterViewInit {
  renderEngineDisplayTree: TreeNode<Object3D>[] = [];
  treeSelection: TreeNode<Object3D> | undefined;
  mouseDownHandler: any;
  gwtAngularPropertyTable: GwtAngularPropertyTable | null = null;
  threeJsModels: any[] = [];
  selectedThreeJsModel: any = null;
  selectedThreeJsName: string | null = null;
  selectedThreeJsObject: any | null = null;
  exportMaterialOnly: boolean = false;

  constructor(private gwtAngularService: GwtAngularService,
              private threeJsRendererServiceImpl: ThreeJsRendererServiceImpl,
              gameMockService: GameMockService,
              private messageService: MessageService,
              private http: HttpClient) {
    super();
    _this = this;
    if (environment.gwtMock) {
      this.threeJsModels = gameMockService.threeJsModels;
    } else {
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
    let threeJsTree = new ThreeJsTree(threeJsRendererServiceImpl);
    this.renderEngineDisplayTree = threeJsTree.getRootTreeNodes();
    this.mouseDownHandler = (event: any) => {
      let object3D = threeJsRendererServiceImpl.intersectObjects(new Vector2(event.clientX, event.clientY));
      if (object3D != null) {
        this.treeSelection = threeJsTree.findTreeNode(object3D);
        threeJsTree.expandParent(this.treeSelection);
        this.displayPropertyTable(object3D)
      }
    }
    threeJsRendererServiceImpl.addMouseDownHandler(this.mouseDownHandler);
  }

  ngOnDestroy(): void {
    this.gwtAngularService.gwtAngularFacade.statusProvider.setStats(null);
    this.threeJsRendererServiceImpl.removeMouseDownHandler(this.mouseDownHandler);
    this.mouseDownHandler = null;
  }

  ngAfterViewInit(): void {
    if (this.gwtAngularService.gwtAngularFacade.statusProvider.getStats() === undefined
      || this.gwtAngularService.gwtAngularFacade.statusProvider.getStats() === null) {
      this.gwtAngularService.gwtAngularFacade.statusProvider.setStats(new Stats());
    }
    // this.gwtAngularService.gwtAngularFacade.statusProvider.getStats().dom.style.cssText = '';
    // (<HTMLDivElement>this.statsContainer.nativeElement).appendChild(this.gwtAngularService.gwtAngularFacade.statusProvider.getStats().dom)
    // this.gwtAngularService.gwtAngularFacade.statusProvider.getStats().showPanel(0);
  }

  onTreeSelectionChanged(event: any) {
    this.displayPropertyTable(event.node.data);
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

  onDump() {
    const exporter = new GLTFExporter();
    try {
      const exporterAny: any = exporter;
      const exportScene = new Scene()
      exportScene.name = "Razarion";
      exportScene.add(this.threeJsRendererServiceImpl.camera)
      exportScene.add(this.threeJsRendererServiceImpl.scene)
      exporterAny.parse(exportScene,
        function (gltf: any) {
          const link = document.createElement("a");
          link.href = URL.createObjectURL(new Blob([gltf]));
          link.setAttribute("download", "main-scene.gltf");
          link.click();
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
        {binary: true});
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

  onSave() {
    let _this = this;
    const exporter = new GLTFExporter();
    try {
      let uploadObject = this.selectedThreeJsObject;
      if (this.exportMaterialOnly) {
        let geometry = new BufferGeometry();
        geometry.setAttribute('position', new BufferAttribute(new Float32Array([-1, -1, 0, 1, -1, 0, -1, 1, 0]), 3));
        uploadObject = new Mesh(geometry, this.selectedThreeJsObject.material);
        uploadObject.name = "Fake Mesh for Material"
      }
      const exporterAny: any = exporter;
      exporterAny.parse(uploadObject,
        function (gltf: any) {
          const httpOptions = {
            headers: new HttpHeaders({
              'Content-Type': 'application/octet-stream'
            })
          };
          _this.http.put(`${URL_THREE_JS_MODEL_EDITOR}/upload/${_this.selectedThreeJsModel.id}`, new Blob([gltf]), httpOptions)
            .subscribe({
              complete: () => _this.messageService.add({
                severity: 'success',
                summary: 'Save successful'
              }),
              error: (error: any) => {
                _this.messageService.add({
                  severity: 'error',
                  summary: `Save failed`,
                  detail: String(error),
                  sticky: true
                });
              }
            })
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
        {binary: true});
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

  private displayPropertyTable(object3D: Object3D) {
    let threeJsPropertyTable = new ThreeJsPropertyTable(object3D);

    this.selectedThreeJsObject = object3D;
    this.selectedThreeJsName = object3D.name;
    this.exportMaterialOnly = false;

    this.gwtAngularPropertyTable = new class implements GwtAngularPropertyTable {
      configId: number = -999888777;
      rootTreeNodes: TreeNode<AngularTreeNodeData>[] = threeJsPropertyTable.getRootTreeNodes();
    }
  }

}
