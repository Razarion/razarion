import {TerrainSlopeCorner, TerrainSlopePosition} from "./generated/razarion-share";

export class SlopeContainer {
    private terrainSlopePositions?: TerrainSlopePosition[];

    setTerrainSlopePositions(terrainSlopePositions: TerrainSlopePosition[]) {
        this.terrainSlopePositions = terrainSlopePositions;
    }

    draw(ctx: CanvasRenderingContext2D) {
        if (!this.terrainSlopePositions) {
            return;
        }
        ctx.save();
        ctx.scale(1, -1);
        this.terrainSlopePositions.forEach(terrainSlopePosition => {
            this.drawPolygon(ctx, terrainSlopePosition);
        });
        ctx.restore();
    }

    private drawPolygon(ctx: CanvasRenderingContext2D, terrainSlopePosition: TerrainSlopePosition) {
        if (terrainSlopePosition.polygon.length < 3) {
            return;
        }

        ctx.beginPath();
        ctx.moveTo(terrainSlopePosition.polygon[0].position.x, terrainSlopePosition.polygon[0].position.y);
        for (let i = 1; i < terrainSlopePosition.polygon.length; i++) {
            ctx.lineTo(terrainSlopePosition.polygon[i].position.x, terrainSlopePosition.polygon[i].position.y)
        }
        ctx.closePath();
        ctx.stroke();

        if(terrainSlopePosition.children) {
            terrainSlopePosition.children.forEach(child => {
                this.drawPolygon(ctx, child);
            })
        }
    }
}