// The formats: recipes for a post. Each one knows where its material comes from, what it makes of
// it and how to tell it has done a subject before. produce.mjs runs them.
//
// A format module exports one object:
//
//   name         what the ledger records as `format`
//   medium       photo | carousel | video - what it posts
//   summary      one line for --list
//   produce(ctx) -> { subject, text, link, tags, media, flags? }
//
// produce() throws NotReady when there is nothing to make right now - every subject done, a week
// too quiet to be worth a post, a draft waiting for a person - rather than making something weak.
// A format states only what its source says: no invented numbers, no sentence about how a unit
// "feels". A post that is wrong once costs more than a week without one.

import duel from './duel.mjs';
import weekInNumbers from './week-in-numbers.mjs';
import devlog from './devlog.mjs';
import battle from './battle.mjs';

export { NotReady } from './not-ready.mjs';

export const FORMATS = [battle, duel, weekInNumbers, devlog];

/**
 * Formats made elsewhere, listed so the overview shows the whole mix and not just the part this
 * directory produces.
 */
export const OTHER_FORMATS = [
  { name: 'clip', medium: 'video', summary: 'a filmed clip, via record_director / record_studio and compose.mjs' },
  { name: 'unit-card', medium: 'photo', summary: 'one unit and its numbers, via generate.mjs' },
];

export function formatByName(name) {
  return FORMATS.find((f) => f.name === name) || null;
}
