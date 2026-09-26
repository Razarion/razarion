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
     * The map is always centred on the camera. It used to stop at the planet's edge instead, which
     * pushed the camera - and the player's base - into a corner of the square: the noob island lies
     * in the planet's lower left corner, and the desktop dock clips the map round, so the base was
     * cut off exactly where every new player starts (23.09.2026). Beyond the planet the map is empty.
     * MiniTerrain has to follow the same rule, or the ground and the units drift apart.
     */
    private setupXShift(width: number, _planetSizeX: number, scale: number, centerOffset: DecimalPosition): number {
        return centerOffset.getX() - width / scale / 2.0;
    }

    private setupYShift(height: number, _playHeight: number, scale: number, centerOffset: DecimalPosition): number {
        return centerOffset.getY() + height / scale / 2.0;
    }
}