import {SlopeContainer} from "./slope-container";
import {Cursor} from "./cursor";
import {Controls} from "./controls";
import {Mode, TerrainEditor} from "./terrain-editor";
import {DecimalPosition} from "../generated/razarion-share";

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

    this.slopeContainer.draw(this.ctx, this.controls, this.terrainEditor.mode);
    if (this.terrainEditor.mode == Mode.SLOPE_INCREASE
      || this.terrainEditor.mode == Mode.SLOPE_DECREASE
      || this.terrainEditor.mode == Mode.DRIVEWAY_INCREASE
      || this.terrainEditor.mode == Mode.DRIVEWAY_DECREASE) {
      this.cursor.draw(this.ctx);
    }

    requestAnimationFrame(this.draw.bind(this));
  }

  private getEventLocation(e: MouseEvent): { x: number, y: number } {
    if (e instanceof MouseEvent && e.offsetX && e.offsetY) {
      return {x: e.offsetX, y: e.offsetY};
    }
    return {x: 0, y: 0};
  }

  private onMouseDown(mouseEvent: MouseEvent) {
    const cursorPosition = this.setupPosition(mouseEvent);

    switch (this.terrainEditor?.mode) {
      case Mode.SELECT: {
        if (this.slopeContainer.getHoverContext()) {
          this.controls.selectedSlope = this.slopeContainer.getHoverContext()?.getIntersectSlope();
          this.controls.selectedDriveway = this.slopeContainer.getHoverContext()?.getIntersectDriveway();
          this.controls.selectedCorner = this.slopeContainer.getHoverContext()?.getIntersectSlope()?.createIntersectCorner(this.slopeContainer.getHoverContext()?.getIntersectCornerIndex(),
            (slope)=>{
            this.slopeContainer.getSaveContext().onManipulated(slope);
            });
        }
        return;
      }
      case Mode.PANNING: {
        this.isDragging = true;
        this.dragStart.x = this.getEventLocation(mouseEvent).x / this.cameraZoom - this.cameraOffset.x;
        this.dragStart.y = (this.canvasDiv.offsetHeight - this.getEventLocation(mouseEvent).y) / this.cameraZoom - this.cameraOffset.y;
        return;
      }
      case Mode.SLOPE_INCREASE:
      case Mode.SLOPE_DECREASE: {
        this.slopeContainer.recalculateHoverContext(this.cursor.getPolygon(), cursorPosition);
        this.slopeContainer.manipulateSlope(this.controls, this.terrainEditor.mode === Mode.SLOPE_INCREASE, this.cursor.getPolygon());
        return;
      }
      case Mode.DRIVEWAY_INCREASE:
      case Mode.DRIVEWAY_DECREASE: {
        this.slopeContainer.recalculateHoverContext(this.cursor.getPolygon(), cursorPosition);
        this.slopeContainer.manipulateDriveway(this.controls, this.terrainEditor.mode === Mode.DRIVEWAY_INCREASE, this.cursor.getPolygon());
        return;
      }
      case Mode.CORNER_ADD: {
        this.slopeContainer.recalculateHoverContext(this.cursor.getPolygon(), cursorPosition);
        this.slopeContainer.addCorner(cursorPosition);
        return;
      }
      case Mode.CORNER_MOVE: {
        this.slopeContainer.moveCorner(cursorPosition);
        return;
      }
      case Mode.CORNER_DELETE: {
        this.slopeContainer.recalculateHoverContext(this.cursor.getPolygon(), cursorPosition);
        this.slopeContainer.removeCorner();
        this.slopeContainer.recalculateHoverContext(this.cursor.getPolygon(), cursorPosition);
        return;
      }
      default: {
        return;
      }
    }
  }

  private onMouseUp(e: MouseEvent) {
    this.isDragging = false;
  }

  private onMouseMove(mouseEvent: MouseEvent) {
    let mousePosition = this.setupPosition(mouseEvent);
    this.controls.xPos = mousePosition.x;
    this.controls.yPos = mousePosition.y;
    this.cursor.move(mousePosition.x, mousePosition.y);


    switch (this.terrainEditor?.mode) {
      case Mode.SELECT: {
        this.slopeContainer.recalculateHoverContext(this.cursor.getPolygon(), mousePosition);
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
          this.slopeContainer.manipulateSlope(this.controls, this.terrainEditor.mode === Mode.SLOPE_INCREASE, this.cursor.getPolygon());
        } else {
          this.slopeContainer.recalculateHoverContext(this.cursor.getPolygon(), mousePosition);
        }
        return;
      }
      case Mode.DRIVEWAY_INCREASE:
      case Mode.DRIVEWAY_DECREASE: {
        if (mouseEvent.buttons === 1) {
          this.slopeContainer.manipulateDriveway(this.controls, this.terrainEditor.mode === Mode.DRIVEWAY_INCREASE, this.cursor.getPolygon());
        } else {
          this.slopeContainer.recalculateHoverContext(this.cursor.getPolygon(), mousePosition);
        }
        return;
      }
      case Mode.CORNER_ADD:
      case Mode.CORNER_DELETE: {
        this.slopeContainer.recalculateHoverContext(this.cursor.getPolygon(), mousePosition);
        return;
      }
      case Mode.CORNER_MOVE: {
        if (mouseEvent.buttons === 1) {
          this.slopeContainer.manipulateCorner(mousePosition);
        } else {
          this.slopeContainer.recalculateHoverContext(this.cursor.getPolygon(), mousePosition);
        }
        return;
      }
      default: {
        return;
      }
    }

  }

  private setupPosition(mouseEvent: MouseEvent): DecimalPosition {
    let x = this.getEventLocation(mouseEvent).x / this.cameraZoom - this.cameraOffset.x;
    let y = (this.canvasDiv.offsetHeight - this.getEventLocation(mouseEvent).y) / this.cameraZoom - this.cameraOffset.y;
    return {x, y};
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
