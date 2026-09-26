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

        // Centred on the camera, never stopped at the edge - see AbstractGameCoordinates.setupXShift.
        let xDownerLimit = (width / imageScale / 2.0);
        let xShift = centerOffset.getX();
        let yUpperLimit = RadarComponent.MINI_MAP_IMAGE_HEIGHT - (height / imageScale / 2.0);
        let yShift = centerOffset.getY();

        ctx.translate(xDownerLimit - xShift, yShift - yUpperLimit);
    }

    protected draw(ctx: CanvasRenderingContext2D): void {
        if (this.imageElement == null) {
            return;
        }
        ctx.drawImage(this.imageElement, 0, 0);
    }
}