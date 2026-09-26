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
});
