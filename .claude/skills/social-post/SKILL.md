---
name: social-post
description: Turn one Razarion gameplay clip into a ready-to-publish post spec for YouTube, X, TikTok and Instagram. Use when the user wants to announce, publish, or write copy for a clip, screenshot, devlog or release on social media.
---

# Writing a Razarion post spec

Produces `razarion-social/posts/<date>-<slug>.json`, which `razarion-social/src/cli.mjs` publishes to
all four platforms. Read `razarion-social/README.md` for what each platform does with the result.

## What to ask for, once

The clip path, and two or three sentences about what happens in it. Nothing else. If the user
already said what is in the clip, do not ask again — write the spec.

## House style

Established over several rounds with the user; do not drift from it.

- **Neutral, not promotional.** No "amazing", no "check this out", no exclamation marks. The game
  is interesting on its facts; state them.
- **Third person, not first.** Never "I built", "my game", "in this video I".
- **Short.** The user has repeatedly cut descriptions down. Default to four or five lines for
  YouTube, two or three for the rest. Length is opt-in, not the default.
- **Always both links** where the platform allows them: `https://www.razarion.com` and
  `https://github.com/Razarion/razarion`.
- **Alpha is stated, not hidden.**

The load-bearing facts, in rough order of how much they interest a stranger: runs in a browser
tab with no download and no account; one persistent world shared by every player; open source
under the LGPL; the engine is Java compiled to WebAssembly via TeaVM, rendering is Babylon.js,
UI is Angular, backend is Spring Boot; currently alpha.

## Per platform

- **YouTube** — title ≤ 70 characters so phones do not truncate it, formatted
  `Razarion – <Subject>`. Description leads with what the clip shows, then the two links. Add
  chapters only when the user gives timestamps; never invent them. Tags: 10–14 lowercase search
  phrases. Leave `privacy` at `private`.
- **X** — ≤ 280 characters including the link, which the user's audience there already knows the
  project from. Use `thread` only if there is genuinely more than one beat to the clip.
- **TikTok** — one line, phrased as the hook a stranger scrolling past would need. This is the
  one place where a question or a claim is appropriate, because nothing else earns the stop.
- **Instagram** — two or three lines plus 5–8 hashtags. `videoUrl` must be a public URL; if the
  user has not hosted the file, say so and leave the field for them to fill.

## Before handing it over

Run `node src/cli.mjs post <spec> ` from `razarion-social` (no `--live`). The dry run validates every
caption limit and path. Show the user the output, not the raw JSON.
