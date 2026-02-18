import { AbstractMiniMap } from './abstract-mini-map';
import { getMiniMapPlanetUrl } from 'src/app/common';
import { BabylonRenderServiceAccessImpl } from 'src/app/game/renderer/babylon-render-service-access-impl.service';
import { GameUiControl } from 'src/app/gwtangular/GwtAngularFacade';
import { RadarComponent } from './radar.component';

export class MiniTerrain extends AbstractMiniMap {
    private imageElement?: HTMLImageElement;

    constructor(gameUiControl: GameUiControl, renderService: BabylonRenderServiceAccessImpl) {
        super(gameUiControl, renderService);
    }

    public show(imageLoaderCallback: () => void): void {
        let imageUrl: string = getMiniMapPlanetUrl(this.gameUiControl.getPlanetConfig().getId());
        let image = new Image();
        image.onload = () => {
            this.imageElement = image;
            imageLoaderCallback && imageLoaderCallback();
        };
        image.onerror = () => {
            console.warn("MiniTerrain loading image failed: " + imageUrl);
        };
        image.src = imageUrl + '?t=' + Date.now();
    }

    protected setupTransformation(zoom: number, ctx: CanvasRenderingContext2D, width: number, height: number): void {
        let imageScale = Math.min( width / RadarComponent.MINI_MAP_IMAGE_WIDTH, height / RadarComponent.MINI_MAP_IMAGE_HEIGHT);
        imageScale *= zoom;
        ctx.scale(imageScale, imageScale);
        let gameScale = this.setupGameScale();
        let divider = imageScale / gameScale;
        let centerOffset = this.getViewField().getCenter().divide(divider, divider);

        let xDownerLimit = (width / imageScale / 2.0);
        let xUpperLimit = RadarComponent.MINI_MAP_IMAGE_WIDTH - xDownerLimit;
        let  xShift;
        if (centerOffset.getX() < xDownerLimit) {
            xShift = xDownerLimit;
        } else if (centerOffset.getX() > xUpperLimit) {
            xShift = xUpperLimit;
        } else {
            xShift = centerOffset.getX();
        }

        let yDownerLimit =  (height / imageScale / 2.0);
        let yUpperLimit = RadarComponent.MINI_MAP_IMAGE_HEIGHT - yDownerLimit;
        let yShift;
        if (centerOffset.getY() < yDownerLimit) {
            yShift = yDownerLimit;
        } else if (centerOffset.getY() > yUpperLimit) {
            yShift = yUpperLimit;
        } else {
            yShift = centerOffset.getY();
        }

        ctx.translate(xDownerLimit - xShift, yShift - yUpperLimit);
    }

    protected draw(ctx: CanvasRenderingContext2D): void {
        if (this.imageElement == null) {
            return;
        }
        ctx.drawImage(this.imageElement, 0, 0);
    }
}