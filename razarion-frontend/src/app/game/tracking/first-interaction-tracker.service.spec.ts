import {FirstInteractionTrackerService} from './first-interaction-tracker.service';

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
