import { AbstractGameCoordinates } from './abstract-game-coordinates';
import { BaseItemUiService, GameUiControl } from 'src/app/gwtangular/GwtAngularFacade';
import { BabylonRenderServiceAccessImpl } from 'src/app/game/renderer/babylon-render-service-access-impl.service';

export class MiniItemView extends AbstractGameCoordinates {
    private static readonly REDRAW_TIME = 2000;
    private static readonly ITEM_WIDTH = 0.4;
    private stopping = false;

    constructor(gameUiControl: GameUiControl, private baseItemUiService: BaseItemUiService, renderService: BabylonRenderServiceAccessImpl) {
        super(gameUiControl, renderService);
    }

    protected draw(ctx: CanvasRenderingContext2D): void {
        let width = this.toCanvasPixel(MiniItemView.ITEM_WIDTH * this.getZoom());

        let bottomLeft = this.canvasToReal(0, this.getHeight());
        let topRight = this.canvasToReal(this.getWidth(), 0);


        for (let nativeSyncBaseItemTickInfo of this.baseItemUiService.getVisibleNativeSyncBaseItemTickInfos(bottomLeft, topRight)) {
            ctx.fillStyle = BabylonRenderServiceAccessImpl.color4Diplomacy(this.baseItemUiService.diplomacy4SyncBaseItem(nativeSyncBaseItemTickInfo)).toHexString();
            ctx.fillRect(nativeSyncBaseItemTickInfo.x, nativeSyncBaseItemTickInfo.y, width, width);
        }
    }

    public startUpdater(): void {
        this.stopping = false;
        this.internalRequestAnimationFrame();
    }

    public stopUpdater(): void {
        this.stopping = true;
    }

    private internalRequestAnimationFrame() {
        requestAnimationFrame(() => {
            if (this.stopping) {
                return;
            }
            this.update();
            setTimeout(() => {
                if (this.stopping) {
                    return;
                }
                this.internalRequestAnimationFrame();
            }, MiniItemView.REDRAW_TIME);
        });

    }
}