import {SlopeContainer} from "./slope-container";
import {Cursor} from "./cursor";
import {Controls} from "./controls";
import {Mode, TerrainEditor} from "./terrain-editor";

export class CanvasController {
  private readonly ctx: CanvasRenderingContext2D;
  private planetSize?: {
    x: number,
    y: number
  }
  private cameraOffset = {x: 0, y: 0};
  private cameraZoom = 1;
  private readonly MAX_ZOOM = 5;
  private readonly MIN_ZOOM = 0.1;
  private readonly SCROLL_SENSITIVITY = 0.0005;
  private isDragging = false;
  private dragStart = {x: 0, y: 0};

  constructor(private canvas: HTMLCanvasElement,
              private canvasDiv: HTMLDivElement,
              private slopeContainer: SlopeContainer,
              private cursor: Cursor,
              private controls: Controls,
              private terrainEditor: TerrainEditor) {
    this.ctx = this.canvas.getContext('2d') as CanvasRenderingContext2D;
    this.setupEventListeners();
    this.draw();
  }

  setPlanetSize(x: number, y: number) {
    this.planetSize = {x: x, y: y};
  }

  private setupEventListeners() {
    this.canvas.addEventListener('mousedown', this.onMouseDown.bind(this));
    this.canvas.addEventListener('mouseup', this.onMouseUp.bind(this));
    this.canvas.addEventListener('mousemove', this.onMouseMove.bind(this));
    this.canvas.addEventListener('wheel', (e) => this.adjustZoom(e.deltaY * this.SCROLL_SENSITIVITY));
  }

  private draw() {
    this.canvas.width = this.canvasDiv.offsetWidth;
    this.canvas.height = this.canvasDiv.offsetHeight;

    this.ctx.translate(0, this.canvasDiv.offsetHeight);


    this.ctx.scale(this.cameraZoom, -this.cameraZoom);
    this.ctx.translate(this.cameraOffset.x, this.cameraOffset.y);
    this.ctx.clearRect(0, 0, this.canvasDiv.offsetWidth, this.canvasDiv.offsetHeight);

    this.drawPlanetSize();

    this.slopeContainer.draw(this.ctx);
    if (this.terrainEditor.mode == Mode.SLOPE_INCREASE || this.terrainEditor.mode == Mode.SLOPE_DECREASE) {
      this.cursor.draw(this.ctx);
    }

    requestAnimationFrame(this.draw.bind(this));
  }

  private getEventLocation(e: MouseEvent | TouchEvent): { x: number, y: number } {
    if (e instanceof MouseEvent && e.offsetX && e.offsetY) {
      return {x: e.offsetX, y: e.offsetY};
    }
    return {x: 0, y: 0};
  }

  private onMouseDown(e: MouseEvent) {
    switch (this.terrainEditor?.mode) {
      case Mode.SELECT: {
        if (this.slopeContainer.getHoverContext()) {
          this.controls.selectedSLope = this.slopeContainer.getHoverContext()?.getIntersectSlope();
        }
        return;
      }
      case Mode.PANNING: {
        this.isDragging = true;
        this.dragStart.x = this.getEventLocation(e).x / this.cameraZoom - this.cameraOffset.x;
        this.dragStart.y = (this.canvasDiv.offsetHeight - this.getEventLocation(e).y) / this.cameraZoom - this.cameraOffset.y;
        return;
      }
      case Mode.SLOPE_INCREASE:
      case Mode.SLOPE_DECREASE: {
        this.slopeContainer.recalculateHoverContext(this.cursor.getPolygon());
        if (this.cursor.getPolygon()) {
          this.slopeContainer.manipulate(this.controls, this.terrainEditor.mode === Mode.SLOPE_INCREASE, this.cursor.getPolygon());
        }
        return;
      }
      case Mode.DRIVEWAY_INCREASE:
      case Mode.DRIVEWAY_DECREASE:
      default: {

        return;
      }
    }
  }

  private onMouseUp(e: MouseEvent | TouchEvent) {
    this.isDragging = false;
  }

  private onMouseMove(mouseEvent: MouseEvent) {
    let x = this.getEventLocation(mouseEvent).x / this.cameraZoom - this.cameraOffset.x;
    let y = (this.canvasDiv.offsetHeight - this.getEventLocation(mouseEvent).y) / this.cameraZoom - this.cameraOffset.y;
    this.controls.xPos = x;
    this.controls.yPos = y;
    this.cursor.move(x, y);


    switch (this.terrainEditor?.mode) {
      case Mode.SELECT: {
        this.slopeContainer.recalculateHoverContext(this.cursor.getPolygon());
        return;
      }
      case Mode.PANNING: {
        if (this.isDragging) {
          if (this.terrainEditor.mode === Mode.PANNING) {
            this.cameraOffset.x = this.getEventLocation(mouseEvent).x / this.cameraZoom - this.dragStart.x;
            this.cameraOffset.y = (this.canvasDiv.offsetHeight - this.getEventLocation(mouseEvent).y) / this.cameraZoom - this.dragStart.y;
          }
        }
        return;
      }
      case Mode.SLOPE_INCREASE:
      case Mode.SLOPE_DECREASE: {
        if (mouseEvent.buttons === 1) {
          this.slopeContainer.manipulate(this.controls, this.terrainEditor.mode === Mode.SLOPE_INCREASE, this.cursor.getPolygon());
        } else {
          this.slopeContainer.recalculateHoverContext(this.cursor.getPolygon());
        }
        return;
      }
      case Mode.DRIVEWAY_INCREASE:
      case Mode.DRIVEWAY_DECREASE:
      default: {

        return;
      }
    }

  }

  private adjustZoom(zoomAmount: number) {
    if (!this.isDragging) {
      this.cameraZoom += zoomAmount;
      this.cameraZoom = Math.max(Math.min(this.cameraZoom, this.MAX_ZOOM), this.MIN_ZOOM);
    }
  }

  private drawPlanetSize() {
    if (this.planetSize) {
      this.ctx.strokeRect(0, 0, this.planetSize.x, this.planetSize.y);
    }
  }
}
