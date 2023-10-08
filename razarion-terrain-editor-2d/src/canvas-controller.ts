import {SlopeContainer} from "./slope-container";
import {Cursor} from "./cursor";

export class CanvasController {
    private controls: HTMLDivElement;
    private canvas: HTMLCanvasElement;
    private ctx: CanvasRenderingContext2D;
    private cameraOffset = {x: window.innerWidth / 2, y: window.innerHeight / 2};
    private cameraZoom = 1;
    private readonly MAX_ZOOM = 5;
    private readonly MIN_ZOOM = 0.1;
    private readonly SCROLL_SENSITIVITY = 0.0005;
    private isDragging = false;
    private dragStart = {x: 0, y: 0};
    private initialPinchDistance: number | null = null;
    private lastZoom = this.cameraZoom;

    constructor(private slopeContainer: SlopeContainer, private cursor: Cursor) {
        this.controls = document.getElementById("controls") as HTMLDivElement;
        this.canvas = document.getElementById("canvas") as HTMLCanvasElement;
        this.ctx = this.canvas.getContext('2d') as CanvasRenderingContext2D;
        this.setupEventListeners();
        this.draw();
    }

    private setupEventListeners() {
        this.canvas.addEventListener('mousedown', this.onPointerDown.bind(this));
        this.canvas.addEventListener('touchstart', (e) => this.handleTouch(e, this.onPointerDown.bind(this)));
        this.canvas.addEventListener('mouseup', this.onPointerUp.bind(this));
        this.canvas.addEventListener('touchend', (e) => this.handleTouch(e, this.onPointerUp.bind(this)));
        this.canvas.addEventListener('mousemove', this.onPointerMove.bind(this));
        this.canvas.addEventListener('touchmove', (e) => this.handleTouch(e, this.onPointerMove.bind(this)));
        this.canvas.addEventListener('wheel', (e) => this.adjustZoom(e.deltaY * this.SCROLL_SENSITIVITY, null));
    }

    private draw() {
        this.canvas.width = window.innerWidth;
        this.canvas.height = window.innerHeight;

        this.ctx.translate(window.innerWidth / 2, window.innerHeight / 2);
        this.ctx.scale(this.cameraZoom, this.cameraZoom);
        this.ctx.translate(-window.innerWidth / 2 + this.cameraOffset.x, -window.innerHeight / 2 + this.cameraOffset.y);
        this.ctx.clearRect(0, 0, window.innerWidth, window.innerHeight);

        this.slopeContainer.draw(this.ctx);
        this.cursor.draw(this.ctx);

        requestAnimationFrame(this.draw.bind(this));
    }

    private getEventLocation(e: MouseEvent | TouchEvent): { x: number, y: number } {
        if (e instanceof TouchEvent && e.touches.length === 1) {
            return { x: e.touches[0].clientX, y: e.touches[0].clientY };
        } else if (e instanceof MouseEvent && e.clientX && e.clientY) {
            return { x: e.clientX, y: e.clientY };
        }
        return { x: 0, y: 0 };
    }

    private onPointerDown(e: MouseEvent | TouchEvent) {
        this.isDragging = true;
        this.dragStart.x = this.getEventLocation(e).x / this.cameraZoom - this.cameraOffset.x;
        this.dragStart.y = this.getEventLocation(e).y / this.cameraZoom - this.cameraOffset.y;
        this.slopeContainer.recalculateSelection(this.cursor.getPolygon());
        if(!e.ctrlKey && this.cursor.getPolygon()) {
            this.slopeContainer.manipulate(this.cursor.getPolygon());
        }
    }

    private onPointerUp(e: MouseEvent | TouchEvent) {
        this.isDragging = false;
    }

    private onPointerMove(e: MouseEvent | TouchEvent) {
        let x = this.getEventLocation(e).x / this.cameraZoom - this.cameraOffset.x;
        let y = -(this.getEventLocation(e).y / this.cameraZoom - this.cameraOffset.y);
        this.controls.innerText = `${x}:${y}`

        this.cursor.move(x, y);
        this.slopeContainer.recalculateSelection(this.cursor.getPolygon());

        if (this.isDragging) {
            if(e.ctrlKey) {
                this.cameraOffset.x = this.getEventLocation(e).x / this.cameraZoom - this.dragStart.x;
                this.cameraOffset.y = this.getEventLocation(e).y / this.cameraZoom - this.dragStart.y;
            } else {
                this.slopeContainer.manipulate(this.cursor.getPolygon());
            }
        }
    }

    private handleTouch(e: TouchEvent, singleTouchHandler: (e: MouseEvent | TouchEvent) => void) {
        if (e.touches.length === 1) {
            singleTouchHandler(e);
        } else if (e.type === "touchmove" && e.touches.length === 2) {
            this.isDragging = false;
            this.handlePinch(e);
        }
    }

    private handlePinch(e: TouchEvent) {
        e.preventDefault();

        const touch1 = { x: e.touches[0].clientX, y: e.touches[0].clientY };
        const touch2 = { x: e.touches[1].clientX, y: e.touches[1].clientY };
        const currentDistance = (touch1.x - touch2.x) ** 2 + (touch1.y - touch2.y) ** 2;

        if (this.initialPinchDistance === null) {
            this.initialPinchDistance = currentDistance;
        } else {
            this.adjustZoom(null, currentDistance / this.initialPinchDistance);
        }
    }

    private adjustZoom(zoomAmount: number | null, zoomFactor: number | null) {
        if (!this.isDragging) {
            if (zoomAmount) {
                this.cameraZoom += zoomAmount;
            } else if (zoomFactor) {
                this.cameraZoom = zoomFactor * this.lastZoom;
            }

            this.cameraZoom = Math.min(this.cameraZoom, this.MAX_ZOOM);
            this.cameraZoom = Math.max(this.cameraZoom, this.MIN_ZOOM);
        }
    }
}
