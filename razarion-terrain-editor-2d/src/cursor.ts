import * as turf from '@turf/turf';
import {Feature, Polygon} from '@turf/turf';

export class Cursor {
    private cursor?: Feature<Polygon, any>;

    draw(ctx: CanvasRenderingContext2D) {
        if (!this.cursor) {
            return;
        }
        ctx.save();
        ctx.scale(1, -1);
        ctx.beginPath();
        ctx.moveTo(this.cursor.geometry.coordinates[0][0][0], this.cursor.geometry.coordinates[0][0][1]);
        for (let i = 1; i < this.cursor.geometry.coordinates[0].length - 1; i++) {
            ctx.lineTo(this.cursor.geometry.coordinates[0][i][0], this.cursor.geometry.coordinates[0][i][1])
        }
        ctx.closePath();
        ctx.stroke();
        ctx.restore();
    }

    move(x: number, y: number) {
        const radius = 20;
        const corners = 10;

        let deltaAngle = 2 * Math.PI / corners;
        let points = [];
        for (let i = 0; i < corners; i++) {
            let angleInRadians = i * deltaAngle;
            const newX = x + radius * Math.cos(angleInRadians);
            const newY = y + radius * Math.sin(angleInRadians);
            points.push([newX, newY])
        }
        points.push([points[0][0], points[0][1]])

        this.cursor = turf.polygon([
            points
        ]);
    }







}