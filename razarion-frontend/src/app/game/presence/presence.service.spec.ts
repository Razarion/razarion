import {NgZone} from '@angular/core';
import {PresenceService} from './presence.service';

describe('PresenceService', () => {
  const zone = {run: (f: () => void) => f(), runOutsideAngular: (f: () => void) => f()} as unknown as NgZone;

  it('knows the online players by their base and names them', () => {
    const service = new PresenceService({} as any, zone);
    service.apply({count: 4, players: [{name: 'Cobalt-31', baseId: 7}, {name: 'Eagle-62', baseId: null}]});

    expect(service.onlineName(7)).toBe('Cobalt-31');
    expect(service.onlineName(8)).toBeUndefined();
    expect(service.namesText()).toBe('Cobalt-31, Eagle-62 and 2 more are playing right now');
  });

  it('speaks of one player in the singular, and forgets who went offline', () => {
    const service = new PresenceService({} as any, zone);
    service.apply({count: 1, players: [{name: 'Cobalt-31', baseId: 7}]});
    expect(service.namesText()).toBe('Cobalt-31 is playing right now');

    service.apply({count: 0, players: []});
    expect(service.onlineName(7)).toBeUndefined();
    expect(service.presence().count).toBe(0);
  });
});
