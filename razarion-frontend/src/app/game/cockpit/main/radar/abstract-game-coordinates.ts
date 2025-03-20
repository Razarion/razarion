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

    protected toCanvasPixel(pixels: number): number {
        return pixels / this.setupGameScale();
    }

    private setupXShift(width: number, planetSizeX: number, scale: number, centerOffset: DecimalPosition): number {
        let xDownerLimit: number = width / scale / 2.0;
        let xUpperLimit: number = planetSizeX - xDownerLimit;
        let xShift: number;
        if (centerOffset.getX() < xDownerLimit) {
            xShift = 0.0;
        } else if (centerOffset.getX() > xUpperLimit) {
            xShift = xUpperLimit - xDownerLimit;
        } else {
            xShift = centerOffset.getX() - xDownerLimit;
        }
        return xShift;
    }

    private setupYShift(height: number, playHeight: number, scale: number, centerOffset: DecimalPosition): number {
        let yDownerLimit: number = height / scale / 2.0;
        let yUpperLimit: number = playHeight - yDownerLimit;
        let yShift: number;
        if (centerOffset.getY() < yDownerLimit) {
            yShift = playHeight - yUpperLimit + yDownerLimit;
        } else if (centerOffset.getY() > yUpperLimit) {
            yShift = playHeight;
        } else {
            yShift = centerOffset.getY() + yDownerLimit;
        }
        return yShift;
    }
}