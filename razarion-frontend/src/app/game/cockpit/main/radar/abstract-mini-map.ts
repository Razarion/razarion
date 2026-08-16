import { BabylonRenderServiceAccessImpl } from 'src/app/game/renderer/babylon-render-service-access-impl.service';
import { DecimalPosition, GameUiControl } from 'src/app/gwtangular/GwtAngularFacade';
import {ViewField} from "../../../renderer/view-field";

export abstract class AbstractMiniMap {
    private canvasElement!: HTMLCanvasElement;
    private width!: number;
    private height!: number;
    private ctx!: CanvasRenderingContext2D;
    private viewField?: ViewField;
    private zoom!: number;

    constructor(
        protected readonly gameUiControl: GameUiControl,
        protected readonly renderService: BabylonRenderServiceAccessImpl) {
        this.gameUiControl = gameUiControl;
    }

    protected abstract setupTransformation(zoom: number, ctx: CanvasRenderingContext2D, width: number, height: number): void;

    protected abstract draw(ctx: CanvasRenderingContext2D): void;

    public init(canvasElement: Element, width: number, height: number, zoom: number): void {
        this.canvasElement = canvasElement as HTMLCanvasElement;
        this.canvasElement.width = width;
        this.canvasElement.height = height;
        this.width = width;
        this.height = height;
        this.ctx = this.canvasElement.getContext("2d") as CanvasRenderingContext2D;
        this.zoom = zoom;
    }

    /**
     * Changes the drawing surface without touching what is drawn on it. The phone radar sits in the
     * corner at about a hundred pixels and grows to most of the screen when the player expands it -
     * as one component, not two. Two instances would each hold their own view field listener and
     * item updater, and MiniTerrain would fetch the planet image a second time (its URL carries a
     * cache-busting timestamp, so the browser cache does not spare that download).
     * <p>
     * The canvas is cleared by the assignment; the caller redraws.
     */
    public resize(width: number, height: number): void {
        this.canvasElement.width = width;
        this.canvasElement.height = height;
        this.width = width;
        this.height = height;
    }

    public update(): void {
        this.clearCanvas();

        this.ctx.save();
        this.setupTransformation(this.zoom, this.ctx, this.width, this.height);
        this.draw(this.ctx);
        this.ctx.restore();
    }

    protected clearCanvas(): void {
        this.ctx.save();
        this.ctx.setTransform(1, 0, 0, 1, 0, 0);
        this.ctx.clearRect(0, 0, this.canvasElement.width, this.canvasElement.height);
        this.ctx.restore();
    }

    protected setupGameScale(): number {
        let planetSize: DecimalPosition = this.gameUiControl.getPlanetConfig().getSize();
        let scale: number = Math.min(this.width / planetSize.getX(), this.height / planetSize.getY());
        scale *= this.zoom;
        return scale;
    }

    public setViewField(viewField: ViewField): void {
        this.viewField = viewField;
    }

    protected getViewField(): ViewField {
        if (!this.viewField) {
            this.viewField = this.renderService.getCurrentViewField();
        }
        return this.viewField;
    }

    protected getWidth(): number {
        return this.width;
    }

    protected getHeight(): number {
        return this.height;
    }

    public setZoom(zoom: number): void {
        this.zoom = zoom;
    }

    protected getZoom(): number {
        return this.zoom;
    }
}
