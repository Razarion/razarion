import { DecimalPosition, GameUiControl } from 'src/app/gwtangular/GwtAngularFacade';
import { AbstractMiniMap } from './abstract-mini-map';
import { BabylonRenderServiceAccessImpl } from 'src/app/game/renderer/babylon-render-service-access-impl.service';
import { GwtInstance } from 'src/app/gwtangular/GwtInstance';

export abstract class AbstractGameCoordinates extends AbstractMiniMap {
    constructor(gameUiControl: GameUiControl, renderService: BabylonRenderServiceAccessImpl) {
        super(gameUiControl, renderService);
    }

    protected setupTransformation(zoom: number, ctx: CanvasRenderingContext2D, width: number, height: number): void {
        let planetSize: DecimalPosition = this.gameUiControl.getPlanetConfig().getSize();

        let scale: number = this.setupGameScale();

        let xShift: number = this.setupXShift(width, planetSize.getX(), scale, this.getViewField().getCenter());
        let yShift: number = this.setupYShift(height, planetSize.getY(), scale, this.getViewField().getCenter());


        ctx.scale(scale, -scale);
        ctx.translate(-xShift, -yShift);
    }

    public canvasToReal(canvasPositionX: number, canvasPositionY: number): DecimalPosition {
        let planetSize: DecimalPosition = this.gameUiControl.getPlanetConfig().getSize();

        let scale: number = this.setupGameScale();
        let real = GwtInstance.newDecimalPosition(canvasPositionX / scale, canvasPositionY / -scale);
        real = real.add(this.setupXShift(this.getWidth(), planetSize.getX(), scale, this.getViewField().getCenter()), this.setupYShift(this.getHeight(), planetSize.getY(), scale, this.getViewField().getCenter()));
        return real;
    }

    /** The inverse of {@link canvasToReal}: where a ground point lands on the canvas, in pixels. */
    public realToCanvas(realX: number, realY: number): { x: number, y: number } {
        let planetSize: DecimalPosition = this.gameUiControl.getPlanetConfig().getSize();
        let scale: number = this.setupGameScale();
        let xShift = this.setupXShift(this.getWidth(), planetSize.getX(), scale, this.getViewField().getCenter());
        let yShift = this.setupYShift(this.getHeight(), planetSize.getY(), scale, this.getViewField().getCenter());
        return {x: (realX - xShift) * scale, y: (realY - yShift) * -scale};
    }

    protected toCanvasPixel(pixels: number): number {
        return pixels / this.setupGameScale();
    }

    /**
     * The map follows the camera and stops at the planet's edge, so it never shows what lies
     * beyond. Between 23.09. and 28.09.2026 it stayed centred instead: the desktop dock was round,
     * and a map stopped at the edge pushed the noob island - in the planet's lower left corner,
     * where every new player starts - into a corner the circle cut off. The dock is square now.
     * MiniTerrain has to follow the same rule, or the ground and the units drift apart.
     */
    private setupXShift(width: number, planetSizeX: number, scale: number, centerOffset: DecimalPosition): number {
        const half = width / scale / 2.0;
        return AbstractGameCoordinates.clampCenter(centerOffset.getX(), half, planetSizeX) - half;
    }

    private setupYShift(height: number, planetSizeY: number, scale: number, centerOffset: DecimalPosition): number {
        const half = height / scale / 2.0;
        return AbstractGameCoordinates.clampCenter(centerOffset.getY(), half, planetSizeY) + half;
    }

    /**
     * The centre the map shows: the camera's, moved in just far enough that the window of
     * half-width half stays on [0, size]. A window wider than the planet is centred on it.
     */
    static clampCenter(center: number, half: number, size: number): number {
        if (2 * half >= size) {
            return size / 2;
        }
        return Math.min(Math.max(center, half), size - half);
    }
}