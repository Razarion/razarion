import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {EditorPanel} from "../editor-model";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {PerfmonEnum} from "../../gwtangular/GwtAngularFacade";
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

  refresh() {
    const perfmonStatistics = this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getPerfmonStatistics();
    this.rendererPerfmonComponent.display(perfmonStatistics);
    this.updatePerfmonComponent.display(perfmonStatistics);
  }
}
