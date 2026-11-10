# Razarion Recorder

Watches the live planet the way a player's browser does and keeps what it sees in memory. It is the
ground for three things that come later: a funnel analysis an AI can query, shorts cut from player
moments (leaving the noob island first), and a replay of what players did.

## What it does

- **One game connection** to `/gameconnection` with its own account. The server sends every client
  the whole planet - all bases, all units, all shots, no filter - so this is everything there is
  to see. The account never has a base and gives no orders; the server ignores anything it sends
  (`ClientGameConnection.readOnly`).
- **The tape** (`lib/tape.mjs`): every message as it came, with its arrival time. Dropped oldest
  first after `RECORDER_KEEP_HOURS` or beyond `RECORDER_MAX_MB`.
- **Keyframes**: the server sends the whole world only on connect and then only changes. Every
  `RECORDER_KEYFRAME_MINUTES` a second connection fetches a fresh world and closes again, so a
  replay can start anywhere.
- **History feed** (`/rest/recorder/events`): quests, levels, XP and boxes reach only the player's
  own client. The server keeps its history rows in memory for two hours for the recorder.
- **World** (`lib/world.mjs`): bases, units, and the events the wire implies - unit built, unit
  destroyed and by whom, box picked.
- **Dashboard** at http://127.0.0.1:4712, read-only. `/api/status`, `/api/bases`, `/api/base/<id>`,
  `/api/events`.

## The chat and its assistant

`lib/chat.mjs` reads the planet's chat from `/rest/recorder/chat` every `RECORDER_CHAT_SECONDS` (5) and
keeps each message with the writer's level and open quest at that moment (`state/chat.json`, which
also holds the last id, so a restart never answers a message twice and a fresh start answers nothing
old).

`lib/assistant.mjs` decides per message whether it is a technical problem (lag, loading, something
broken) or a how-to question, and answers those as "Razarion AI" through `/rest/recorder/chat/answer`
- the recorder's one write. Everything else (greetings, jokes, insults) gets no answer. It runs
`claude -p` on this machine's Claude login, like the social pipeline's writer, and may only say what
`chat-knowledge.md` and the player context say; an answer with a link, a promise or another player's
name is not posted. Not answered: messages older than 10 minutes when seen, the same question
repeated, more than 30 answers an hour. `RECORDER_CHAT_ANSWER=0` keeps reading and answers nothing.

The server itself posts as "Razarion" when a player leaves the noob island (ChatService, fed by the
community ticker); the recorder keeps those as `news` and answers none of them.

## Analysis, and the AI's way in

`lib/players.mjs` keeps one record per player from the history feed (quests, levels, built, lost)
and the wire (units set going - the nearest the recorder comes to seeing the player act). On start,
and every ten minutes, `/rest/recorder/players` adds level and open quest of players who were
already playing.

| API | MCP tool (`mcp.mjs`) | What |
|---|---|---|
| `/api/status` | `recorder_status` | connected?, since when, how much held |
| `/api/players` | `list_players` | every player: status, level, open quest, losses, noob island |
| `/api/player/<userId>` | `player_story` | one player in order, with pauses of 3+ min |
| `/api/funnel` | `funnel` | per quest step: reached, median minutes, stopped here |
| `/api/stalls?min=5` | `stalls` | players on a quest open at least that long |
| `/api/events` | `recent_events` | the latest events involving players |
| `/api/chat` | `chat_log` | the chat with player context and what the assistant made of it |

The MCP server is in the repo's `.mcp.json` as `razarion-recorder`; it asks the running recorder at
`RECORDER_URL` (default http://127.0.0.1:4712). It hands out user ids and base ids, never names.

Quests mostly have no name; they are called by what they ask for ("L1 Build 1 Factory"), with the
same words as the backend funnel (`lib/quest-text.mjs`).

`npm test` runs the tests of the player model and the replay track.

## Replay

http://127.0.0.1:4712/player (or "replay" next to a player on the dashboard): the planet as a map, a
stretch of the tape played back - the last N minutes, or one player's time from their base on.
Units are dots, buildings squares, each player in a colour, bots grey-red; shots are lines and
explosions rings. The time line carries the moments that matter (quest passed, level, base,
losses); a click on one in the list jumps there. Wheel zooms, drag moves, "Follow" keeps a base
in view.

`/api/replay` (`lib/replay.mjs`) builds the track: the world at the start from the last keyframe
before it, then every position the server sent with the path the unit was on. The page walks a
unit along that path to where it was next seen - the tape knows the future, so nothing is guessed
but the pace in between. A stretch is at most 90 minutes.

## Replay in the real game client

"3D from here" in the 2D player opens the followed base in the real game client, at the moment on
screen; pressed again for the same base it jumps there in the same tab. The client is the normal one
on its `/game/replay` route (ReplayService in the frontend), served through the recorder's proxy at
http://127.0.0.1:4713 (`lib/replay-server.mjs`):

- `/gameconnection` is answered from the tape: the world at that moment (keyframe plus the tape up
  to it), then the tape as it happened. The client is told the followed base is its own, so it
  opens on it and draws it the way its player saw it.
- `/systemconnection` is accepted and stays silent. REST goes to the game server with the recorder's
  token, the tracking calls and every write but the context load are answered by the proxy.
- `/replay/status` tells the client which base and where its units or its fight are; the director's
  follow camera frames that. The renderer alone only knows what is in its view field.

Limits: it plays at the speed it happened (the client's worker ticks in real time and holds back any
tick info ahead of it), a jump to another base reloads the page, and the base has to stand at the
start (the replay starts when it is created). Orders given in the client are ignored by the proxy;
the client may still move a clicked unit for a moment.

`RECORDER_REPLAY_UI=http://localhost:4201` serves the pages from an Angular dev server instead of the
game server, so a frontend change needs no server build. `RECORDER_REPLAY_PORT` moves the proxy.

## Shorts from players' moments

`rules.json` says which moments become a short: a game_history row (leaving the noob island is
QUEST_PASSED 392), how many seconds before and after it to film, in which shapes, how often. It is
read anew for every row. "Short from here" in the 2D player marks a moment by hand.

`lib/moments.mjs` films a moment once the tape holds all of it: the stretch replayed in a headless
Chrome through the replay proxy (a session of its own, so a person watching another replay is not
disturbed), the camera on the player's base, the canvas recorded by the director's recorder - names
locked off, no HUD in the picture. A take under 20 fps or under 80 % of its length is refused, the
same as razarion-social's record_director.mjs does.

The takes go to the social engine (`POST /api/moments` on 127.0.0.1:4711, `RECORDER_ENGINE_URL`),
which makes the post with its `moment` format - cut, worded by the writer, into the ledger - and
publishes it in its slots. It is approved by the engine's checks like the planner's posts
(`moments.autoApprove`, on by default; off, it waits on the review page). A handover the engine does not take is tried
again every ten minutes. The dashboard lists the moments and where each one is.

The takes are kept in `state/moments/` (the last 100 moments).

A rule with `"chat": true` (leaving the noob island) also goes into the planet chat (`lib/chat-video.mjs`):
the portrait take made small with ffmpeg (540 wide, no sound, faststart, about 4 MB for 47 s) and
posted to `/rest/recorder/chat/video`. The server keeps the newest 50 and posts "<name>'s crossing to the
mainland" under "Razarion"; the chat shows a Watch button and loads the clip from our own server only
when it is clicked. What became of it is on the moment (`chat`: posted with the video id, or why not).

## Memory

In memory only. On disk (`state/`) are the log and `volume.json`, the measured volume per hour,
which hold sizes and counts and nothing of the game.

## Setup

1. Register an account on razarion.com for the recorder (it never plays).
2. Set its e-mail on the server: `RAZARION_RECORDER_EMAIL` (k8s deployment). The server then gives
   it the role RECORDER: read-only game connection, no ad-network events, `/rest/recorder`.
3. `razarion-recorder/.env`:

   ```
   RAZARION_BASE_URL=https://www.razarion.com
   RAZARION_RECORDER_EMAIL=...
   RAZARION_RECORDER_PASSWORD=...
   # optional
   RECORDER_KEEP_HOURS=6
   RECORDER_MAX_MB=512
   RECORDER_KEYFRAME_MINUTES=5
   RECORDER_PORT=4712
   ```

4. `npm install`, then `node recorder.mjs`, or `scheduled\install-recorder.ps1` for the Windows task.
