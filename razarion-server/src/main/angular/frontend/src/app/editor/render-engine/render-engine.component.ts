import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { EditorPanel } from "../editor-model";
import { GwtAngularService } from "../../gwtangular/GwtAngularService";
import {
  RendererEditorService,
  RenderTaskRunnerControl
} from "../../gwtangular/GwtAngularFacade";
import * as Stats from 'stats.js';
import { environment } from 'src/environments/environment';
import { GameMockService } from 'src/app/game/renderer/game-mock.service';
import { Loader } from 'three/editor/js/Loader';
import { ThreeJsRendererServiceImpl } from 'src/app/game/renderer/three-js-renderer-service.impl';
import { Config } from 'three/editor/js/Config';
import { Strings } from 'three/editor/js/Strings';
import { Sidebar } from 'three/editor/js/Sidebar';
import { History } from 'three/editor/js/History';
import signals from 'signals';

@Component({
  selector: 'render-engine',
  templateUrl: './render-engine.component.html'
})
export class RenderEngineComponent extends EditorPanel implements OnInit, OnDestroy, AfterViewInit {
  @ViewChild('threeJsScene')
  threeJsScene!: ElementRef;
  rendererEditorService: RendererEditorService;
  renderTaskRunnerControls: RenderTaskRunnerControl[];

  constructor(private gwtAngularService: GwtAngularService, private threeJsRendererServiceImpl: ThreeJsRendererServiceImpl, private gameMockService: GameMockService) {
    super();
    if (environment.gwtMock) {
      this.rendererEditorService = gameMockService.setupRendererEditorService();
      this.renderTaskRunnerControls = []
    } else {
      this.rendererEditorService = gwtAngularService.gwtAngularFacade.editorFrontendProvider.getCameraFrontendService();
      this.renderTaskRunnerControls = this.rendererEditorService.getRenderTaskRunnerControls();
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
    let config = Config();
    let strings = Strings(config);

    let signalsEditor = {

      // script

      editScript: new signals.Signal(),

      // player

      startPlayer: new signals.Signal(),
      stopPlayer: new signals.Signal(),

      // vr

      toggleVR: new signals.Signal(),
      exitedVR: new signals.Signal(),

      // notifications

      editorCleared: new signals.Signal(),

      savingStarted: new signals.Signal(),
      savingFinished: new signals.Signal(),

      transformModeChanged: new signals.Signal(),
      snapChanged: new signals.Signal(),
      spaceChanged: new signals.Signal(),
      rendererCreated: new signals.Signal(),
      rendererUpdated: new signals.Signal(),

      sceneBackgroundChanged: new signals.Signal(),
      sceneEnvironmentChanged: new signals.Signal(),
      sceneFogChanged: new signals.Signal(),
      sceneFogSettingsChanged: new signals.Signal(),
      sceneGraphChanged: new signals.Signal(),
      sceneRendered: new signals.Signal(),

      cameraChanged: new signals.Signal(),
      cameraResetted: new signals.Signal(),

      geometryChanged: new signals.Signal(),

      objectSelected: new signals.Signal(),
      objectFocused: new signals.Signal(),

      objectAdded: new signals.Signal(),
      objectChanged: new signals.Signal(),
      objectRemoved: new signals.Signal(),

      cameraAdded: new signals.Signal(),
      cameraRemoved: new signals.Signal(),

      helperAdded: new signals.Signal(),
      helperRemoved: new signals.Signal(),

      materialAdded: new signals.Signal(),
      materialChanged: new signals.Signal(),
      materialRemoved: new signals.Signal(),

      scriptAdded: new signals.Signal(),
      scriptChanged: new signals.Signal(),
      scriptRemoved: new signals.Signal(),

      windowResize: new signals.Signal(),

      showGridChanged: new signals.Signal(),
      showHelpersChanged: new signals.Signal(),
      refreshSidebarObject3D: new signals.Signal(),
      historyChanged: new signals.Signal(),

      viewportCameraChanged: new signals.Signal(),

      animationStopped: new signals.Signal()
    };


    let editor: any = {
      strings: strings,
      config: config,
      camera: this.threeJsRendererServiceImpl.camera,
      scene: this.threeJsRendererServiceImpl.scene,
      scripts: [],
      selected: null,
      signals: signalsEditor,
      history: new History({ config: config, signals: signalsEditor })
    }
    editor.selectById = function (id: any) {
      console.info("selectById: " + id);
    };
    let sidebar = Sidebar(editor);
    this.threeJsScene.nativeElement.appendChild(sidebar.dom);
  }

  uploadThreejsModel(event: any) {
    console.info("Uploading Thrr.js model");

    let self = this;
    let callback = {
      execute(arg: any) {
        self.threeJsRendererServiceImpl.addToSceneEditor(arg.object);
      }
    }

    let loader = new Loader(callback);
    loader.loadFiles(event.files);
  }
}
