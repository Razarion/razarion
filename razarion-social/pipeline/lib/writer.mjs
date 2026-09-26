// Writes a post's text for each network, in a chosen tone, through Claude Code in print mode.
//
// No API key and no second bill: `claude -p` runs on the Claude subscription this machine is
// logged in with, and a call counts against its usage like a chat message does. It needs that
// login to be there - an unattended run on a machine where nobody is logged in gets null back,
// and the caller falls back to the format's own template text.
//
// Why a writer at all: the pipeline's posts all read the same - one sentence, third person,
// "Recorded in the game's own engine" - and on X their reach per post fell to a twentieth of the
// hand-written ones before them, which were in the first person and asked the reader something.
// A tone is chosen per post and kept in the ledger, so which one carries can be measured.
//
// What the model may say is fenced in twice. The instructions allow only what the facts contain,
// and the answer is checked here: every number in it must be one of the numbers it was given,
// no link, no promise of a follow-up that nobody has planned. A failed check is handed back once
// with the reasons; a second failure falls back to the template. Every text still lands on
// "review" - this writes drafts, it decides nothing.

import { spawn } from 'node:child_process';
import { tmpdir } from 'node:os';
import { xLength } from './entries.mjs';

export const TONES = {
  question: 'First person, as the solo developer. Casual and direct. Ends with one genuine question to the reader that is easy to answer in a comment.',
  'matter-of-fact': 'Third person, calm and concrete. Says what is shown and why it is interesting. No question, no exclamation marks.',
  'behind-the-scenes': 'First person, as the solo developer. What I built or noticed, and why it matters to a player. Honest, a little nerdy.',
  punchy: 'Short and energetic. One strong opening line, then the essentials. At most one exclamation mark.',
};

const MODEL = 'sonnet';
const TIMEOUT_MS = 180_000;

const SYSTEM = `You write social media posts for Razarion, an open-source multiplayer real-time strategy game that runs in a browser tab: one persistent world shared by all players, no download, no account needed. It is made by a solo developer.

Rules - breaking any of them makes the post unusable:
- State only what the "facts" and "reference" fields say. Never invent numbers, features, plans, dates, events or anything about other players.
- Every number you write must appear in the facts or the reference. Do not compute new numbers (no damage per second, no totals, no percentages) unless they are given.
- Never promise anything: no "next post", "stay tuned", "coming soon", "tomorrow", "I'll show/run/post", no answer announced for later.
- Never name a player, a base or a bot.
- No links and no URLs anywhere - the pipeline adds them. No hashtags - the pipeline adds them.
- "reference" is the plain template version of this post. It shows what has to get across - do not copy its wording, its order or its lists. Pick the one or two facts that make the point and leave the rest to the picture.
- English. Write like a person, not like an ad. Do not end with the same slogan every time, and never write "Recorded in the game's own engine".

Answer with one JSON object and nothing else:
{"x": "...", "instagram": "...", "facebook": "...", "youtube_title": "...", "youtube_description": "..."}
- x: at most 240 characters.
- instagram: one to three short paragraphs.
- facebook: like instagram, may be a little longer.
- youtube_title: at most 70 characters, no clickbait, no emoji.
- youtube_description: one to three sentences.`;

const PROMISE = /\b(next (post|time|week)|stay tuned|coming soon|tomorrow|i'?ll (show|run|post|share|reveal)|we'?ll (show|run|post|share|reveal)|answer (in|next|below soon)|before i run)\b/i;

/** Numbers as the text writes them: 1,240 and 1240 and 1.5 are what they look like. */
function numbersIn(text) {
  return (String(text).match(/\d[\d,.]*/g) || [])
    .map((n) => n.replace(/[.,]$/, ''))
    .map((n) => (/^\d{1,3}(,\d{3})+$/.test(n) ? n.replace(/,/g, '') : n))
    .filter(Boolean);
}

export function checkWritten(written, allowedSource) {
  const problems = [];
  for (const key of ['x', 'instagram', 'facebook', 'youtube_title', 'youtube_description']) {
    if (typeof written?.[key] !== 'string' || !written[key].trim()) problems.push(`"${key}" is missing or empty.`);
  }
  if (problems.length) return problems;

  const allowed = new Set(numbersIn(allowedSource));
  const all = Object.values(written).join('\n');
  const invented = [...new Set(numbersIn(all).filter((n) => !allowed.has(n)))];
  if (invented.length) problems.push(`These numbers are not in the facts: ${invented.join(', ')}. Use only given numbers.`);
  if (/https?:\/\/|www\.|\.com\b/i.test(all)) problems.push('There is a link or a domain in the text. Remove it.');
  if (/(^|\s)#\w/.test(all)) problems.push('There is a hashtag in the text. Remove it.');
  const promise = all.match(PROMISE);
  if (promise) problems.push(`"${promise[0]}" promises something nobody has planned. Remove it.`);
  if (xLength(written.x) > 240) problems.push(`x is ${xLength(written.x)} characters, at most 240.`);
  if (written.youtube_title.length > 70) problems.push(`youtube_title is ${written.youtube_title.length} characters, at most 70.`);
  return problems;
}

function runClaude(prompt) {
  return new Promise((resolvePromise) => {
    // An empty working directory: run from the repository, the session would read its CLAUDE.md
    // and memory, which are about writing code, not posts.
    const child = spawn('claude', [
      '-p', '--output-format', 'json', '--model', MODEL,
      '--tools', '', '--strict-mcp-config', '--no-session-persistence',
      '--system-prompt', SYSTEM,
    ], { cwd: tmpdir(), windowsHide: true });
    let out = '';
    let err = '';
    const timer = setTimeout(() => child.kill(), TIMEOUT_MS);
    child.stdout.on('data', (d) => (out += d));
    child.stderr.on('data', (d) => (err += d));
    child.on('error', (e) => {
      clearTimeout(timer);
      resolvePromise({ error: `claude could not be started: ${e.message}` });
    });
    child.on('close', (code) => {
      clearTimeout(timer);
      try {
        const reply = JSON.parse(out);
        if (reply.is_error) return resolvePromise({ error: String(reply.result || 'error').slice(0, 300) });
        resolvePromise({ text: String(reply.result || '') });
      } catch {
        resolvePromise({ error: `claude exited ${code}: ${(err || out).slice(0, 300)}` });
      }
    });
    child.stdin.end(prompt);
  });
}

function parseReply(text) {
  const body = text.replace(/^```(?:json)?\s*/i, '').replace(/\s*```\s*$/, '');
  const start = body.indexOf('{');
  const end = body.lastIndexOf('}');
  if (start < 0 || end < start) return null;
  try {
    return JSON.parse(body.slice(start, end + 1));
  } catch {
    return null;
  }
}

/** The tone used longest ago - or never - among the ones the format allows. */
export function pickTone(ledger, allowed = Object.keys(TONES)) {
  const lastUse = (tone) => {
    const hits = ledger.items.filter((e) => e.tone === tone);
    return hits.length ? Date.parse(hits.at(-1).date) : 0;
  };
  return [...allowed].sort((a, b) => lastUse(a) - lastUse(b))[0];
}

/**
 * The texts for one post, or { written: null, reason } when there are none to be had.
 *
 * `reference` is the format's own template text - what the post has to get across - and `facts`
 * the data behind it. Both bound what the answer may contain.
 */
export async function writePost({ format, summary, facts, reference, tone }) {
  if (!TONES[tone]) throw new Error(`Unknown tone "${tone}". Known: ${Object.keys(TONES).join(', ')}`);
  const task = {
    format,
    about: summary,
    tone: `${tone}: ${TONES[tone]}`,
    facts: facts ?? {},
    reference,
  };
  const allowedSource = JSON.stringify(task.facts) + '\n' + reference;

  let prompt = JSON.stringify(task, null, 2);
  let lastProblems = [];
  for (let attempt = 1; attempt <= 2; attempt++) {
    const reply = await runClaude(prompt);
    if (reply.error) return { written: null, reason: reply.error };
    const written = parseReply(reply.text);
    const problems = written ? checkWritten(written, allowedSource) : ['The answer was not one JSON object.'];
    if (!problems.length) {
      return { written: Object.fromEntries(Object.entries(written).map(([k, v]) => [k, String(v).trim()])) };
    }
    lastProblems = problems;
    prompt = `${JSON.stringify(task, null, 2)}\n\nYour previous answer was rejected:\n- ${problems.join('\n- ')}\n\nWrite it again, fixing exactly these points.`;
  }
  return { written: null, reason: `rejected twice: ${lastProblems.join(' ')}` };
}
