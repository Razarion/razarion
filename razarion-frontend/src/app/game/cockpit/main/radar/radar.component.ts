import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import { MiniViewField } from './mini-view-field';
import { GameUiControl } from 'src/app/gwtangular/GwtAngularFacade';
import { BabylonRenderServiceAccessImpl } from 'src/app/game/renderer/babylon-render-service-access-impl.service';
import { GwtAngularService } from 'src/app/gwtangular/GwtAngularService';
import { MiniTerrain } from './mini-terrain';
import { MiniItemView } from './mini-item-view';
import { MiniQuestMarker } from './mini-quest-marker';
import { QuestMarkerService } from './quest-marker.service';
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
export class RadarComponent implements ViewFieldListener, OnInit, OnChanges, OnDestroy {
  public static readonly WIDTH = 200;
  public static readonly HEIGHT = 200;
  public static readonly DEFAULT_ZOOM = 13;
  public static readonly MAX_ZOOM = 15;
  public static readonly MINI_MAP_IMAGE_WIDTH = 1000;
  public static readonly MINI_MAP_IMAGE_HEIGHT = 1000;
  private static readonly ZOOM_ANIMATION_DURATION_MS = 2000;
  /**
   * Edge length of the map in pixels. The cockpit leaves it at the desktop's 200; the phone corner
   * runs it far smaller and swaps the value when the player expands the map, which is why this is
   * an input the component follows rather than a constant.
   */
  @Input() size = RadarComponent.WIDTH;
  /**
   * Zoom buttons and slider. They are a row of desktop-sized controls under the map - on a corner
   * map barely wider than they are they would take more room than the map itself, and there is
   * nothing to aim them at until the map is expanded.
   */
  @Input() showZoomControls = true;
  /**
   * The map is there from the first minute, for finding your way; a working radar adds what is not
   * yours - enemies and other bases. Before 23.09.2026 the whole map waited for the radar (quests
   * 361/362), and the quests where players lose their target most - 358, 363, 365 - all come first.
   */
  @Input() radarWorking = true;
  /** A tap that moved the camera. The phone layout closes the expanded map on it. */
  @Output() mapClicked = new EventEmitter<void>();
  zoom = 1;
  readonly maxZoom = RadarComponent.MAX_ZOOM;
  private miniViewField!: MiniViewField;
  private miniTerrain!: MiniTerrain;
  private miniItemView!: MiniItemView;
  private miniQuestMarker!: MiniQuestMarker;
  /**
   * Whether the layers exist. They need the engine's GameUiControl and planet, and since the map is
   * on screen from level 1 it is created with the cockpit - before the engine has handed those over.
   * While the radar gated it, the game was always long running by the time the map appeared.
   */
  private started = false;
  private startTimer: ReturnType<typeof setTimeout> | null = null;
  private pulseFrame: number | null = null;
  private static readonly PULSE_MS = 1400;
  /** The pulse redraws only the marker layer, at about 30 frames a second: a 200-pixel canvas. */
  private static readonly PULSE_FRAME_MS = 33;
  private readonly markerListener = () => this.updateQuestMarker();
  private zoomAnimationId: number | null = null;

  @ViewChild('miniMapElement', { static: true })
  miniMapElement!: ElementRef<HTMLDivElement>;
  @ViewChild('miniTerrainElement', { static: true })
  miniTerrainElement!: ElementRef<HTMLCanvasElement>;
  @ViewChild('miniViewFieldElement', { static: true })
  miniViewFieldElement!: ElementRef<HTMLCanvasElement>;
  @ViewChild('miniItemViewElement', { static: true })
  miniItemViewElement!: ElementRef<HTMLCanvasElement>;
  @ViewChild('miniQuestMarkerElement', { static: true })
  miniQuestMarkerElement!: ElementRef<HTMLCanvasElement>;

  constructor(private gwtAngularService: GwtAngularService, private renderService: BabylonRenderServiceAccessImpl,
              private questMarkerService: QuestMarkerService) {
  }

  ngOnInit(): void {
    this.applyElementSize();
    this.startWhenReady();
  }

  /**
   * Whether the renderer's camera gives a usable view field yet. Every layer reads it; asked too
   * early, setupViewField() throws on the missing camera (PROD-local 23.09.2026: 'getViewMatrix' of
   * undefined), and the item layer's redraw loop died of it - own units then showed up only after
   * the player moved the map.
   */
  private viewFieldReady(): boolean {
    const center = this.renderService.getCurrentViewField()?.getCenter();
    return !!center && Number.isFinite(center.getX()) && Number.isFinite(center.getY());
  }

  private startWhenReady(): void {
    const facade = this.gwtAngularService.gwtAngularFacade;
    let ready = false;
    try {
      ready = !!facade?.gameUiControl?.getPlanetConfig() && !!facade.baseItemUiService && this.viewFieldReady();
    } catch (e) {
      ready = false;
    }
    if (!ready) {
      this.startTimer = setTimeout(() => this.startWhenReady(), 500);
      return;
    }
    this.startTimer = null;
    this.miniTerrain = new MiniTerrain(facade.gameUiControl, this.renderService);
    this.miniViewField = new MiniViewField(facade.gameUiControl, this.renderService);
    this.miniItemView = new MiniItemView(facade.gameUiControl, facade.baseItemUiService, this.renderService);
    this.miniQuestMarker = new MiniQuestMarker(facade.gameUiControl, this.renderService);
    this.started = true;
    this.zoom = 1;
    this.miniTerrain.init(this.miniTerrainElement.nativeElement, this.size, this.size, this.zoom);
    this.miniViewField.init(this.miniViewFieldElement.nativeElement, this.size, this.size, this.zoom);
    this.miniItemView.init(this.miniItemViewElement.nativeElement, this.size, this.size, this.zoom);
    this.miniQuestMarker.init(this.miniQuestMarkerElement.nativeElement, this.size, this.size, this.zoom);
    this.miniItemView.setShowForeign(this.radarWorking);
    this.miniQuestMarker.setMarker(this.questMarkerService.get());
    this.questMarkerService.addListener(this.markerListener);
    this.updatePulse();

    this.renderService.addViewFieldListener(this);

    this.miniTerrain.show(() => {
      this.updateMiniMap();
      this.animateZoomIn();
    });
    this.miniItemView.startUpdater();
  }

  ngOnChanges(changes: SimpleChanges): void {
    // The first change arrives before ngOnInit, which does the sizing itself.
    if (!this.started) {
      if (changes['size'] && !changes['size'].firstChange) {
        this.applyElementSize();
      }
      return;
    }
    if (changes['radarWorking'] && !changes['radarWorking'].firstChange) {
      this.miniItemView.setShowForeign(this.radarWorking);
      this.miniItemView.update();
    }
    if (changes['size'] && !changes['size'].firstChange) {
      this.miniTerrain.resize(this.size, this.size);
      this.miniViewField.resize(this.size, this.size);
      this.miniItemView.resize(this.size, this.size);
      this.miniQuestMarker.resize(this.size, this.size);
      this.applyElementSize();
      this.updateMiniMap();
    }
  }

  private applyElementSize(): void {
    this.miniMapElement.nativeElement.style.setProperty("width", this.size + "px");
    this.miniMapElement.nativeElement.style.setProperty("height", this.size + "px");
  }

  ngOnDestroy(): void {
    if (this.startTimer !== null) {
      clearTimeout(this.startTimer);
    }
    if (!this.started) {
      return;
    }
    this.cancelZoomAnimation();
    this.stopPulse();
    this.renderService.removeViewFieldListener(this);
    this.questMarkerService.removeListener(this.markerListener);
    this.miniItemView.stopUpdater();
  }

  onMapClicke(pointerEvent: MouseEvent) {
    if (!this.started) {
      return;
    }
    let real = this.miniViewField.canvasToReal(pointerEvent.offsetX, pointerEvent.offsetY)
    this.renderService.setViewFieldCenter(real.getX(), real.getY());
    // The one camera move that was never counted: without it nobody can tell whether the map is
    // used for finding the way, which is what it is on screen from level 1 for.
    this.renderService.reportFirstInteraction('MINIMAP_JUMP');
    this.mapClicked.emit();
  }

  onViewFieldChanged(viewField: ViewField): void {
    if (!this.started) {
      return;
    }
    this.miniTerrain.setViewField(viewField);
    this.miniViewField.setViewField(viewField);
    this.miniItemView.setViewField(viewField);
    this.miniQuestMarker.setViewField(viewField);

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
    if (!this.started) {
      return;
    }
    this.miniTerrain.setZoom(this.zoom);
    this.miniViewField.setZoom(this.zoom);
    this.miniItemView.setZoom(this.zoom);
    this.miniQuestMarker.setZoom(this.zoom);
    this.updateMiniMap();
  }

  private updateMiniMap() {
    this.miniTerrain.update();
    this.miniViewField.update();
    this.miniItemView.update();
    this.miniQuestMarker.update();
  }

  private updateQuestMarker(): void {
    this.miniQuestMarker.setMarker(this.questMarkerService.get());
    this.miniQuestMarker.update();
    this.updatePulse();
  }

  /** Runs the marker's pulse while there is a marker, and not otherwise. */
  private updatePulse(): void {
    if (!this.miniQuestMarker.hasMarker()) {
      this.stopPulse();
      return;
    }
    if (this.pulseFrame !== null) {
      return;
    }
    let last = 0;
    const step = (now: number) => {
      if (!this.started || !this.miniQuestMarker.hasMarker()) {
        this.pulseFrame = null;
        return;
      }
      if (now - last >= RadarComponent.PULSE_FRAME_MS) {
        last = now;
        this.miniQuestMarker.setPhase((now % RadarComponent.PULSE_MS) / RadarComponent.PULSE_MS);
        try {
          this.miniQuestMarker.update();
        } catch (e) {
          // the next frame tries again
        }
      }
      this.pulseFrame = requestAnimationFrame(step);
    };
    this.pulseFrame = requestAnimationFrame(step);
  }

  private stopPulse(): void {
    if (this.pulseFrame !== null) {
      cancelAnimationFrame(this.pulseFrame);
      this.pulseFrame = null;
    }
  }

}
