import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {EditorPanel} from "../editor-model";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {PerfmonEnum, PerfmonStatistic} from "../../gwtangular/GwtAngularFacade";
import {PerfmonComponent} from "./perfmon.component";

@Component({
  selector: 'render-engine',
  templateUrl: './render-engine.component.html'
})
export class RenderEngineComponent extends EditorPanel implements OnInit, OnDestroy {
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

  constructor(private gwtAngularService: GwtAngularService) {
    super();
  }

  ngOnInit(): void {
    this.refresher = setInterval(() => {
      this.refresh();
    }, 1000);
  }

  ngOnDestroy(): void {
    clearInterval(this.refresher);
  }

  refresh(): void {
    const perfmonStatistics = this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getClientPerfmonStatistics();
    this.display(perfmonStatistics);
    this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getWorkerPerfmonStatistics().then(perfmonStatistics => {
      this.display(perfmonStatistics);
    });
  }

  private display(perfmonStatistics: PerfmonStatistic[]): void {
    this.rendererPerfmonComponent.display(perfmonStatistics);
    this.updatePerfmonComponent.display(perfmonStatistics);
    this.gameEnginePerfmonComponent.display(perfmonStatistics);
  }
}
