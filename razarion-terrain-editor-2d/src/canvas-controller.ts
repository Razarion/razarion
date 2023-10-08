import {SlopeContainer} from "./slope-container";
import {Cursor} from "./cursor";

export class CanvasController {
    private controls: HTMLDivElement;
    private canvas: HTMLCanvasElement;
    private readonly ctx: CanvasRenderingContext2D;
    private cameraOffset = {x: 0, y: 0};
    private cameraZoom = 1;
    private readonly MAX_ZOOM = 5;
    private readonly MIN_ZOOM = 0.1;
    private readonly SCROLL_SENSITIVITY = 0.0005;
    private isDragging = false;
    private dragStart = {x: 0, y: 0};
    private initialPinchDistance: number | null = null;

    constructor(private slopeContainer: SlopeContainer, private cursor: Cursor, private planetSize: {
        x: number,
        y: number
    }) {
        this.controls = document.getElementById("controls") as HTMLDivElement;
        this.canvas = document.getElementById("canvas") as HTMLCanvasElement;
        this.ctx = this.canvas.getContext('2d') as CanvasRenderingContext2D;
        this.setupEventListeners();
        this.draw();
    }

    private setupEventListeners() {
        this.canvas.addEventListener('mousedown', this.onPointerDown.bind(this));
        this.canvas.addEventListener('mouseup', this.onPointerUp.bind(this));
        this.canvas.addEventListener('mousemove', this.onPointerMove.bind(this));
        this.canvas.addEventListener('wheel', (e) => this.adjustZoom(e.deltaY * this.SCROLL_SENSITIVITY));
    }

    private draw() {
        this.canvas.width = window.innerWidth;
        this.canvas.height = window.innerHeight;

        this.ctx.translate(0, window.innerHeight);


        this.ctx.scale(this.cameraZoom, -this.cameraZoom);
        this.ctx.translate(this.cameraOffset.x, this.cameraOffset.y);
        this.ctx.clearRect(0, 0, window.innerWidth, window.innerHeight);

        this.drawPlanetSize(this.ctx);

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
        this.dragStart.y = (window.innerHeight - this.getEventLocation(e).y) / this.cameraZoom - this.cameraOffset.y;
        this.slopeContainer.recalculateSelection(this.cursor.getPolygon());
        if(!e.ctrlKey && this.cursor.getPolygon()) {
            this.slopeContainer.manipulate(this.cursor.getPolygon());
        }
    }

    private onPointerUp(e: MouseEvent | TouchEvent) {
        this.isDragging = false;
    }

    private onPointerMove(e: MouseEvent | TouchEvent) {
        console.log(`onPointerMove ${this.getEventLocation(e).x}:${this.getEventLocation(e).y} zoom:${this.cameraZoom}`)
        console.log(`cameraOffset ${this.cameraOffset.x}:${this.cameraOffset.y}`)
        let x = this.getEventLocation(e).x / this.cameraZoom - this.cameraOffset.x;
        let y = (window.innerHeight - this.getEventLocation(e).y)/ this.cameraZoom - this.cameraOffset.y;
        this.controls.innerText = `${x}:${y}`
        console.log(`game ${x}:${y}`)

        this.cursor.move(x, y);
        this.slopeContainer.recalculateSelection(this.cursor.getPolygon());

        if (this.isDragging) {
            if (e.ctrlKey) {
                this.cameraOffset.x = this.getEventLocation(e).x / this.cameraZoom - this.dragStart.x;
                this.cameraOffset.y = (window.innerHeight - this.getEventLocation(e).y) / this.cameraZoom - this.dragStart.y;
            } else {
                this.slopeContainer.manipulate(this.cursor.getPolygon());
            }
        }
    }

    private adjustZoom(zoomAmount: number) {
        if (!this.isDragging) {
            this.cameraZoom += zoomAmount;
            this.cameraZoom = Math.max(Math.min(this.cameraZoom, this.MAX_ZOOM), this.MIN_ZOOM);
        }
        console.log(`adjustZoom ${this.cameraZoom}`)
    }

    private drawPlanetSize(ctx: CanvasRenderingContext2D) {
        this.ctx.strokeRect(0, 0, this.planetSize.x, this.planetSize.y)
    }
}
