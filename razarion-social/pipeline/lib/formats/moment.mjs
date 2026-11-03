// A moment a real player had on the live planet - leaving the noob island first - filmed by
// razarion-recorder from its recording of the planet and handed to the engine (POST /api/moments).
//
// The recorder does the filming: it replays the stretch around the moment in the game's own client,
// the camera following the player's base, and records the canvas. Here it is only cut and worded,
// the same way a battle is.
//
// Nobody is named: not in the picture (the replay client draws no names and the take is the canvas
// only), not in the text (the facts carry no name, no id). The player is "a player".
//
// Not planned: plan.mjs never runs it. There is no moment until the recorder sends one, and then
// the engine runs `produce.mjs --format moment --input <file>`.

import { existsSync } from 'node:fs';
import { readJson, toRelative } from '../paths.mjs';
import { cutClip } from '../cut.mjs';
import { probeVideo } from '../video.mjs';
import { NotReady } from './not-ready.mjs';
import { step } from '../../../src/util/log.mjs';

const LINK = 'https://www.razarion.com';

export default {
  name: 'moment',
  medium: 'video',
  summary: 'a real player\'s moment on the live planet, replayed and filmed by razarion-recorder',
  tones: ['punchy', 'question', 'behind-the-scenes'],

  /**
   * ctx.args.input: the moment the engine received, as a JSON file -
   *   { id, rule, title, text, shows, facts, takes: { portrait, landscape }, cut: { start, length } }
   * takes are absolute paths to the recorder's raw takes; cut is the part of them to post.
   */
  async produce(ctx) {
    if (!ctx.args.input) throw new NotReady('A moment comes from razarion-recorder (--input <file>); there is nothing to plan here.');
    const moment = readJson(String(ctx.args.input));
    if (!moment) throw new Error(`No moment in ${ctx.args.input}.`);

    const media = { type: 'video', file: null, portrait: null, landscape: null, url: null };
    const problems = [];
    for (const shape of ['portrait', 'landscape']) {
      const take = moment.takes?.[shape];
      if (!take) continue;
      if (!existsSync(take)) {
        problems.push(`${shape} take missing: ${take}`);
        continue;
      }
      const probe = await probeVideo(take);
      const start = Math.max(0, Math.min(moment.cut?.start ?? 0, probe.duration - 1));
      const length = Math.min(moment.cut?.length ?? probe.duration, probe.duration - start);
      const out = ctx.mediaFile(`moment-${shape}`, 'mp4');
      await cutClip(take, out, { start, length });
      step(`cut ${shape}: ${start.toFixed(1)}s + ${length.toFixed(1)}s`);
      media[shape] = toRelative(out);
    }
    if (!media.portrait && !media.landscape) throw new Error(problems.join(' ') || 'The moment came without a take.');
    media.file = media.portrait || media.landscape;

    return {
      subject: `moment:${moment.rule}:${moment.id}`,
      text: moment.text,
      link: LINK,
      tags: ['moment', moment.rule],
      media: [media],
      flags: media.landscape ? [] : ['no-landscape-take'],
      facts: {
        ...(moment.facts || {}),
        what_the_clip_shows: moment.shows,
        who: 'a real player on the live server, not named',
        how: 'replayed from a recording of the live planet in the game\'s own engine, the camera following that player',
      },
    };
  },
};
