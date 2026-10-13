import {progressText, remainingSeconds} from './progress-eta';

describe('remainingSeconds', () => {
  it('knows nothing from a single sample or less than a second', () => {
    expect(remainingSeconds([{t: 0, value: 100}], 0)).toBeNull();
    expect(remainingSeconds([{t: 0, value: 100}, {t: 500, value: 98}], 0)).toBeNull();
  });

  it('counts a shrinking distance down to zero', () => {
    // 4 units a second, 80 left.
    expect(remainingSeconds([{t: 0, value: 88}, {t: 1000, value: 84}, {t: 2000, value: 80}], 0)).toBeCloseTo(20);
  });

  it('counts a growing buildup up to one', () => {
    // A tenth a second, half done.
    expect(remainingSeconds([{t: 0, value: 0.3}, {t: 2000, value: 0.5}], 1)).toBeCloseTo(5);
  });

  it('measures over the recent samples only, so a change of pace shows', () => {
    const samples = [{t: 0, value: 100}, {t: 1000, value: 100}, {t: 6000, value: 90}, {t: 7000, value: 80}, {t: 8000, value: 70}];
    // The standstill at the start is outside the window: 10 a second, 70 left.
    expect(remainingSeconds(samples, 0)).toBeCloseTo(7);
  });

  it('says nothing while there is no progress toward the target', () => {
    expect(remainingSeconds([{t: 0, value: 50}, {t: 2000, value: 50}], 0)).toBeNull();
    expect(remainingSeconds([{t: 0, value: 50}, {t: 2000, value: 55}], 0)).toBeNull();
  });
});

describe('progressText', () => {
  it('rounds up and never says zero seconds', () => {
    expect(progressText('driving', 11.2, 0)).toBe('On the way · 12 s');
    expect(progressText('building', 0.2, 0.99)).toBe('Building · 1 s');
  });

  it('falls back to no time on the way and to the share built', () => {
    expect(progressText('driving', null, 0)).toBe('On the way');
    expect(progressText('building', null, 0.4)).toBe('Building · 40 %');
  });
});
