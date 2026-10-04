import {QuestMarkerService} from './quest-marker.service';
import {nearestOnOutline} from './mini-quest-marker';

describe('QuestMarkerService', () => {

  it('the tip marker wins over the quest region, and the region comes back when the tip clears', () => {
    const service = new QuestMarkerService();
    const changes: number[] = [];
    service.addListener(() => changes.push(1));
    service.set('quest', {kind: 'circle', x: 10, y: 10, radius: 5});
    service.set('tip', {kind: 'point', x: 1, y: 2});
    expect(service.get()).toEqual({kind: 'point', x: 1, y: 2});
    service.set('tip', null);
    expect(service.get()).toEqual({kind: 'circle', x: 10, y: 10, radius: 5});
    expect(changes.length).toBe(3);
  });

  it('does not notify for a target that moved by less than a tenth of a unit', () => {
    const service = new QuestMarkerService();
    let changes = 0;
    service.addListener(() => changes++);
    service.set('tip', {kind: 'point', x: 1, y: 2});
    service.set('tip', {kind: 'point', x: 1.05, y: 2});
    expect(changes).toBe(1);
  });

  it('points at the part of a bent strip nearest to the camera, not at its centroid', () => {
    // An L: its centroid lies outside it.
    const corners = [{x: 0, y: 0}, {x: 10, y: 0}, {x: 10, y: 2}, {x: 2, y: 2}, {x: 2, y: 10}, {x: 0, y: 10}];
    expect(nearestOnOutline(corners, {x: 20, y: 1})).toEqual({x: 10, y: 1});
    expect(nearestOnOutline(corners, {x: 1, y: 20})).toEqual({x: 1, y: 10});
  });

  describe('jumpPoint (the quest line takes the camera there)', () => {
    it('goes to a point and to a circle\'s centre, and nowhere without a marker', () => {
      expect(QuestMarkerService.jumpPoint({kind: 'point', x: 3, y: 4}, null)).toEqual({x: 3, y: 4});
      expect(QuestMarkerService.jumpPoint({kind: 'circle', x: 7, y: 8, radius: 5}, {x: 100, y: 100})).toEqual({x: 7, y: 8});
      expect(QuestMarkerService.jumpPoint(null, {x: 0, y: 0})).toBeNull();
    });

    it('goes into a bent region, at the part nearest to the camera', () => {
      // An L: its centroid lies outside it.
      const corners = [{x: 0, y: 0}, {x: 10, y: 0}, {x: 10, y: 2}, {x: 2, y: 2}, {x: 2, y: 10}, {x: 0, y: 10}];
      const inside = (p: { x: number, y: number }) => (p.x >= 0 && p.x <= 10 && p.y >= 0 && p.y <= 2)
        || (p.x >= 0 && p.x <= 2 && p.y >= 0 && p.y <= 10);
      const nearRight = QuestMarkerService.jumpPoint({kind: 'polygon', corners}, {x: 20, y: 1})!;
      const nearTop = QuestMarkerService.jumpPoint({kind: 'polygon', corners}, {x: 1, y: 20})!;
      expect(inside(nearRight)).toBeTrue();
      expect(inside(nearTop)).toBeTrue();
      expect(nearRight.y).toBeLessThan(2);
      expect(nearTop.y).toBeGreaterThan(2);
    });
  });
});
