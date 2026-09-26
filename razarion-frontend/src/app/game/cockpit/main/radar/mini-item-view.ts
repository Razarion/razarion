import { AbstractGameCoordinates } from './abstract-game-coordinates';
import { BaseItemUiService, Diplomacy, GameUiControl } from 'src/app/gwtangular/GwtAngularFacade';
import { BabylonRenderServiceAccessImpl } from 'src/app/game/renderer/babylon-render-service-access-impl.service';
import { GwtHelper } from 'src/app/gwtangular/GwtHelper';

export class MiniItemView extends AbstractGameCoordinates {
    private static readonly REDRAW_TIME = 2000;
    private static readonly ITEM_WIDTH = 0.4;
    private stopping = false;
    /**
     * Whether a working radar shows what is not the player's. Without one the map still shows the
     * ground and the player's own units - the map is for finding your way from the first minute -
     * and the radar adds the others: 'fog, not a lock'. Allies count as own.
     */
    private showForeign = true;

    constructor(gameUiControl: GameUiControl, private baseItemUiService: BaseItemUiService, renderService: BabylonRenderServiceAccessImpl) {
        super(gameUiControl, renderService);
    }

    protected draw(ctx: CanvasRenderingContext2D): void {
        let width = this.toCanvasPixel(MiniItemView.ITEM_WIDTH * this.getZoom());

        let bottomLeft = this.canvasToReal(0, this.getHeight());
        let topRight = this.canvasToReal(this.getWidth(), 0);


        for (let nativeSyncBaseItemTickInfo of this.baseItemUiService.getVisibleNativeSyncBaseItemTickInfos(bottomLeft, topRight)) {
            // Normalised the way color4Diplomacy does it: the value crosses the WASM bridge and is not
            // guaranteed to compare equal to the TypeScript enum as it arrives.
            const diplomacy = GwtHelper.gwtIssueStringEnum(this.baseItemUiService.diplomacy4SyncBaseItem(nativeSyncBaseItemTickInfo), Diplomacy);
            if (!this.showForeign && diplomacy !== Diplomacy.OWN && diplomacy !== Diplomacy.FRIEND) {
                continue;
            }
            ctx.fillStyle = BabylonRenderServiceAccessImpl.color4Diplomacy(diplomacy).toHexString();
            ctx.fillRect(nativeSyncBaseItemTickInfo.x, nativeSyncBaseItemTickInfo.y, width, width);
        }
    }

    public setShowForeign(showForeign: boolean): void {
        this.showForeign = showForeign;
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
            // One failed draw must not end the loop: without it the layer only redraws when the
            // view field changes, and own units stay missing from the map until the player moves it.
            try {
                this.update();
            } catch (e) {
                console.warn('MiniItemView update failed', e);
            }
            setTimeout(() => {
                if (this.stopping) {
                    return;
                }
                this.internalRequestAnimationFrame();
            }, MiniItemView.REDRAW_TIME);
        });

    }
}