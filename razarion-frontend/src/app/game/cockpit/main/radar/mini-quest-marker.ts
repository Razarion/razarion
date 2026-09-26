import {GameUiControl} from 'src/app/gwtangular/GwtAngularFacade';
import {BabylonRenderServiceAccessImpl} from 'src/app/game/renderer/babylon-render-service-access-impl.service';
import {AbstractGameCoordinates} from './abstract-game-coordinates';
import {MarkerPoint, QuestMarker} from './quest-marker.service';

/**
 * The quest target on the minimap: a ring on a point, an outline on a region - and, when the target
 * lies outside the part of the planet the map shows, a wedge on the map's edge pointing at it.
 * <p>
 * The wedge matters more than the ring. The map shows a window around the camera; the coast of
 * quest 386 and the far island of 392 lie outside it at the default zoom, and a marker that is
 * only drawn when it is on the map would be missing exactly when the player needs it.
 */
export class MiniQuestMarker extends AbstractGameCoordinates {
  /**
   * Magenta, with a dark edge: the map is green land, blue water and the server image's orange
   * squares - an orange marker (the first try) vanished among those.
   */
  private static readonly COLOR = '#ff3df5';
  private static readonly RING_PX = 5;
  private static readonly EDGE_INSET_PX = 7;
  private marker: QuestMarker | null = null;
  /** 0..1 through one pulse; the component advances it while a marker is up. */
  private phase = 0;

  constructor(gameUiControl: GameUiControl, renderService: BabylonRenderServiceAccessImpl) {
    super(gameUiControl, renderService);
  }

  setMarker(marker: QuestMarker | null): void {
    this.marker = marker;
  }

  hasMarker(): boolean {
    return this.marker !== null;
  }

  setPhase(phase: number): void {
    this.phase = phase;
  }

  protected draw(ctx: CanvasRenderingContext2D): void {
    const marker = this.marker;
    if (!marker) {
      return;
    }
    const anchor = this.anchorOf(marker);
    const onCanvas = this.realToCanvas(anchor.x, anchor.y);
    const inside = this.onCanvas(onCanvas.x, onCanvas.y);
    if (marker.kind !== 'point' && (inside || this.regionOnCanvas(marker))) {
      this.drawRegion(ctx, marker);
    }
    // Canvas pixels from here: the ring and the wedge keep their size at every zoom.
    ctx.setTransform(1, 0, 0, 1, 0, 0);
    if (inside) {
      this.drawRing(ctx, onCanvas.x, onCanvas.y);
    } else {
      this.drawWedge(ctx, onCanvas.x, onCanvas.y);
    }
  }

  /**
   * The one point that stands for the marker: the point itself, the circle's centre, and for a
   * polygon the point of its outline nearest to the camera - a bent coastal strip has its centroid
   * outside itself, and the wedge should point at the part the player is closest to.
   */
  private anchorOf(marker: QuestMarker): MarkerPoint {
    switch (marker.kind) {
      case 'point':
      case 'circle':
        return {x: marker.x, y: marker.y};
      case 'polygon': {
        const center = this.getViewField().getCenter();
        return nearestOnOutline(marker.corners, {x: center.getX(), y: center.getY()});
      }
    }
  }

  private regionOnCanvas(marker: QuestMarker): boolean {
    if (marker.kind === 'polygon') {
      return marker.corners.some(corner => {
        const p = this.realToCanvas(corner.x, corner.y);
        return this.onCanvas(p.x, p.y);
      });
    }
    return false;
  }

  private drawRegion(ctx: CanvasRenderingContext2D, marker: QuestMarker): void {
    ctx.lineWidth = this.toCanvasPixel(1.5);
    ctx.strokeStyle = MiniQuestMarker.COLOR;
    ctx.fillStyle = 'rgba(255, 61, 245, 0.25)';
    ctx.beginPath();
    if (marker.kind === 'circle') {
      ctx.arc(marker.x, marker.y, marker.radius, 0, 2 * Math.PI);
    } else if (marker.kind === 'polygon') {
      marker.corners.forEach((corner, i) => i === 0 ? ctx.moveTo(corner.x, corner.y) : ctx.lineTo(corner.x, corner.y));
      ctx.closePath();
    }
    ctx.fill();
    ctx.stroke();
  }

  private drawRing(ctx: CanvasRenderingContext2D, x: number, y: number): void {
    // The pulse: a halo leaving the ring and fading. A still dot among still dots is not found at a
    // glance; one thing moving on the map is.
    const halo = MiniQuestMarker.RING_PX + this.phase * 12;
    ctx.lineWidth = 2;
    ctx.strokeStyle = 'rgba(255, 61, 245, ' + (0.9 * (1 - this.phase)).toFixed(3) + ')';
    ctx.beginPath();
    ctx.arc(x, y, halo, 0, 2 * Math.PI);
    ctx.stroke();
    ctx.lineWidth = 4;
    ctx.strokeStyle = 'rgba(0, 0, 0, 0.7)';
    ctx.beginPath();
    ctx.arc(x, y, MiniQuestMarker.RING_PX, 0, 2 * Math.PI);
    ctx.stroke();
    ctx.lineWidth = 2;
    ctx.strokeStyle = MiniQuestMarker.COLOR;
    ctx.beginPath();
    ctx.arc(x, y, MiniQuestMarker.RING_PX, 0, 2 * Math.PI);
    ctx.stroke();
    ctx.fillStyle = MiniQuestMarker.COLOR;
    ctx.beginPath();
    ctx.arc(x, y, 1.5, 0, 2 * Math.PI);
    ctx.fill();
  }

  /**
   * A triangle just inside the edge, on the line from the map's centre to the target, pointing out.
   * On a circle rather than the square's border: the desktop dock clips the map round, and a wedge
   * in a corner of the square would be cut off.
   */
  private drawWedge(ctx: CanvasRenderingContext2D, x: number, y: number): void {
    const w = this.getWidth();
    const h = this.getHeight();
    const cx = w / 2;
    const cy = h / 2;
    const dx = x - cx;
    const dy = y - cy;
    if (dx === 0 && dy === 0) {
      return;
    }
    const inset = MiniQuestMarker.EDGE_INSET_PX;
    const radius = Math.min(cx, cy) - inset;
    const length = Math.sqrt(dx * dx + dy * dy);
    const ex = cx + dx / length * radius;
    const ey = cy + dy / length * radius;
    const angle = Math.atan2(dy, dx);
    const size = 6 + 3 * Math.sin(this.phase * Math.PI);
    ctx.fillStyle = MiniQuestMarker.COLOR;
    ctx.strokeStyle = '#000';
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(ex + Math.cos(angle) * size, ey + Math.sin(angle) * size);
    ctx.lineTo(ex + Math.cos(angle + 2.4) * size, ey + Math.sin(angle + 2.4) * size);
    ctx.lineTo(ex + Math.cos(angle - 2.4) * size, ey + Math.sin(angle - 2.4) * size);
    ctx.closePath();
    ctx.fill();
    ctx.stroke();
  }

  private onCanvas(x: number, y: number): boolean {
    return x >= 0 && y >= 0 && x <= this.getWidth() && y <= this.getHeight();
  }
}

export function nearestOnOutline(corners: MarkerPoint[], from: MarkerPoint): MarkerPoint {
  let best = corners[0];
  let bestDistance = Infinity;
  for (let i = 0, j = corners.length - 1; i < corners.length; j = i++) {
    const a = corners[j];
    const b = corners[i];
    const dx = b.x - a.x;
    const dy = b.y - a.y;
    const lengthSquared = dx * dx + dy * dy;
    const t = lengthSquared > 0 ? Math.max(0, Math.min(1, ((from.x - a.x) * dx + (from.y - a.y) * dy) / lengthSquared)) : 0;
    const p = {x: a.x + t * dx, y: a.y + t * dy};
    const distance = (p.x - from.x) ** 2 + (p.y - from.y) ** 2;
    if (distance < bestDistance) {
      best = p;
      bestDistance = distance;
    }
  }
  return best;
}
