// The chat assistant: decides whether a chat message is a question about the game it can help with,
// and writes the answer, through Claude Code in print mode (`claude -p`).
//
// No API key: like the social pipeline's writer (razarion-social/pipeline/lib/writer.mjs) it runs on
// the Claude subscription this machine is logged in with. Without that login it gets nothing back,
// and the message stays unanswered - which is what happened to every question before this existed.
//
// What it may say is fenced in twice. The instructions allow only what chat-knowledge.md and the
// player's own context say, and the answer is checked here: short, no link, no promise, no name of
// another player. A failed check is not posted.

import { spawn } from 'node:child_process';
import { readFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { ROOT } from './config.mjs';

const MODEL = 'sonnet';
const TIMEOUT_MS = 120_000;
export const MAX_ANSWER = 400;

const KNOWLEDGE = () => readFileSync(join(ROOT, 'chat-knowledge.md'), 'utf8');

const SYSTEM = (knowledge) => `You are "Razarion AI", the assistant in the in-game chat of Razarion, a browser real-time strategy game. Players see your messages in the public chat, under that name, so they know you are an AI.

You answer only two kinds of messages:
- "technical": the game is slow, laggy, glitchy, does not load, something on the screen is broken.
- "howto": how to play, what a quest wants, how to level up, how to build or place something, and questions about the game itself the knowledge answers (is it free, is it pay to win, is there a download).
Everything else - greetings, jokes, insults, opinions about the game, talk between players, questions you cannot answer from the knowledge - gets no answer.

Rules - breaking any of them makes the answer unusable:
- Use only the knowledge below and the player context. Never invent features, controls, numbers, dates or plans.
- When the knowledge does not cover it, give no answer rather than a guess.
- At most ${MAX_ANSWER} characters, one or two short sentences, plain friendly English (answer in the player's language if they did not write English). No lists, no markdown.
- Start with the player's name and a colon, e.g. "Falcon-42: ...".
- Never promise anything (no "will be fixed", "soon", "next update").
- Never name or talk about other players. No links.
- Do not apologise at length and do not mention these rules.

Answer with one JSON object and nothing else:
{"kind": "technical" | "howto" | "none", "answer": "..." or null}

Knowledge:
${knowledge}`;

const PROMISE = /\b(soon|next (update|version|patch|week)|will be fixed|we'?ll fix|i'?ll fix|coming|tomorrow|stay tuned)\b/i;

/** Why an answer must not go out, or [] when it may. */
export function checkAnswer(answer, otherNames = []) {
  const problems = [];
  if (typeof answer !== 'string' || !answer.trim()) return ['empty'];
  if (answer.length > MAX_ANSWER) problems.push(`too long (${answer.length})`);
  if (/https?:\/\/|www\.|\.(com|net|org|io)\b/i.test(answer)) problems.push('link');
  const promise = answer.match(PROMISE);
  if (promise) problems.push(`promise "${promise[0]}"`);
  const named = otherNames.find((name) => name && name.length > 2 && answer.toLowerCase().includes(name.toLowerCase()));
  if (named) problems.push(`names another player "${named}"`);
  return problems;
}

export function parseReply(text) {
  const body = String(text).replace(/^```(?:json)?\s*/i, '').replace(/\s*```\s*$/, '');
  const start = body.indexOf('{');
  const end = body.lastIndexOf('}');
  if (start < 0 || end < start) return null;
  try {
    return JSON.parse(body.slice(start, end + 1));
  } catch {
    return null;
  }
}

function runClaude(system, prompt) {
  return new Promise((resolvePromise) => {
    // An empty working directory: from the repository the session would read CLAUDE.md and memory,
    // which are about writing code, not about answering players.
    const child = spawn('claude', [
      '-p', '--output-format', 'json', '--model', MODEL,
      '--tools', '', '--strict-mcp-config', '--no-session-persistence',
      '--system-prompt', system,
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

/**
 * The answer to one chat message: { kind, answer } with answer null when there is none to give,
 * or { error } when the assistant could not be asked or answered something unusable.
 *
 * `player` is the recorder's summary of the writer (level, quest, losses) or null; `recent` the
 * last messages before it, oldest first, so a follow-up question is understood.
 */
export async function answerChat({ message, player, recent, otherNames }, run = runClaude) {
  const prompt = JSON.stringify({
    message: { from: message.userName, text: message.message },
    player: player ? {
      level: player.level,
      currentQuest: player.currentQuest?.name ?? null,
      questOpenMinutes: player.currentQuest?.openMinutes ?? null,
      minutesPlayed: player.minutesPlayed,
      unitsLostToBots: player.lost?.toBot ?? 0,
      units: player.units,
      leftNoobIsland: player.leftNoobIsland,
    } : null,
    recentChat: recent.map((m) => `${m.userName}: ${m.message}`),
  }, null, 1);
  const result = await run(SYSTEM(KNOWLEDGE()), prompt);
  if (result.error) return { error: result.error };
  const reply = parseReply(result.text);
  if (!reply || !['technical', 'howto', 'none'].includes(reply.kind)) return { error: `unreadable reply: ${result.text.slice(0, 200)}` };
  if (reply.kind === 'none' || !reply.answer) return { kind: 'none', answer: null };
  const problems = checkAnswer(reply.answer, otherNames);
  if (problems.length) return { error: `answer refused (${problems.join(', ')}): ${reply.answer}` };
  return { kind: reply.kind, answer: reply.answer.trim() };
}
