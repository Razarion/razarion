import { GameUiControl } from 'src/app/gwtangular/GwtAngularFacade';
import { AbstractGameCoordinates } from './abstract-game-coordinates';
import { BabylonRenderServiceAccessImpl } from 'src/app/game/renderer/babylon-render-service-access-impl.service';
import {ViewField} from "../../../renderer/view-field";

export class MiniViewField extends AbstractGameCoordinates {
    private static readonly LINE_WIDTH = 1;

    constructor(gameUiControl: GameUiControl, renderService: BabylonRenderServiceAccessImpl) {
        super(gameUiControl, renderService);
    }

    protected draw(ctx: CanvasRenderingContext2D): void {
        let viewField: ViewField = this.getViewField();

        ctx.lineWidth = this.toCanvasPixel(MiniViewField.LINE_WIDTH);

        ctx.strokeStyle = "#fff";
        ctx.beginPath();
        ctx.moveTo(viewField.getBottomLeft().getX(), viewField.getBottomLeft().getY());
        ctx.lineTo(viewField.getBottomRight().getX(), viewField.getBottomRight().getY());
        ctx.lineTo(viewField.getTopRight().getX(), viewField.getTopRight().getY());
        ctx.lineTo(viewField.getTopLeft().getX(), viewField.getTopLeft().getY());
        ctx.closePath();
        ctx.stroke();
    }
}
