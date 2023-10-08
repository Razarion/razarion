import {TerrainSlopePosition} from "./generated/razarion-share";
import {SelectionContext, Slope} from "./model";
import {Feature, Polygon} from "@turf/turf";
import * as turf from '@turf/turf';

export class SlopeContainer {
    private slopes: Slope[] = [];
    private selectionContext?: SelectionContext;

    setTerrainSlopePositions(terrainSlopePositions: TerrainSlopePosition[]) {
        terrainSlopePositions.forEach(terrainSlopePosition => {
            this.slopes.push(new Slope(terrainSlopePosition));
        });
    }

    draw(ctx: CanvasRenderingContext2D) {
        ctx.save();
        ctx.scale(1, -1);
        this.slopes.forEach(slopes => {
            slopes.draw(ctx);
        });
        ctx.restore();
    }

    recalculateSelection(cursorPolygon: Feature<Polygon, any> | undefined) {
        this.selectionContext = new SelectionContext();
        if (!cursorPolygon) {
            return;
        }
        this.slopes.forEach(slope => {
            slope.recalculateSelection(cursorPolygon, this.selectionContext!);
        });

    }

    manipulate(cursorPolygon?: Feature<Polygon, any>) {
        if(!cursorPolygon) {
            return;
        }

        if (!this.selectionContext?.valid()) {
            return;
        }
        this.selectionContext.getSelectedSlope().adjoin(cursorPolygon);
    }
}