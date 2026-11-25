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
    /** Names only on a map that can carry them: the phone's corner radar is about 100 pixels. */
    private static readonly NAMES_FROM_WIDTH = 150;
    private static readonly ONLINE_COLOR = '#ffe066';
    private static readonly NEW_COLOR = '#7cffb2';
    private static readonly NEW_MILLIS = 8 * 60_000;
    private onlineName: (baseId: number) => string | undefined = () => undefined;

    constructor(gameUiControl: GameUiControl, private baseItemUiService: BaseItemUiService, renderService: BabylonRenderServiceAccessImpl) {
        super(gameUiControl, renderService);
    }

    protected draw(ctx: CanvasRenderingContext2D): void {
        let width = this.toCanvasPixel(MiniItemView.ITEM_WIDTH * this.getZoom());

        let bottomLeft = this.canvasToReal(0, this.getHeight());
        let topRight = this.canvasToReal(this.getWidth(), 0);

        const online = new Map<number, { baseId: number, name: string, x: number, y: number, n: number }>();
        for (let nativeSyncBaseItemTickInfo of this.baseItemUiService.getVisibleNativeSyncBaseItemTickInfos(bottomLeft, topRight)) {
            // Normalised the way color4Diplomacy does it: the value crosses the WASM bridge and is not
            // guaranteed to compare equal to the TypeScript enum as it arrives.
            const diplomacy = GwtHelper.gwtIssueStringEnum(this.baseItemUiService.diplomacy4SyncBaseItem(nativeSyncBaseItemTickInfo), Diplomacy);
            const onlineName = diplomacy === Diplomacy.FRIEND ? this.onlineName(nativeSyncBaseItemTickInfo.baseId) : undefined;
            if (onlineName) {
                const at = online.get(nativeSyncBaseItemTickInfo.baseId) ?? {baseId: nativeSyncBaseItemTickInfo.baseId, name: onlineName, x: 0, y: 0, n: 0};
                at.x += nativeSyncBaseItemTickInfo.x;
                at.y += nativeSyncBaseItemTickInfo.y;
                at.n++;
                online.set(nativeSyncBaseItemTickInfo.baseId, at);
            }
            if (!this.showForeign && diplomacy !== Diplomacy.OWN && diplomacy !== Diplomacy.FRIEND) {
                continue;
            }
            ctx.fillStyle = BabylonRenderServiceAccessImpl.color4Diplomacy(diplomacy).toHexString();
            ctx.fillRect(nativeSyncBaseItemTickInfo.x, nativeSyncBaseItemTickInfo.y, width, width);
        }
        this.drawOnlinePlayers(ctx, [...online.values()]);
    }

    /**
     * The other people playing right now, each as a ring where their units are and - once the map
     * is big enough to read it - their name. Yellow dots alone said nothing: an offline player's
     * base looks the same, and nobody learnt that yellow means a person.
     */
    private drawOnlinePlayers(ctx: CanvasRenderingContext2D, players: { baseId: number, name: string, x: number, y: number, n: number }[]): void {
        if (players.length === 0) {
            return;
        }
        const withNames = this.getWidth() >= MiniItemView.NAMES_FROM_WIDTH;
        const createdById = this.baseCreatedMillis();
        const now = Date.now();
        ctx.save();
        // In canvas pixels: the game transformation flips y, and text drawn under it stands on its head.
        ctx.setTransform(1, 0, 0, 1, 0, 0);
        ctx.font = 'bold 11px sans-serif';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'bottom';
        for (const player of players) {
            const at = this.realToCanvas(player.x / player.n, player.y / player.n);
            const created = createdById.get(player.baseId);
            // Just landed: the few minutes in which a visit counts - the noob island takes a new
            // player about six (2026-11-18..24) - drawn apart so a passer-by can find them.
            const fresh = created !== undefined && created > 0 && now - created < MiniItemView.NEW_MILLIS;
            const color = fresh ? MiniItemView.NEW_COLOR : MiniItemView.ONLINE_COLOR;
            ctx.strokeStyle = color;
            ctx.lineWidth = fresh ? 3 : 2;
            ctx.beginPath();
            ctx.arc(at.x, at.y, fresh ? 8 : 6, 0, 2 * Math.PI);
            ctx.stroke();
            if (withNames) {
                const label = fresh ? player.name + ' · new' : player.name;
                ctx.lineWidth = 3;
                ctx.strokeStyle = 'rgba(0, 0, 0, 0.8)';
                // Kept inside the map: a player at the edge had the start of his name cut off.
                const half = ctx.measureText(label).width / 2 + 2;
                const x = Math.min(Math.max(at.x, half), this.getWidth() - half);
                const y = Math.max(at.y - 9, 13);
                ctx.strokeText(label, x, y);
                ctx.fillStyle = color;
                ctx.fillText(label, x, y);
            }
        }
        ctx.restore();
    }

    /** When each base was built, by base id. */
    private baseCreatedMillis(): Map<number, number> {
        const created = new Map<number, number>();
        try {
            for (const base of this.baseItemUiService.getBases() ?? []) {
                created.set(base.getBaseId(), base.getCreatedMillis());
            }
        } catch (e) {
            // A base list that cannot be read leaves everybody drawn as plain online.
        }
        return created;
    }

    /** Where the online players come from - see PresenceService. Unset, nobody is shown as online. */
    public setOnlineName(onlineName: (baseId: number) => string | undefined): void {
        this.onlineName = onlineName;
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