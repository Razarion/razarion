import {Injectable} from '@angular/core';
import {PlaceConfig} from '../../../../gwtangular/GwtAngularFacade';

export interface MarkerPoint {
  x: number;
  y: number;
}

/**
 * Where the quest wants the player to go, for the minimap: a point (a unit, an enemy, a field) or
 * an area (a quest region). Null when there is nothing to go to.
 */
export type QuestMarker =
  { kind: 'point', x: number, y: number } |
  { kind: 'polygon', corners: MarkerPoint[] } |
  { kind: 'circle', x: number, y: number, radius: number };

/**
 * The one place the quest target lives for the minimap.
 * <p>
 * The tip guide already works out where the player has to look - the direction arrow at the edge
 * of the screen points there - but that arrow gives a direction and no distance, and 42 % of the
 * target-off-screen stalls never resolved (PROD, 7 days to 23.09.2026). A marker on the map says
 * where, and a tap on the map takes the camera there.
 * <p>
 * Two writers: the tip guide for quests with a tip, the quest cockpit for the region of a quest
 * without one. Each writes under its own name, so one clearing its marker does not take down the
 * other's; the tip wins while it has one.
 */
@Injectable({
  providedIn: 'root'
})
export class QuestMarkerService {
  private readonly markers = new Map<'tip' | 'quest', QuestMarker | null>();
  private readonly listeners: (() => void)[] = [];
  private current: QuestMarker | null = null;

  set(source: 'tip' | 'quest', marker: QuestMarker | null): void {
    this.markers.set(source, marker);
    const next = this.markers.get('tip') ?? this.markers.get('quest') ?? null;
    if (!sameMarker(next, this.current)) {
      this.current = next;
      this.listeners.forEach(listener => listener());
    }
  }

  get(): QuestMarker | null {
    return this.current;
  }

  addListener(listener: () => void): void {
    this.listeners.push(listener);
  }

  removeListener(listener: () => void): void {
    const index = this.listeners.indexOf(listener);
    if (index >= 0) {
      this.listeners.splice(index, 1);
    }
  }

  /** A quest region as a marker: the circle or the polygon the PlaceConfig carries. */
  static fromPlaceConfig(placeConfig: PlaceConfig | null | undefined): QuestMarker | null {
    if (!placeConfig) {
      return null;
    }
    const position = placeConfig.getPosition();
    if (position) {
      return {kind: 'circle', x: position.getX(), y: position.getY(), radius: placeConfig.toRadiusAngular() || 1};
    }
    const corners = placeConfig.getPolygon2D()?.toCornersAngular().map(corner => ({x: corner.getX(), y: corner.getY()}));
    return corners && corners.length >= 3 ? {kind: 'polygon', corners} : null;
  }
}

/** Positions compared to a tenth of a unit: a target that moves by less is not worth a redraw. */
function sameMarker(one: QuestMarker | null, other: QuestMarker | null): boolean {
  if (one === null || other === null) {
    return one === other;
  }
  if (one.kind !== other.kind) {
    return false;
  }
  const close = (a: number, b: number) => Math.abs(a - b) < 0.1;
  switch (one.kind) {
    case 'point':
      return close(one.x, (other as typeof one).x) && close(one.y, (other as typeof one).y);
    case 'circle':
      return close(one.x, (other as typeof one).x) && close(one.y, (other as typeof one).y)
        && close(one.radius, (other as typeof one).radius);
    case 'polygon': {
      const corners = (other as typeof one).corners;
      return one.corners.length === corners.length
        && one.corners.every((corner, i) => close(corner.x, corners[i].x) && close(corner.y, corners[i].y));
    }
  }
}
