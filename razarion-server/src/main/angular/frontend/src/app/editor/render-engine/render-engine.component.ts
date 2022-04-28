import {AfterViewInit, Component, ElementRef, OnDestroy, ViewChild} from '@angular/core';
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
import {MessageService, TreeNode} from 'primeng/api';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {URL_THREE_JS_MODEL_EDITOR} from 'src/app/common';
import {BufferAttribute, BufferGeometry, Group, Mesh, Object3D, Scene, Texture} from 'three';

const IGNORED_THREE_JS_OBJECT_PROPERTIES: string[] = ["parent", "children", "up", "_listeners", "_onChangeCallback"];
const READONLY_THREE_JS_OBJECT_PROPERTIES: string[] = ["uuid", "type"];
let _this: any = null;

function setupCreateOptionLabels(createOptions: any[]) {
  let createOptionLabels = []
  for (let i = 0; i < createOptions.length; i++) {
    createOptionLabels.push({"label": createOptions[i].name, "value": i});
  }
  return createOptionLabels;
}

@Component({
  selector: 'render-engine',
  templateUrl: './render-engine.component.html'
})
export class RenderEngineComponent extends EditorPanel implements OnDestroy, AfterViewInit {
  renderEngineDisplayTree: TreeNode[] = [];
  gwtAngularPropertyTable: GwtAngularPropertyTable | null = null;
  @ViewChild('selectedDiv')
  selectedDiv!: ElementRef;
  threeJsModels: any[] = [];
  selectedThreeJsModel: any = null;
  selectedThreeJsName: string | null = null;
  selectedThreeJsObject: any | null = null;
  exportMaterialOnly: boolean = false;
  rendererEditorService: RendererEditorService;
  renderTaskRunnerControls: RenderTaskRunnerControl[];

  createOptions = [
    {
      name: 'Texture',
      exec(): any {
        let texture = new Texture();
        texture.image = new Image();
        return texture
      }
    },
    {
      name: 'Group',
      exec(): any {
        return new Group();
      }
    }
  ]

  createOptionLabels = setupCreateOptionLabels(this.createOptions);

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
    this.initRenderEngineDisplayTree();
  }

  private static getSpecialSelector(property: any): string | null {
    switch (property.constructor.name) {
      case 'HTMLImageElement':
      case 'ImageBitmap':
        return 'image-property-editor';
    }
    return null;
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
  }

  onTreeSelectionChanged(event: any) {
    let object3D = event.node.data;

    let rootTreeNodes: TreeNode<AngularTreeNodeData>[] = [];
    this.recursivelyAddProperty(object3D, null, null, rootTreeNodes)

    this.selectedThreeJsObject = object3D;
    this.selectedThreeJsName = object3D.name;
    this.exportMaterialOnly = false;

    this.gwtAngularPropertyTable = new class implements GwtAngularPropertyTable {
      configId: number = -999888777;
      rootTreeNodes: TreeNode<AngularTreeNodeData>[] = rootTreeNodes;
    }
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

  private recursivelyAddProperty(object3D: Object3D, parent: Object3D | null, parentKey: string | null, treeNodes: TreeNode[]) {
    const _this = this;
    Object.keys(object3D).forEach(function (key, index) {
      if (IGNORED_THREE_JS_OBJECT_PROPERTIES.includes(key)) {
        return;
      }
      const property = (<any>object3D)[key];
      if (typeof property === "object") {
        if (property && (!Array.isArray(property) || property.length > 0)) {
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

                onCreate(gwtAngularPropertyTable: GwtAngularPropertyTable, createOption: any): void {
                }

                onDelete(gwtAngularPropertyTable: GwtAngularPropertyTable): void {
                }

                setValue(value: any): void {
                  if ((<any>object3D).isTexture) {
                    let image = new Image();
                    image.src = value;
                    const texture = (<any>object3D).clone();
                    texture.image = image;
                    texture.needsUpdate = true;
                    (<any>parent)[<string>parentKey] = texture;
                  } else {
                    (<any>object3D)[key] = value;
                  }
                }

              }
            });
          } else {
            const childTreeNodes: TreeNode[] = [];
            _this.recursivelyAddProperty(property, object3D, key, childTreeNodes);
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

                onCreate(gwtAngularPropertyTable: GwtAngularPropertyTable, createOption: any): void {
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
              canHaveChildren: boolean = true;
              createAllowed: boolean = true;
              deleteAllowed: boolean = true;
              name: string = key;
              nullable: boolean = false;
              options: string[] = [];
              propertyEditorSelector: string = '';
              value: any;
              createOptions: any = _this.createOptionLabels;

              onCreate(gwtAngularPropertyTable: GwtAngularPropertyTable, createOption: any): void {
                (<any>object3D)[key] = _this.createOptions[createOption].exec();
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
            propertyEditorSelector: string = RenderEngineComponent.setupPropertyEditorSelector(property);
            value: any = property;

            onCreate(gwtAngularPropertyTable: GwtAngularPropertyTable, createOption: any): void {
            }

            onDelete(gwtAngularPropertyTable: GwtAngularPropertyTable): void {
            }

            setValue(value: any): void {
              (<any>object3D)[key] = value;
            }

          }
        });
        // TODO gui.add(object3D, key).enable(!READONLY_THREE_JS_OBJECT_PROPERTIES.includes(key));
      }
    });
  }

  private static setupPropertyEditorSelector(property: any): string {
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
    return new class implements TreeNode<Object3D> {
      label = name;
      icon = 'pi pi-globe';
      children = children;
      data = object3D;
    };
  }
}
