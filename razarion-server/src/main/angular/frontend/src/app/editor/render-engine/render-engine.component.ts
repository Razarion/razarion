import {AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {EditorPanel} from "../editor-model";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {
  PerfmonEnum,
  PerfmonStatistic,
  RendererEditorService,
  RenderTaskRunnerControl
} from "../../gwtangular/GwtAngularFacade";
import {PerfmonComponent} from "./perfmon.component";
import * as Stats from 'stats.js';

@Component({
  selector: 'render-engine',
  templateUrl: './render-engine.component.html'
})
export class RenderEngineComponent extends EditorPanel implements OnInit, OnDestroy, AfterViewInit {
  @ViewChild('statsContainer')
  statsContainer!: ElementRef;
  rendererPerfmonType: PerfmonEnum = PerfmonEnum.RENDERER;
  @ViewChild("rendererPerfmon")
  rendererPerfmonComponent!: PerfmonComponent;
  updatePerfmonType: PerfmonEnum = PerfmonEnum.CLIENT_GAME_ENGINE_UPDATE;
  @ViewChild("updatePerfmon")
  updatePerfmonComponent!: PerfmonComponent;
  gameEnginePerfmonType: PerfmonEnum = PerfmonEnum.GAME_ENGINE;
  @ViewChild("gameEnginePerfmon")
  gameEnginePerfmonComponent!: PerfmonComponent;
  refresher: any;
  rendererEditorService: RendererEditorService;
  renderTaskRunnerControls: RenderTaskRunnerControl[];

  constructor(public gwtAngularService: GwtAngularService) {
    super();
    this.rendererEditorService = gwtAngularService.gwtAngularFacade.editorFrontendProvider.getCameraFrontendService();
    this.renderTaskRunnerControls = this.rendererEditorService.getRenderTaskRunnerControls()
  }

  ngOnInit(): void {
    this.refresher = setInterval(() => {
      this.refresh();
    }, 1000);
  }

  ngOnDestroy(): void {
    clearInterval(this.refresher);
    this.gwtAngularService.gwtAngularFacade.statusProvider.setStats(null);
  }

  ngAfterViewInit(): void {
    if (this.gwtAngularService.gwtAngularFacade.statusProvider.getStats() === undefined
      || this.gwtAngularService.gwtAngularFacade.statusProvider.getStats() === null) {
      this.gwtAngularService.gwtAngularFacade.statusProvider.setStats(new Stats());
    }
    this.gwtAngularService.gwtAngularFacade.statusProvider.getStats().dom.style.cssText = '';
    (<HTMLDivElement>this.statsContainer.nativeElement).appendChild(this.gwtAngularService.gwtAngularFacade.statusProvider.getStats().dom)
    this.gwtAngularService.gwtAngularFacade.statusProvider.getStats().showPanel(0);
  }

  refresh(): void {
    const perfmonStatistics = this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getClientPerfmonStatistics();
    this.display(perfmonStatistics);
    this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getWorkerPerfmonStatistics().then(perfmonStatistics => {
      this.display(perfmonStatistics);
    });
  }

  private display(perfmonStatistics: PerfmonStatistic[]): void {
    try {
      this.rendererPerfmonComponent.display(perfmonStatistics);
      this.updatePerfmonComponent.display(perfmonStatistics);
      this.gameEnginePerfmonComponent.display(perfmonStatistics);
    } catch (error) {
      console.error(error);
    }
  }

  toRad(degree: number) {
    return degree * Math.PI / 180;
  }

  onRenderTaskRunnerControl(event: any) {
    console.info(event)
  }
}
