import {SlopeContainer} from "./slope-container";
import {Cursor} from "./cursor";
import {Controls} from "./controls";

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
              private controls: Controls) {
    this.ctx = this.canvas.getContext('2d') as CanvasRenderingContext2D;
    this.setupEventListeners();
    this.draw();
  }

  setPlanetSize(x: number, y: number) {
    this.planetSize = {x: x, y: y};
  }

  private setupEventListeners() {
    this.canvas.addEventListener('mousedown', this.onPointerDown.bind(this));
    this.canvas.addEventListener('mouseup', this.onPointerUp.bind(this));
    this.canvas.addEventListener('mousemove', this.onPointerMove.bind(this));
    window.addEventListener('keydown', this.onKeydown.bind(this));
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
    this.cursor.draw(this.ctx);

    requestAnimationFrame(this.draw.bind(this));
  }

    private getEventLocation(e: MouseEvent | TouchEvent): { x: number, y: number } {
        if (e instanceof MouseEvent && e.clientX && e.clientY) {
            return {x: e.clientX, y: e.clientY};
        }
        return {x: 0, y: 0};
    }

    private onPointerDown(e: MouseEvent | TouchEvent) {
      this.isDragging = true;
      this.dragStart.x = this.getEventLocation(e).x / this.cameraZoom - this.cameraOffset.x;
      this.dragStart.y = (this.canvasDiv.offsetHeight - this.getEventLocation(e).y) / this.cameraZoom - this.cameraOffset.y;
      this.slopeContainer.recalculateSelection(this.cursor.getPolygon());
      if (!e.ctrlKey && this.cursor.getPolygon()) {
        this.slopeContainer.manipulate(this.controls, this.cursor.getPolygon());
      }
    }

    private onPointerUp(e: MouseEvent | TouchEvent) {
        this.isDragging = false;
    }

    private onPointerMove(e: MouseEvent | TouchEvent) {
      let x = this.getEventLocation(e).x / this.cameraZoom - this.cameraOffset.x;
      let y = (this.canvasDiv.offsetHeight - this.getEventLocation(e).y) / this.cameraZoom - this.cameraOffset.y;
      this.controls.xPos = x;
      this.controls.yPos = y;

      this.cursor.move(x, y);

      if (this.isDragging) {
        if (e.ctrlKey) {
          this.cameraOffset.x = this.getEventLocation(e).x / this.cameraZoom - this.dragStart.x;
          this.cameraOffset.y = (this.canvasDiv.offsetHeight - this.getEventLocation(e).y) / this.cameraZoom - this.dragStart.y;
        } else {
          this.slopeContainer.manipulate(this.controls, this.cursor.getPolygon());
        }
      } else {
        this.slopeContainer.recalculateSelection(this.cursor.getPolygon());
      }
    }

  private onKeydown(e: KeyboardEvent) {
    if (e.key == ' ') {
      if (this.slopeContainer.getSelectionContext()) {
        this.controls.selectedSLope = this.slopeContainer.getSelectionContext()?.getInsideOf() || this.slopeContainer.getSelectionContext()?.getIntersect();
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
