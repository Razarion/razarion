import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MiniViewField } from './mini-view-field';
import { GameUiControl } from 'src/app/gwtangular/GwtAngularFacade';
import { BabylonRenderServiceAccessImpl } from 'src/app/game/renderer/babylon-render-service-access-impl.service';
import { GwtAngularService } from 'src/app/gwtangular/GwtAngularService';
import { MiniTerrain } from './mini-terrain';
import { MiniItemView } from './mini-item-view';
import {Button} from 'primeng/button';
import {Slider} from 'primeng/slider';
import {FormsModule} from '@angular/forms';
import {ViewField, ViewFieldListener} from '../../../renderer/view-field';

@Component({
  selector: 'radar',
  templateUrl: './radar.component.html',
  imports: [
    Button,
    Slider,
    FormsModule
  ],
  styleUrls: ['./radar.component.scss']
})
export class RadarComponent implements ViewFieldListener, OnInit, OnDestroy {
  public static readonly WIDTH = 200;
  public static readonly HEIGHT = 200;
  public static readonly DEFAULT_ZOOM = 13;
  public static readonly MAX_ZOOM = 15;
  public static readonly MINI_MAP_IMAGE_WIDTH = 1000;
  public static readonly MINI_MAP_IMAGE_HEIGHT = 1000;
  private static readonly ZOOM_ANIMATION_DURATION_MS = 2000;
  zoom = 1;
  readonly maxZoom = RadarComponent.MAX_ZOOM;
  private miniViewField: MiniViewField;
  private miniTerrain: MiniTerrain;
  private miniItemView: MiniItemView;
  private zoomAnimationId: number | null = null;

  @ViewChild('miniMapElement', { static: true })
  miniMapElement!: ElementRef<HTMLDivElement>;
  @ViewChild('miniTerrainElement', { static: true })
  miniTerrainElement!: ElementRef<HTMLCanvasElement>;
  @ViewChild('miniViewFieldElement', { static: true })
  miniViewFieldElement!: ElementRef<HTMLCanvasElement>;
  @ViewChild('miniItemViewElement', { static: true })
  miniItemViewElement!: ElementRef<HTMLCanvasElement>;

  constructor(gwtAngularService: GwtAngularService, private renderService: BabylonRenderServiceAccessImpl) {
    this.miniTerrain = new MiniTerrain(gwtAngularService.gwtAngularFacade.gameUiControl, renderService);
    this.miniViewField = new MiniViewField(gwtAngularService.gwtAngularFacade.gameUiControl, renderService);
    this.miniItemView = new MiniItemView(gwtAngularService.gwtAngularFacade.gameUiControl, gwtAngularService.gwtAngularFacade.baseItemUiService, renderService);
  }

  ngOnInit(): void {
    this.zoom = 1;
    this.miniTerrain.init(this.miniTerrainElement.nativeElement, RadarComponent.WIDTH, RadarComponent.HEIGHT, this.zoom);
    this.miniViewField.init(this.miniViewFieldElement.nativeElement, RadarComponent.WIDTH, RadarComponent.HEIGHT, this.zoom);
    this.miniItemView.init(this.miniItemViewElement.nativeElement, RadarComponent.WIDTH, RadarComponent.HEIGHT, this.zoom);

    this.miniMapElement.nativeElement.style.setProperty("width", RadarComponent.WIDTH + "px");
    this.miniMapElement.nativeElement.style.setProperty("height", RadarComponent.HEIGHT + "px");
    this.renderService.addViewFieldListener(this);

    this.miniTerrain.show(() => {
      this.updateMiniMap();
      this.animateZoomIn();
    });
    this.miniItemView.startUpdater();
  }

  ngOnDestroy(): void {
    this.cancelZoomAnimation();
    this.renderService.removeViewFieldListener(this);
    this.miniItemView.stopUpdater();
  }

  onMapClicke(pointerEvent: MouseEvent) {
    let real = this.miniViewField.canvasToReal(pointerEvent.offsetX, pointerEvent.offsetY)
    this.renderService.setViewFieldCenter(real.getX(), real.getY());
  }

  onViewFieldChanged(viewField: ViewField): void {
    this.miniTerrain.setViewField(viewField);
    this.miniViewField.setViewField(viewField);
    this.miniItemView.setViewField(viewField);

    this.updateMiniMap();
  }

  zoomInButtonClick() {
    this.cancelZoomAnimation();
    this.zoom++;
    if (this.zoom > RadarComponent.MAX_ZOOM) {
      this.zoom = RadarComponent.MAX_ZOOM;
    }
    this.changeZoom();
  }

  zoomOuButtonClick() {
    this.cancelZoomAnimation();
    this.zoom--;
    if (this.zoom < 1) {
      this.zoom = 1;
    }
    this.changeZoom();
  }

  onZoomSliderChange() {
    this.cancelZoomAnimation();
    this.changeZoom();
  }

  private animateZoomIn() {
    const startZoom = 1;
    const targetZoom = RadarComponent.DEFAULT_ZOOM;
    const duration = RadarComponent.ZOOM_ANIMATION_DURATION_MS;
    const startTime = performance.now();

    const step = (now: number) => {
      const elapsed = now - startTime;
      const t = Math.min(elapsed / duration, 1);
      const eased = t * t * t * t * t; // ease-in quintic
      this.zoom = t < 1
        ? startZoom + (targetZoom - startZoom) * eased
        : targetZoom;
      this.changeZoom();

      if (t < 1) {
        this.zoomAnimationId = requestAnimationFrame(step);
      } else {
        this.zoomAnimationId = null;
      }
    };

    this.zoomAnimationId = requestAnimationFrame(step);
  }

  private cancelZoomAnimation() {
    if (this.zoomAnimationId !== null) {
      cancelAnimationFrame(this.zoomAnimationId);
      this.zoomAnimationId = null;
    }
  }

  private changeZoom() {
    this.miniTerrain.setZoom(this.zoom);
    this.miniViewField.setZoom(this.zoom);
    this.miniItemView.setZoom(this.zoom);
    this.updateMiniMap();
  }

  private updateMiniMap() {
    this.miniTerrain.update();
    this.miniViewField.update();
    this.miniItemView.update();
  }

}
