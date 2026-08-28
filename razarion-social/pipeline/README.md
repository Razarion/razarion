# razarion-social-pipeline

Mirrors what @razariongame posts on X to Instagram, Facebook and YouTube - one reviewed step at a
time. Built for the 2026 backfill and kept for what comes after it. Sits next to `razarion-social`,
and reuses its `.env`, HTTP layer and logging.

| Target | How it gets there | State |
|---|---|---|
| Instagram | `build_captions` → `render_cards` → `upload_media` → `publish` | `state/posted.json` |
| Facebook | `build_fb_posts` → `upload_media --source fb` → `publish_fb` | `state/posted_fb.json` |
| YouTube | `build_yt_posts` → you, in Studio | none - not automated, and why is below |

Posts are written here now rather than mirrored from X: `generate.mjs` builds one from the live
game data, `compose.mjs` from your own clip and a couple of sentences. `fetch_posts.mjs` and the
two builders remain for the 2026 backfill they were written for.

**[ANLEITUNG.md](ANLEITUNG.md) is the cheat sheet** - what to type, in order. This file explains why
things are the way they are.

```bash
cd razarion-social/pipeline
npm install                 # one dependency, for rendering text cards
node fetch_posts.mjs        # X timeline  -> data/posts.json
node build_captions.mjs     # X text      -> data/captions.json   <- you review this by hand
node render_cards.mjs       # text-only posts -> data/cards/*.jpg
node upload_media.mjs       # media -> GitHub release, public URLs
node publish.mjs            # dry run. --live actually posts
```

Each step writes JSON to disk and reads the previous step's file, so you can look at every stage
before letting the next one run. `publish.mjs` posts nothing without `--live`.

## Where the posts come from

The X archive export is not needed. `GET /2/users/:id/tweets` reaches back 3200 posts and accepts
`start_time`, which covers 2026 comfortably. The seven-day window people run into belongs to
recent search, not to this endpoint.

Reads are billed per post returned, currently about **$0.005 each**, so a few hundred posts cost
one or two dollars. `fetch_posts.mjs` prints what a run cost when it finishes. `--limit N` caps it.

Set `X_BEARER_TOKEN` in `razarion-social/.env`: an app-only bearer token from developer.x.com, from a
project with read access and billing enabled. The `X_CLIENT_ID` / `X_CLIENT_SECRET` pair already in
that file is for OAuth2 posting and cannot read a timeline.

## What gets kept, and what does not

| On X | Here |
|---|---|
| Original post | one Instagram post |
| Self-reply chain (a thread) | **merged into one** Instagram post, parts joined by a blank line |
| Reply to somebody else | skipped - it makes no sense lifted out of the conversation |
| Retweet | skipped |
| Quote post | kept, but flagged `quote` for review: the quoted post is invisible on Instagram |
| Thread whose first post is older than the window | skipped as `thread-part-without-root` |

Everything skipped is written to the `skipped` array in `posts.json` with a reason, so nothing
disappears silently.

`fetch_posts.mjs` deliberately does not pass `exclude=replies` to the API. On X a thread is a chain
of self-replies, so excluding replies server-side would drop every thread continuation the account
ever posted. The distinction between a self-reply and a reply to someone else is made locally.

## The caption rules

`build_captions.mjs` applies what an Instagram caption can and cannot do:

- **Links come out.** They are not clickable there. A sentence that existed only to hand over to a
  link ("Play it at https://...") goes with the link; a sentence that says something of its own
  keeps its text and is flagged `link-remnant`. The caption ends with `Link in bio.` instead.
- **@mentions lose the @.** A bare handle in an Instagram caption links to whoever holds that name
  on Instagram, which is almost never the account meant on X. The name survives as plain text and
  the entry is flagged `had-mentions`.
- **Hashtags** are the six that fit the account (`#rts #indiedev #opensource #webassembly
  #browsergame #gamedev`), topped up with at most four drawn from what the post actually mentions,
  capped at ten.
- `--date-line` adds `Originally posted on 14 March 2026.` Instagram cannot backdate, so without it
  a backfill of forty posts all reads as posted today. Off by default.

## The review step

`captions.json` is the point of the whole thing. Every entry lands with `"status": "review"`, and
the publish step will only ever touch entries set to `"ok"`:

```json
{
  "id": "1234567890",
  "status": "review",        // -> "ok" to publish, "skip" to leave out
  "edited": false,           // -> true after you rewrite the caption
  "flags": ["quote", "needs-card"],
  "caption": "...",
  "source_text": "what it said on X"
}
```

Re-running `build_captions.mjs` keeps every entry you have touched - anything with `edited: true`
or a status other than `review` survives untouched, and only the machine-derived fields refresh.
`--force` regenerates everything and throws those edits away.

Flags worth looking at: `needs-card` (no media, so it cannot be posted until the card step renders
one), `quote` (refers to a post nobody on Instagram can see), `empty-text` (the post was only media
and a link), `link-remnant`, `too-long`.

## Text cards

Instagram refuses a post that has neither an image nor a video, so `render_cards.mjs` gives the
text-only posts something to be. 1080x1350, the game's own loading-screen palette (`#1c1917`
ground, `#10b981` accent, taken from `razarion-frontend/src/index.html`), type size chosen to fit
the text. Hashtags and "Link in bio." stay out of the picture - they are already under it.

A card is rendered only for entries that are not set to `skip`, and the entry's media then points
at the card. Cards for captions you have shortened are re-rendered with `--force`.

## Uploading

Instagram never accepts a file. It takes a URL and fetches the media itself, so everything has to
be public first. Two hosts are supported, both reduced to the same two calls:

- **`github`** (default) - the files hang off a GitHub release (`instagram-media`, marked as a
  prerelease) in the public repo. No payment details anywhere, and nothing enters the git history.
  Needs `GITHUB_TOKEN`, a fine-grained token with *Contents: read and write*.
- **`r2`** (`--to r2`) - Cloudflare R2 with a signed PUT, no AWS SDK, 40 lines of HMAC. Needs a
  card on file at Cloudflare even though the usage is free, and a **public** bucket: enable the
  `r2.dev` subdomain or attach a custom domain and put that base in `R2_PUBLIC_BASE_URL`.

Images are prepared before they go up, because both of these are rejected at **publish** time
rather than at upload time:

- **Format.** Instagram publishes JPEG and nothing else, so a PNG is re-encoded.
- **Aspect ratio.** Only 4:5 to 1.91:1 is accepted. A UI panel at 3.7:1 or a phone capture at
  0.52:1 is padded onto the game's background colour rather than cropped - the content is the
  point, and on a dark screenshot the bars are close to invisible. Measured in practice:
  Instagram is more forgiving than its own documentation for a single image (0.69:1 went through),
  and strict for carousels, which is where it first bit.

Objects are keyed by the hash of their contents. Re-rendering a card produces the same filename
with different bytes, and reusing the URL would leave Instagram serving the old image from cache;
a changed file becomes a new object instead.

**About the GitHub route.** A release asset answers with a 302 to a signed URL that carries
`Content-Disposition: attachment` and `Content-Type: application/octet-stream`, whatever type it
was uploaded with. That looked like it would fail, and it does not: Meta's fetcher follows the
redirect and reads the bytes regardless. Verified by container status reaching `FINISHED` and by
live posts of both images and Reels.

If it ever does start refusing, the fallback is `raw.githubusercontent.com` from a separate media
repo - that serves `200` with a real `Content-Type` and no redirect, at the cost of the files
living in a git history.

## Publishing

`publish.mjs` is a **dry run unless you pass `--live`**. Publishing cannot be undone and the
account is public, so the safe path is the default one.

```bash
node publish.mjs                    # what it would do
node publish.mjs --live --limit 1   # one post, to watch it land
node publish.mjs --live             # the rest, oldest first, 60s apart
node publish.mjs --live --pause 120 # slower
```

- Only entries with `"status": "ok"` are ever posted. `review` and `skip` are left alone.
- `state/posted.json` records every published post and is written after **each** one, so an
  interrupted run never republishes what already went out.
- Images go up as `IMAGE`, videos as `REELS`, several files as a `CAROUSEL` of up to ten. **Every**
  container is polled until `status_code` is `FINISHED`, images included. The documentation frames
  processing as a video concern, but an image container is `IN_PROGRESS` for a few seconds too, and
  publishing inside that window fails with 9007 / 2207027, "Media ID is not available".
- **Two different limits, and the one that bites is not the documented one.** The publishing quota
  is 100 posts per 24 hours, and the script asks
  `GET /{ig-user-id}/content_publishing_limit` what is left rather than counting its own, since
  posts made from the phone count against the same window. The one that actually stops a backfill
  is the *app-level* rate limit: roughly 200 API calls per hour for an app with a single user, and
  one post costs four to eight calls. Measured in practice: 26 posts in half an hour hit the wall
  with error 4, "Application request limit reached".

  A throttle (codes 4, 17, 32, 613) is treated as "come back later", not as a failure: the run
  waits `--throttle-wait` seconds (default 15 minutes) and tries the same post again, up to twice.
  For a long backfill, `--pause 180` keeps the rate at about 19 posts and 130 calls per hour, with
  room under the ceiling.
- 5xx is retried with backoff. **4xx stops the run**, prints Meta's message and, where the code is
  a known one, what usually causes it - `190` is an expired token, `9004` is media Instagram could
  not fetch, `10` is the publishing permission not being through App Review.

## What Instagram needs on its side

- An Instagram **Business** account. Creator accounts cannot publish through the API.
- A Meta app with `instagram_business_basic` and `instagram_business_content_publish`, both
  **approved in App Review**. Without the review the endpoints exist but refuse to publish.
- A long-lived access token in `.env` as `INSTAGRAM_ACCESS_TOKEN`, plus `INSTAGRAM_USER_ID`. The
  token expires after 60 days.

## Files

```
fetch_posts.mjs        X timeline -> data/posts.json (+ data/raw-timeline.json, + data/media/)
build_captions.mjs     posts.json -> data/captions.json
render_cards.mjs       text-only entries -> data/cards/*.jpg
upload_media.mjs       media -> GitHub release (or R2) -> data/uploads.json
publish.mjs            captions.json -> Instagram, state/posted.json
build_fb_posts.mjs     posts.json -> data/fb_posts.json
publish_fb.mjs         fb_posts.json -> Facebook Page, state/posted_fb.json
compose.mjs            one new post -> all three review files
generate.mjs           the next unit from the live game -> all three, card and all
lib/razarion.mjs       admin login, unit types, images from the running server
lib/entries.mjs        one text -> the three shapes the feeds want
publish_x.mjs          x_posts.json -> X, state/posted_x.json
fb_token.mjs           user token -> permanent Page token
build_yt_posts.mjs     posts.json -> data/youtube/ for a manual Studio upload
sync_new.mjs           the whole chain for both networks, for whatever is new
scheduled/run.ps1      what the scheduled task calls: token, sync, deliver
refresh_token.mjs      swaps the Instagram token for a fresh 60-day one
check.mjs              preflight: is everything ready for a live run
lib/x.mjs              X API client: pagination, rate-limit retry, media variant picking
lib/r2.mjs             SigV4 signing for one PUT
lib/github.mjs         release lookup and asset upload
lib/instagram.mjs      Graph API calls, container polling, error-code translation
lib/facebook.mjs       Page API calls, error-code translation
lib/paths.mjs          where everything lives, atomic JSON writes
lib/args.mjs           --flag parsing
```

`data/` and `state/` are gitignored. Nothing in them belongs in the repository.

## Facebook

The same 2026 posts on the Page, through a second pair of steps:

```bash
node fb_token.mjs                       # once: user token -> permanent Page token
node build_fb_posts.mjs                 # posts.json -> data/fb_posts.json   <- review by hand
node upload_media.mjs --source fb       # the untouched originals
node publish_fb.mjs                     # dry run. --live actually posts
```

**Derived from posts.json, not from captions.json.** Those captions were bent into Instagram's
shape - links stripped, "Link in bio." appended, cards standing in for missing media, a hashtag set
glued on. Every one of those is wrong on Facebook, where links are clickable, text-only posts are
allowed, and hashtags do almost nothing. The three posts skipped for Instagram are included here:
they were link-only, which is exactly what Facebook handles best.

Media is uploaded `--source fb`, meaning untouched: no JPEG conversion and no aspect-ratio padding,
because Facebook needs neither.

### Backdating, and why it happens after publishing

Facebook can date a post to when it was written, so the Page ends up in chronological order rather
than as 67 posts stamped today. Backdated posts also stay out of followers' news feeds, which is
what you want from a backfill.

**Passing `backdated_time` when creating the post does not work, and fails differently per
endpoint.** `/videos` rejects it outright. `/photos` accepts the parameter and silently drops it -
the post goes out carrying today's date and the response says success. Believing that response
would have mis-dated every photo post without anyone noticing.

What works for all types is a second call: publish, then `POST /{page-id}_{post-id}` with
`backdated_time`. Two things make that fiddly, both measured rather than documented:

- The post id needs the **page prefix**. Without it the request reaches a deprecated endpoint and
  answers "singular statuses API is deprecated".
- A **video** is not queryable for about a minute after upload ("object does not exist"), and not
  editable while it transcodes. Both the post-id lookup and the dating retry in 15-second steps.

The date is read back after setting it, because "success" turned out not to be worth believing.

### Order of operations

State is written the moment a post exists - before the post id is resolved and before it is dated.
Anything after that is refinement of something already public: it warns and carries on rather than
ending the run. The first version wrote state at the end, and a failed lookup left a post on the
Page with no record of it, which is precisely the duplicate the state file exists to prevent.

### Getting the token

Facebook needs three hops where Instagram needed one, and `fb_token.mjs` does two of them. By hand:
a short-lived user token from the Graph API Explorer with `pages_show_list`, `pages_manage_posts`
and `pages_read_engagement`, extended once at
[the Access Token Debugger](https://developers.facebook.com/tools/debug/accesstoken). The script
trades that for a Page token, which - derived from a long-lived user token - does not expire.

It also checks two things that otherwise surface much later: whether the permissions were really
granted, and whether your role on the Page includes `CREATE_CONTENT`. Being an admin and being
allowed to post are not the same thing.

An app created through the Instagram use case has *Facebook Login for Business*, whose Explorer
variant wants a configuration rather than individual permissions. An app with classic Facebook
Login is the shorter path.

## YouTube

Prepared, not published. `build_yt_posts.mjs` writes the 34 video clips into `data/youtube/`,
named by date so dragging them all into YouTube Studio lands them in order, plus a
`metadata.json` carrying a title, description and tag list for each.

```bash
node build_yt_posts.mjs
```

**Why there is no uploader.** A video uploaded through an API project that has not passed Google's
compliance audit is locked private, and that lock cannot be appealed - the video has to be
re-uploaded by hand anyway. The rule applies to projects created after 28 July 2020, and the
Razarion cloud project (`neural-passkey-426618-j3`) dates from June 2024. The audit is possible but
takes weeks; uploading 34 clips by hand takes an hour and they are public immediately.

Quota, which used to be the other obstacle, no longer is: `videos.insert` cost 1600 units of a
10000 daily budget until December 2025, capping a project at six uploads a day. Since June 2026
uploads have their own allowance of about 100 a day.

**Shorts, honestly.** 33 of the 34 clips are landscape, and YouTube only treats a video as a Short
if it is square or portrait. They will be normal videos whatever route they take. One clip
(720x960) becomes a Short on its own. Making the rest into Shorts would mean re-cutting them
vertically - that is video editing, not backfilling.

## Writing a post here instead of on X

Everything above mirrors what X already carries. `compose.mjs` is the other direction - a clip or
an image plus a couple of sentences, turned into the three shapes the feeds want:

```bash
node compose.mjs --media data/clips/harvester.mp4 --text "Der Harvester sammelt jetzt..."
node compose.mjs --text "Nur Text" --link https://www.razarion.com
node compose.mjs --media shot.jpg --text "..." --tags "harvester,economy"
```

One input, three entries, all on `review`:

| | What it gets |
|---|---|
| **X** | Text with the link inline, checked against the 280 limit as X counts it - a link weighs 23 characters whatever its length |
| **Instagram** | Caption without the link, `Link in bio.` instead, hashtags underneath. No media means a card gets rendered |
| **Facebook** | Text with the link inline and clickable, nothing appended |

The id is `own-<timestamp>`, which cannot collide with an X post id. That matters because
`build_captions.mjs` and `build_fb_posts.mjs` rebuild their files from `posts.json`: both carry
over entries that are not in it, so a composed post survives every later sync instead of vanishing
at the next one.

Media is copied into `data/own/` rather than referenced. The review files point at paths that have
to still be there when publishing happens, which may be days later.

### Posting to X

`publish_x.mjs` reads `x_posts.json` and posts through the module in `../src/platforms/x.mjs`.
X takes the file itself rather than a URL, so the local copy is what it uses - the GitHub uploads
that Instagram and Facebook need are irrelevant on that side.

**It bills per post**: $0.015, or **$0.20 once a link is in the text**. The dry run prints the
estimate for the batch, the live run asks for confirmation of the total, and `state/posted_x.json`
is written after each post so a crash never leads to paying twice for the same text.

Writing needs an OAuth2 client with write scope - `X_CLIENT_ID` and `X_CLIENT_SECRET` in `.env`,
then `node ../src/cli.mjs auth x`. The `X_BEARER_TOKEN` used for reading cannot post.

## Keeping it in sync

`sync_new.mjs` runs the whole chain against whatever @razariongame has posted since the last run.
It resumes from `state/sync.json`, falling back to the newest entry in `captions.json`.

```bash
node sync_new.mjs                    # fetch, prepare both networks - then stop
node sync_new.mjs --auto             # ...and mark unflagged new posts as ok
node sync_new.mjs --auto --publish   # ...and publish them
node sync_new.mjs --only fb          # one network instead of both
```

**Publishing is off by default, and so is `--auto`.** A job that posts whatever it finds, unread,
is how an account ends up publishing a link-only post that says nothing on Instagram. `--auto`
promotes only new entries that carry **no flags at all**; anything flagged waits for a person.

It resumes from the newest post it has *seen*, not the newest it kept. A run that finds only replies
to other accounts still moves the mark forward - otherwise every future run re-reads the same
replies, and reads are billed.

A run with nothing new costs nothing - no posts read, no files uploaded, and it stops before the
rest of the chain.

### On a schedule

`scheduled/run.ps1` is what a scheduled task should call. It checks the Instagram token, syncs, and
then publishes from the **approved** backlog - one post per network per run by default:

```
schtasks /create /tn "razarion-social" /sc weekly /d MON,WED,FRI /st 10:00 ^
  /tr "powershell -NoProfile -ExecutionPolicy Bypass -File C:\dev\projects\razarion\code\razarion2\razarion-social\pipeline\scheduled\run.ps1"
```

Three runs a week, one post each, matches the cadence the editorial plan assumes. Everything lands
in `state/scheduled.log`.

The division of labour is deliberate: the task fetches, prepares and delivers; a person decides once
a week what gets approved. Nothing reaches a feed that was not set to `ok` by hand, and a run with
an empty approved backlog is a no-op rather than an improvisation.

A failing step does not stop the ones after it - if Instagram is throttled, Facebook still delivers,
and the token check runs regardless of both.

## The Instagram token expires

Long-lived tokens last 60 days. `refresh_token.mjs` exchanges the current one for a fresh 60-day
token and writes it to `state/instagram-token.json`, which takes precedence over `.env` while it is
valid - `.env` is never rewritten.

```bash
node refresh_token.mjs           # refreshes if fewer than 14 days are left
node refresh_token.mjs --force
```

Run it from the same schedule as the sync. A token can only be refreshed **while it is still
valid**: let it lapse and the only way back is generating a new one in the Meta dashboard by hand.
