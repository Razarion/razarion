import {AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {EditorPanel} from "../editor-model";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {
  AngularTreeNodeData,
  GwtAngularPropertyTable,
  RendererEditorService,
  RenderTaskRunnerControl
} from "../../gwtangular/GwtAngularFacade";
import * as Stats from 'stats.js';
import {environment} from 'src/environments/environment';
import {GameMockService} from 'src/app/game/renderer/game-mock.service';
import {GLTFExporter} from 'three/examples/jsm/exporters/GLTFExporter';
import {Loader} from 'three/editor/js/Loader';
import {ThreeJsRendererServiceImpl} from 'src/app/game/renderer/three-js-renderer-service.impl';
import {Sidebar} from 'three/editor/js/Sidebar';
import {Editor} from 'three/editor/js/Editor';
import {MessageService, TreeNode} from 'primeng/api';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {URL_THREE_JS_MODEL_EDITOR} from 'src/app/common';
import {Object3D} from 'three';
import GUI from 'lil-gui';

const IGNORED_THREE_JS_OBJECT_PROPERTIES: string[] = ["parent", "children", "up", "_listeners", "_onChangeCallback"];
const READONLY_THREE_JS_OBJECT_PROPERTIES: string[] = ["uuid", "type"];
let _this: any = null;

@Component({
  selector: 'render-engine',
  templateUrl: './render-engine.component.html'
})
export class RenderEngineComponent extends EditorPanel implements OnInit, OnDestroy, AfterViewInit {
  renderEngineDisplayTree: TreeNode[] = [];
  gwtAngularPropertyTable: GwtAngularPropertyTable | null = null;
  @ViewChild('object3DDisplayELement')
  object3DDisplayELement!: ElementRef;
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
    _this = this;
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

  private static getSpecialSelector(property: any): string | null {
    switch (property.constructor.name) {
      case 'HTMLImageElement':
        return 'image-property-editor';
    }
    return null;
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

  onTreeSelectionChanged(event: any) {
    let object3D = event.node.data;

    let rootTreeNodes: TreeNode<AngularTreeNodeData>[] = [];
    this.recursivelyAddProperty(object3D, rootTreeNodes)

    this.gwtAngularPropertyTable = new class implements GwtAngularPropertyTable {
      configId: number = -999888777;
      rootTreeNodes: TreeNode<AngularTreeNodeData>[] = rootTreeNodes;
    }
    // --- old
    this.object3DDisplayELement.nativeElement.innerHTML = '';
    let gui = new GUI({
      container: this.object3DDisplayELement.nativeElement,
      title: object3D.name
    });
    this._recursivelyAddProperty(object3D, gui);
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

  private recursivelyAddProperty(object3D: Object3D, treeNodes: TreeNode[]) {
    Object.keys(object3D).forEach(function (key, index) {
      if (IGNORED_THREE_JS_OBJECT_PROPERTIES.includes(key)) {
        return;
      }
      const property = (<any>object3D)[key];
      if (typeof property === "object") {
        if (property && (property.length === undefined || property.length > 0)) {
          let specialSelector = RenderEngineComponent.getSpecialSelector(property);
          if (specialSelector != null) {
            treeNodes.push(new class implements TreeNode<AngularTreeNodeData> {
              data = new class implements AngularTreeNodeData {
                canHaveChildren: boolean = false;
                createAllowed: boolean = false;
                deleteAllowed: boolean = false;
                name: string = key;
                nullable: boolean = false;
                options: string[] = [];
                propertyEditorSelector: string = specialSelector != null ? specialSelector : 'Stupid typescript case';
                value: any = property;

                onCreate(gwtAngularPropertyTable: GwtAngularPropertyTable): void {
                }

                onDelete(gwtAngularPropertyTable: GwtAngularPropertyTable): void {
                }

                setValue(value: any): void {
                }

              }
            });
          } else {
            const childTreeNodes: TreeNode[] = [];
            _this.recursivelyAddProperty(property, childTreeNodes);
            treeNodes.push(new class implements TreeNode<AngularTreeNodeData> {
              children = childTreeNodes;
              data = new class implements AngularTreeNodeData {
                canHaveChildren: boolean = true;
                createAllowed: boolean = false;
                deleteAllowed: boolean = false;
                name: string = key;
                nullable: boolean = false;
                options: string[] = [];
                propertyEditorSelector: string = '';
                value: any;

                onCreate(gwtAngularPropertyTable: GwtAngularPropertyTable): void {
                }

                onDelete(gwtAngularPropertyTable: GwtAngularPropertyTable): void {
                }

                setValue(value: any): void {
                }

              }
            });
          }
        } else {
          treeNodes.push(new class implements TreeNode<AngularTreeNodeData> {
            data = new class implements AngularTreeNodeData {
              canHaveChildren: boolean = false;
              createAllowed: boolean = false;
              deleteAllowed: boolean = false;
              name: string = key;
              nullable: boolean = false;
              options: string[] = [];
              propertyEditorSelector: string = '';
              value: any = "?????????";

              onCreate(gwtAngularPropertyTable: GwtAngularPropertyTable): void {
              }

              onDelete(gwtAngularPropertyTable: GwtAngularPropertyTable): void {
              }

              setValue(value: any): void {
              }

            }
          });
        }
      } else {
        treeNodes.push(new class implements TreeNode<AngularTreeNodeData> {
          data = new class implements AngularTreeNodeData {
            canHaveChildren: boolean = false;
            createAllowed: boolean = false;
            deleteAllowed: boolean = false;
            name: string = key;
            nullable: boolean = false;
            options: string[] = [];
            propertyEditorSelector: string = _this.setupPropertyEditorSelector(property);
            value: any = property;

            onCreate(gwtAngularPropertyTable: GwtAngularPropertyTable): void {
            }

            onDelete(gwtAngularPropertyTable: GwtAngularPropertyTable): void {
            }

            setValue(value: any): void {
              (<any>object3D)[key] = value;
            }

          }
        });
        // gui.add(object3D, key).enable(!READONLY_THREE_JS_OBJECT_PROPERTIES.includes(key));
      }
    });
  }

  private setupPropertyEditorSelector(property: any): string {
    if (property == null) {
      return ''
    }
    switch (typeof property) {
      case 'number':
        return 'double-property-editor';
      case 'boolean':
        return 'boolean-property-editor';
      case 'string':
        return 'string-property-editor';
    }
    return ''
  }

  private _recursivelyAddProperty(object3D: Object3D, gui: GUI) {
    Object.keys(object3D).forEach(function (key, index) {
      if (IGNORED_THREE_JS_OBJECT_PROPERTIES.includes(key)) {
        return;
      }
      const property = (<any>object3D)[key];
      if (typeof property === "object") {
        if (property && (property.length === undefined || property.length > 0)) {
          const folder = gui.addFolder(`${key}`);
          folder.add({"jsclass": property.constructor.name}, "jsclass").name("class").enable(false);
          _this._recursivelyAddProperty(property, folder);
          folder.open(false);
        } else {
          gui.add({"null": "-"}, "null").name(key).enable(false);
        }
      } else {
        gui.add(object3D, key).enable(!READONLY_THREE_JS_OBJECT_PROPERTIES.includes(key));
      }
    });
  }

  private initRenderEngineDisplayTree() {
    let camera = this.threeJsRendererServiceImpl.camera;
    this.renderEngineDisplayTree.push(new class implements TreeNode {
      label = camera.name;
      icon = 'pi pi-video';
      data = camera;
    });
    this.renderEngineDisplayTree.push(this.recursivelyAddTreeNodes(this.threeJsRendererServiceImpl.scene));
  }

  private recursivelyAddTreeNodes(object3D: Object3D): TreeNode<Object3D> {
    let children: TreeNode<Object3D>[] = [];

    for (let i = 0, l = object3D.children.length; i < l; i++) {
      const child = object3D.children[i];
      children.push(this.recursivelyAddTreeNodes(child));
    }

    let name = object3D.name;
    let treeNode = new class implements TreeNode<Object3D> {
      label = name;
      icon = 'pi pi-globe';
      children = children;
      data = object3D;
    };
    return treeNode;
  }
}
