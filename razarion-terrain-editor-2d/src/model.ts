import {TerrainSlopePosition} from "./generated/razarion-share";
import * as turf from "@turf/turf";
import {Feature, Polygon} from "@turf/turf";

export class Slope {
    private terrainSlopePosition: TerrainSlopePosition;
    private children: Slope[] = [];
    private polygon: Feature<Polygon, any>;
    private selected = false;

    constructor(terrainSlopePosition: TerrainSlopePosition) {
        this.terrainSlopePosition = terrainSlopePosition;

        this.polygon = this.createPolygon(terrainSlopePosition);

        if (terrainSlopePosition.children) {
            terrainSlopePosition.children.forEach(child => {
                this.children.push(new Slope(child))
            })
        }
    }

    private createPolygon(terrainSlopePosition: TerrainSlopePosition): Feature<Polygon, any> {
        let points: any = [];
        terrainSlopePosition.polygon.forEach(terrainSlopeCorner => {
            points.push([terrainSlopeCorner.position.x, terrainSlopeCorner.position.y])
        })
        points.push([points[0][0], points[0][1]])

        return turf.polygon([
            points
        ]);
    }

    draw(ctx: CanvasRenderingContext2D) {
        this.children.forEach(child => child.draw(ctx))

        if (this.selected) {
            ctx.fillStyle = "blue";
        } else {
            ctx.fillStyle = "green";
        }

        ctx.beginPath();
        ctx.moveTo(this.polygon.geometry.coordinates[0][0][0], this.polygon.geometry.coordinates[0][0][1]);
        for (let i = 1; i < this.polygon.geometry.coordinates[0].length - 1; i++) {
            ctx.lineTo(this.polygon.geometry.coordinates[0][i][0], this.polygon.geometry.coordinates[0][i][1])
        }
        ctx.closePath();
        ctx.fill();
    }

    recalculateSelection(cursorPolygon: Feature<Polygon, any>, selectionContext: SelectionContext) {
        if (turf.intersect(this.polygon, cursorPolygon)) {
            selectionContext.add(this);
            this.selected = true;
        } else {
            this.selected = false;
        }
    }

    adjoin(polygon: Feature<Polygon, any>) {
        this.polygon = <any>turf.union(polygon, this.polygon);
    }
}

export class SelectionContext {
    private slopes: Slope[] = [];

    add(slope: Slope) {
        this.slopes.push(slope);
    }

    valid(): boolean {
        return this.slopes.length > 0
    }

    getSelectedSlope(): Slope {
        return this.slopes[0];
    }
}