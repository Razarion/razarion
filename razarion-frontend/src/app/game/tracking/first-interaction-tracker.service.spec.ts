import {FirstInteractionTrackerService, groupSizeDetail} from './first-interaction-tracker.service';

/**
 * What the tracker sends and what it swallows.
 *
 * Each kind used to be sent exactly once, which is right for the questions the interaction kinds
 * answer - "did the player ever pan, select, command". It is wrong for ENGINE_ERROR, where the
 * second reason is not a repeat of the first and may well be the one that explains the defect. A
 * broken tick repeats every tick, though, so the cap has to stay.
 */
describe('First interaction tracker', () => {
  let sent: { kind: string, detail: string | null }[];
  let service: any;

  beforeEach(() => {
    sent = [];
    service = Object.create(FirstInteractionTrackerService.prototype);
    (service as any).reported = new Set<string>();
    (service as any).countPerKind = new Map<string, number>();
    service.trackerControllerImplClient = {
      firstInteraction: (json: any) => {
        sent.push({kind: json.kind, detail: json.detail});
        return Promise.resolve();
      }
    };
    (window as any).RAZ_gameSessionUuid = 'PGTEST123';
    (window as any).RAZ_pageLoadedAt = Date.now();
  });

  afterEach(() => {
    delete (window as any).RAZ_gameSessionUuid;
    delete (window as any).RAZ_pageLoadedAt;
  });

  it('sends a kind without a detail exactly once', () => {
    service.report('POINTER_DOWN');
    service.report('POINTER_DOWN');

    expect(sent.length).toBe(1);
  });

  it('sends a second reason, because it is not a repeat of the first', () => {
    service.report('ENGINE_ERROR', 'tick did not complete');
    service.report('ENGINE_ERROR', 'onTickUpdate: Cannot read properties of null');

    expect(sent.map(s => s.detail)).toEqual([
      'tick did not complete',
      'onTickUpdate: Cannot read properties of null'
    ]);
  });

  it('swallows the same reason repeated', () => {
    service.report('ENGINE_ERROR', 'onTickUpdate: boom');
    service.report('ENGINE_ERROR', 'onTickUpdate: boom');

    expect(sent.length).toBe(1);
  });

  it('stops after a handful, because a broken tick repeats every tick', () => {
    for (let i = 0; i < 20; i++) {
      service.report('ENGINE_ERROR', 'reason ' + i);
    }

    expect(sent.length).toBe(5);
  });

  it('counts the cap per kind, not across all of them', () => {
    for (let i = 0; i < 10; i++) {
      service.report('ENGINE_ERROR', 'reason ' + i);
    }
    service.report('POINTER_DOWN');

    expect(sent.length).toBe(6);
    expect(sent[5].kind).toBe('POINTER_DOWN');
  });

  it('says nothing at all without a game session, which is what it joins on', () => {
    delete (window as any).RAZ_gameSessionUuid;

    service.report('POINTER_DOWN');

    expect(sent.length).toBe(0);
  });
});

/**
 * Group sizes are bucketed rather than counted. The cap allows five distinct details per kind and
 * per session, and a player who selects two, then three, then five units would spend it on
 * counting instead of on the question: was a group formed, and was it the three quest 379 needs.
 */
describe('Group size detail', () => {
  it('keeps the sizes that mean something apart', () => {
    expect(groupSizeDetail(2)).toBe('units=2');
    expect(groupSizeDetail(3)).toBe('units=3');
  });

  it('puts everything above three in one bucket, so the cap is never spent on counting', () => {
    expect(groupSizeDetail(4)).toBe('units=4plus');
    expect(groupSizeDetail(9)).toBe('units=4plus');
    expect(groupSizeDetail(40)).toBe('units=4plus');
  });

  it('leaves room under the cap for the whole vocabulary', () => {
    const alle = new Set([2, 3, 4, 5, 12, 30].map(groupSizeDetail));
    expect(alle.size).toBeLessThan(5);
  });
});
