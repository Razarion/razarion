import {fakeAsync} from '@angular/core/testing';
import {TipTestbed} from './tip-testbed';

function run(body: (bed: TipTestbed) => void): string[] {
  const bed = new TipTestbed();
  body(bed);
  bed.finish();
  return bed.outcome();
}

/**
 * One case of the catalog (docs/architecture/quest-tip-case-catalog.md). Passes when the case's own
 * checks and every rule R1-R7 held throughout.
 */
export function tipCase(name: string, body: (bed: TipTestbed) => void): void {
  it(name, fakeAsync(() => {
    expect(run(body)).toEqual([]);
  }));
}

/**
 * A reported bug, pinned to the rules it broke. Checks only those rules, so it keeps guarding the
 * fix even if the same scene is also a case of its own.
 */
export function tipRegression(name: string, rules: string[], body: (bed: TipTestbed) => void): void {
  it(name, fakeAsync(() => {
    expect(run(body).filter(entry => rules.some(rule => entry.startsWith(rule + ' ')))).toEqual([]);
  }));
}

/**
 * A case the quests are prepared against (catalog scope): it need not teach anything, only fail
 * gracefully - no stuck tip, no wrong prompt, no exception. Checks the case's own expectations
 * and every rule except R1.
 */
export function tipGraceful(name: string, body: (bed: TipTestbed) => void): void {
  it(name, fakeAsync(() => {
    expect(run(body).filter(entry => !entry.startsWith('R1 '))).toEqual([]);
  }));
}
