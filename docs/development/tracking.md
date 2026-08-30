# Page Request Tracking

## Overview

`RequestInfoLoggingFilter` tracks page visits with campaign parameters. It captures landing page and game page requests and forwards them to `PageRequestService`.

## Tracked Endpoints

| Path | Method | Description |
|------|--------|-------------|
| `/` | `onLanding()` | The landing page itself — recorded for its `Referer`, see *Where a visitor came from* |
| `/t.gif` | `onHomeEvent()` | Landing page pixel and beacons — see *Landing page events* below |
| `/game` or `/game/index.html` | `onGame()` | Game page visit |

## Where a visitor came from

`LANDING` is the only record that holds the origin, and the reason is worth stating once: the pixel
is a subresource of the landing page, so its `Referer` is that page, and "Play Now" is a navigation
to `/game`, so from there on `document.referrer` is that page too. Everything except the landing
page request itself answers `razarion.com` — which is what the history reported for 130 of 139
sessions before this existed, for visitors who had all come from X.

It is also the only record an organic visitor produces at all: the pixel and the beacons need a
query string, and someone arriving without campaign parameters has none.

Unlike the pixel it is recorded whether or not campaign parameters are present, so it must never be
counted as a funnel step — it describes a wider population than `HOME` does. `DailyProgressService`
skips it for exactly that reason.

That is also why the backend funnel's table has two halves. Everything above `Game (total)` rests
on the pixel; from there down it also holds the visitors who arrived over a plain link and fired
none. `Game (from Home)` counts the ones in both, so the landing page's own conversion is measured
on a single population and no row below is a share of a number it could never reach. The *All*
view's landing count is shown as context, never as a percentage base.

A visitor who arrives without parameters is invisible above that line twice over: no pixel, and no
`GAME` record either, since `/game` is only recorded when it carries a query string. Their game
visit is known from the startup records alone — which is why the funnel reads those too.

Both tables can be split by device. The funnel classifies in the browser (`classifyDevice()`, shared
with the Controls tab), *Daily* on the server (`TrackingDevice.of()`); the two are kept in step so
they answer the same question the same way. `UNKNOWN` is a value of its own rather than a bucket
folded into the desktops: it means the records carry no user agent at all.

### The Facebook app fetching for itself

A request whose user agent is the app's own token with no browser string in front of it —

```
[FBAN/FB4A;FBAV/575.1.0.55.73;FBDM/{density=2.8125,width=1080,height=2340};FBLC/en_GB;]
```

— is the Facebook app warming a link, not a browser showing the page to anybody. Over seven days of
PROD, 952 of these produced **no play click, no exit event, no game page and no client start**, and
not one was ever reported visible. A real in-app browser is the opposite case and must be kept: it
sends the whole Mozilla string and *appends* the token. What separates them is whether the token
stands alone.

Two things follow, and they are separate:

- **Device.** The bare token contains none of the words the classification looks for, so 937 of
  those 952 counted as desktops — while `FBAN/FB4A` is Facebook for Android and `FBDM` states a
  1080×2340 screen. Both classifiers now read the app token. Measured effect on seven days: the
  desktop landing conversion went from 3.65% to **16.54%**, because the bucket had been four fifths
  machine.
- **Denominator.** `TrackingDevice.isAppFetch()` (server) and `isAppFetch()` (frontend) keep these
  rows out of the funnel — 15% of all `HOME` records. They are kept in the collection, because how
  much of this there is, is worth reading; they are simply not a population any rate is measured
  against.

A caveat worth stating, because it decides which past numbers were wrong: the app prefetches the
link with **the same click id** the person then arrives with. So a rate counted per click id was
never inflated — the prefetch collapses into the same visitor. Rates counted per http session, and
every device split, were.

*Daily* answers the platform question with the same three steps
(`TrackingPlatforms`, shared with the history), counts a game visit from the startup records as the
funnel does, and reports as many days back as the table asks for — 10 by default, 90 at most.

Both tables count **visitors**, not http sessions (`VisitorGroups` on the server,
`HandleGroups` in `tracking-container-analyzer.ts`). Records are the same visitor when they share a
click id, a game session or an http session, chains included. A browser that keeps no cookies is
handed a fresh session per request, so one boot arrives as a dozen sessions of one beacon each —
counted per session it reads as a dozen people who all stopped at the first step.

`PlayerSessionService.origin()` reads it, and ignores any referrer pointing at razarion.com itself:
a page of ours is the step before, not an origin. The client's own `document.referrer` is kept as
the fallback for someone who opened `/game` directly, where it is the real thing.

## Landing page events

Four out of five landing page visits never reach the game, and the visit alone does not say why.
All three signals arrive through the same `/t.gif` URL and are told apart by the `e` parameter,
so they always carry the same campaign parameters and the same http session as the visit itself.

| `e` | `PageRequestType` | Sent when |
|-----|-------------------|-----------|
| *(absent)* | `HOME` | the page rendered and the `<img>` pixel loaded |
| `play` | `HOME_PLAY_CLICKED` | "Play Now" was pressed |
| `exit` | `HOME_EXIT` | the page was left; `d` carries the milliseconds it was open, `p=1` that play had been pressed |

`HOME_PLAY_CLICKED` is what splits the loss in two: everyone missing above it was not convinced by
the page, everyone missing between it and `GAME` wanted to play and did not get there. Those two
call for opposite fixes.

The events are sent with `navigator.sendBeacon`, which POSTs — `TrackingPixelController` answers
that POST so the static resource handler (GET only) does not reject it. The recording itself stays
in `RequestInfoLoggingFilter` for both methods. The beacons only run when the landing page carries
a query string, exactly like the pixel, so all the counts describe the same population.

### What the exit event carries

An exit that says only "they left after nine seconds" cannot be acted on. These six say what became
of the call to action, and they ride on the same pixel URL, so they cost no extra request. All of
them are attacker-controlled, so an implausible value is dropped rather than stored — a single
crafted visit must not be able to move a median or invent a category.

| Parameter | Stored field | What it answers |
|-----------|--------------|-----------------|
| `bs` | `buttonSeenMillis` | when the Play button was first half on screen; absent means it never was |
| `bp` | `buttonPressed` | a finger landed on the button. Absent on an exit means none did — a real statement, not a missing measurement |
| `sd` | `scrollDepth` | how far down they got, in percent |
| `vp` | `viewport` | the viewport on arrival; the height an app's own browser leaves over is not in the user agent |
| `tf` | `tapFailure` | why the last touch on the button did not become a game — see below |
| `tm` | `tapFailureMeasure` | pixels for `d`, milliseconds for `c`, `h` and `o` |

`bp` without a `HOME_PLAY_CLICKED` of its own is a touch that went nowhere. That happens often
enough on these visitors to be the largest single loss on the page, and the four ways it happens
call for four different repairs, which is what `tf` separates:

| `tf` | The gesture | What it would mean |
|------|-------------|--------------------|
| `c` | the browser took it away before the finger came up | the webview claimed it for a scroll or a swipe; cancelled within ~50 ms is a scroll starting under a finger that never meant to press |
| `d` | the finger travelled further than `TAP_SLOP` | with `tm` near the limit the limit is too tight; with `tm` large it was a swipe, not a tap |
| `h` | it rested longer than `TAP_MILLIS` | a long press, or a page too busy to respond |
| `o` | it never came up at all | they left mid-press, or the event never arrived |

A touch that reaches the game clears `tf` again, however it got there — a gesture the pointer
handlers had given up on before a native `click` arrived is not a failure.

`userAgent` and `referer` are stored for every page request. Without them a visit that never
continues cannot be told apart from a crawler or an ad network's click verification, both of which
fetch the pixel just like a browser.

## Query Parameters

| Parameter | Stored field | Description |
|-----------|--------------|-------------|
| `utm_campaign` | `utmCampaign` | Campaign name |
| `utm_source` | `utmSource` | Traffic source |
| `utm_medium` | `utmMedium` | Campaign medium |
| `rdt_cid` | `rdtCid` | Reddit click ID |
| `twclid` | `twclid` | X (Twitter) click ID |
| `fbclid` | `fbclid` | Meta (Facebook/Instagram) click ID |
| `e` | *(picks the `PageRequestType`)* | Landing page event, see below |
| `d` | `dwellMillis` | Milliseconds the landing page was open, on the exit event only |
| *(everything)* | `rawQueryString` | The complete raw query string, so any additional/unknown parameter is preserved |

A session ID is also captured automatically via `HttpSession`.

## Test URLs

Reddit:
```
http://localhost:8080/?utm_campaign=test_campaign&utm_source=reddit&rdt_cid=0123456789
```

X (Twitter):
```
http://localhost:8080/?utm_campaign=x_launch&utm_source=x&utm_medium=cpc&twclid=abc123
```

## How It Works

1. `IndexController` (`/`) forwards the **complete raw query string** (sanitized) into the
   landing page as `${qs}`, so any campaign parameter is preserved — not just a whitelist.
2. The landing page (`index.ftl`) appends `${qs}` to a hidden tracking pixel:
   ```html
   <img src="/t.gif${qs}" width="1" height="1" alt="" style="position:absolute;opacity:0">
   ```
3. When the user clicks "Play Now", the query string is forwarded to `/game`.
4. `RequestInfoLoggingFilter` intercepts `/`, `/t.gif` and `/game`, extracts the named parameters
   plus the raw query string, and stores a `PageRequest` document via `PageRequestService`.
   `/` is recorded only when it carries a `Referer` or a query string — it is what every crawler
   asks for first, and without either there is nothing to learn from it.

## Key Files

- `razarion-server/.../web/IndexController.java` - Forwards the raw query string to the landing page
- `razarion-server/.../web/RequestInfoLoggingFilter.java` - Servlet filter
- `razarion-server/.../service/tracking/PageRequestService.java` - Tracking service
- `razarion-server/.../model/tracking/PageRequest.java` - MongoDB document (`page_request` collection)
- `razarion-server/src/main/resources/templates/index.ftl` - Landing page template

# First Interaction — the Controls tab

`first_interaction` records the first time a player used each control in a game session
(`POINTER_DOWN`, `PLACER_SHOWN`, `PLACER_REJECTED`, `PLACER_CONFIRMED`, `CAMERA_PAN_TOUCH`,
`CAMERA_PINCH`, `CAMERA_KEYBOARD`, `CAMERA_WHEEL`, `SELECT`, `COMMAND`). Reported once per session and kind by `FirstInteractionTrackerService`,
stored by `FirstInteractionService` with a 180-day TTL — the only tracking collection that
expires. `kind` is a free string on the server, so a new one needs no server change.

### Three classes of kind

Every share on the Controls tab depends on keeping these apart:

| Class | Kinds | Meaning |
|---|---|---|
| not the player | `PLACER_SHOWN` | the *game* did something — it asked for a base |
| reached, no result | `POINTER_DOWN`, `PLACER_REJECTED` | the player reached and got nothing |
| achievement | everything else | it worked |

A session in which only `PLACER_SHOWN` happened is a session in which the player did nothing, and
still reads as **silent**. Getting that wrong would send the one number this view was built for to
zero on the day the event shipped.

**Why the placer events exist.** Placing the starting base is the first thing the game asks of
anybody. Measured over 14 days on PROD: of 338 users in the History tab, 160 never built a base —
95 of them had a running game and did nothing, and **63 of those 95 had reported no interaction of
any kind**. That number cannot say whether they were shown the placer and ignored it, never got
one, or tapped a spot that was refused. Those are three different repairs, and these three events
separate them.

**`POINTER_DOWN` is the only kind that is not an outcome.** Every other kind records something
that worked — a camera that moved, a unit that was picked, an order that went out — so a finger
that touches the screen and gets no answer leaves no trace at all. Seven days of PROD showed the
paid mobile cohort at 37 sessions with a running game, one interaction between them and no base;
that number cannot tell *never reached for it* from *reached for it and the game did not
respond*, and those are opposite repairs. It is reported for a mouse as well as a finger: a kind
that exists on only one device cannot be checked against the other.

**Absence is the signal.** The funnel counts how many stop before the first quest but cannot say
whether they could steer at all. A session that never reports `CAMERA_PAN_TOUCH` never found the
gesture. Every share is therefore measured against the sessions that reached `RUN_GAME`, not
against the sessions that reported something — measuring the reporters against themselves hides
exactly the players the collection exists to find.

The **Controls** tab in the backend view shows, per device class:

- one row per control: sessions, share of the running games, median time to first use;
- a nested funnel: game running → **touched the field** → **got a reaction** → selected → gave
  an order → passed a quest. The first two steps used to be one. `touchedWithoutEffect` — the
  gap between them — is a player who reached for the game and got nothing, and it is shown next
  to `silent`, who never reached at all.

## Why the funnel nests and why the camera is not a step

Read as independent shares, the rows produce percentages over 100% against real data: more sessions
select than move the camera (tapping a unit needs no panning), and more users pass a quest than were
seen giving an order (a quest is recorded per user and needs no interaction record). Each step
therefore counts only the sessions of the step above it. The unconditional counts stay in the
per-control table, so nesting loses nothing.

"Moved the camera" is deliberately not a step: nothing later requires it, and whether the gesture
was found is what the per-control table already answers.

## Device classification

Taken from the `userAgent` on the `PAGE_LOADED` startup task, joined by `gameSessionUuid` — which is
why the record carries the session and not only a user. Tablet is tested before mobile (an Android
tablet carries `Android` too). Samsung and the desktop-site setting send a desktop userAgent from a
phone, so Mobile is undercounted; the tab reports how many desktop-userAgent sessions produced a
touch gesture rather than silently reclassifying them, because the correction would be a guess.

## Key files

- `razarion-frontend/.../game/tracking/first-interaction-tracker.service.ts` — reports, once per kind
- `razarion-server/.../service/tracking/FirstInteractionService.java` — stores (`first_interaction`)
- `razarion-frontend/.../backend/tracking-container/first-interaction-analyzer.ts` — the analysis
- `razarion-frontend/.../backend/first-interaction/` — the Controls tab

# Ad-Network Conversion Tracking (Server-Side)

Beyond storing page requests, the server sends **server-to-server conversion events** to ad
networks so campaigns can optimise and attribute. Three networks are wired up, all following the
same funnel:

| Funnel event | Fired from | Reddit | X (Twitter) | Meta |
|--------------|-----------|--------|-------------|------|
| Landing view | `RequestInfoLoggingFilter` (`/t.gif` page view only) | — | — | `GameLandingView` |
| Page visit | `RequestInfoLoggingFilter` (`/game` with query string) | `GamePageVisit` | ✓ | ✓ |
| Client startup | `ClientGameConnectionService.afterConnectionEstablished` (WebSocket connect) | `GameClientStartup` | ✓ | ✓ |
| Builder deployed | `ServerGameEngineControl.onBaseCreated` (first base) | `GameBuilderDeployed` | ✓ | ✓ |
| Quest passed | `ServerLevelQuestService.onQuestPassed` | `GameQuestPassed_level{n}_Quest{id}` | ✓ (`GameQuestPassed`, detail in `description`) | ✓ (detail in `custom_data.description`) |
| Level up | `ServerLevelQuestService.onQuestPassed` | `GameLevelUp_level{n}` | ✓ (`GameLevelUp`, detail in `description`) | ✓ (detail in `custom_data.description`) |

All three services are **fire-and-forget** (`@Async`, failures only logged) and fall back to a
**MOCK mode** (log, don't send) when their credentials are not configured.

## Click-ID persistence — IN-MEMORY ONLY (GDPR-conservative)

The click ids (`rdt_cid` for Reddit, `twclid` for X, `fbclid` for Meta) are **never persisted on
the user record**.
Each service keeps a `userId -> clickId` `ConcurrentHashMap`, populated on WebSocket connect
(looked up from the latest `page_request` for the http session) and cleared on disconnect.
Consequence: late-firing events in a *new* session (no fresh click id in the URL) are dropped and
not attributed. Accepted limitation pending a GDPR review before persisting.

## Reddit — `RedditConversionService`

- Endpoint: `POST https://ads-api.reddit.com/api/v3/pixels/{pixelId}/conversion_events`
- Auth: **Bearer token**
- Config: `reddit.ads.pixel-id`, `reddit.ads.access-token` (env `REDDIT_ADS_*`, k8s secret `reddit-ads-secrets`)
- Each event uses `tracking_type: CUSTOM` with a freeform `custom_event_name` + `click_id`.

## X (Twitter) — `XConversionService`

- Endpoint: `POST https://ads-api.x.com/{version}/measurement/conversions/{pixelId}`
- Auth: a single **`X-Pixel-Token`** header. The token is generated once in the X Ads Events
  Manager under *Manual → Conversion API → Generate access token* (not OAuth 1.0a — that older
  Ads-API flow is not needed for this endpoint).
- Config (env `X_ADS_*`, k8s secret `x-ads-secrets`, all `optional: true`):
  - `x.ads.pixel-id`, `x.ads.pixel-token`, `x.ads.api-version` (default `12`)
  - `x.ads.event.{page-visit,client-startup,builder-deployed,quest-passed,level-up}` — the
    **event-tag ids** created in the X Ads Events Manager (e.g. `tw-o8z6j-o8z21`). Unlike Reddit,
    X requires each conversion event to be pre-created there; the request references it by
    `event_id`. When an event id is left empty the funnel-step name is used instead (works for
    MOCK logging). The dynamic quest/level detail is carried in the `description` field.
- Request body: `{ "conversions": [ { conversion_time, event_id, identifiers: [{twclid}],
  conversion_id, description? } ] }`

## Meta (Facebook and Instagram) — `MetaConversionService`

- Endpoint: `POST https://graph.facebook.com/{version}/{pixelId}/events`
- Auth: a **system-user access token** from Events Manager → *Settings → Conversions API →
  Generate access token*, passed as the `access_token` query parameter (the url is never logged).
- **The click id is not sent as itself.** Meta matches on `fbc`, the value its browser pixel would
  have written into a cookie: `fb.1.{millis when the click was seen}.{fbclid}`. It is built once,
  when the click id is first seen, and kept with the user — rebuilding it at event time would
  stamp a quest passed an hour later with the wrong click time. A wrong format here fails
  silently: the events are accepted and matched to nobody.
- Config (env `META_ADS_*`, k8s secret `meta-ads-secrets`, all `optional: true`):
  - `meta.ads.pixel-id`, `meta.ads.access-token`, `meta.ads.api-version` (default `v21.0`)
  - `meta.ads.test-event-code` — set while verifying in Events Manager → *Test events*; events
    then arrive there instead of counting. Leave empty in production.
  - `meta.ads.event.{landing-view,page-visit,client-startup,builder-deployed,quest-passed,level-up}` — the
    **event name** per funnel step. Empty means the custom name in the table above, which shows up
    as a custom event. Point a step at a standard event name (e.g. `CompleteRegistration` for
    builder-deployed) to let campaigns optimise towards it without a deploy.
- **Meta gets one step the other two do not: the landing view.** Its optimiser needs roughly fifty
  conversions a week of an event before it stops guessing, and the step below — the game page — is
  reached by barely one Meta visitor in a hundred: four a day against three hundred landing views.
  It is fired from the page's own pixel rather than the document request, so Meta's link crawler,
  which never runs the script and made up nine of ten requests on the first campaign day, is not
  counted as a visitor. Only the page view fires it; the play click and the exit ride on the same
  pixel url and would report one visitor three times.
- **The browser goes with every event.** Meta counts `client_user_agent` as a required parameter
  for a website event. It is only known on the request that brought the visitor, so it is stored
  next to the `fbc` and replayed on the later funnel steps — by the time a base is built there is
  no request left to read it from. `client_ip_address` is deliberately **not** sent: no IP address
  is stored anywhere in this system, and that stands.
- Request body: `{ "data": [ { event_name, event_time (seconds), action_source: "website",
  event_id, user_data: { fbc, client_user_agent }, custom_data: { description }? } ],
  test_event_code? }`

### Conversion-API key files

- `razarion-server/.../service/tracking/RedditConversionService.java`
- `razarion-server/.../service/tracking/XConversionService.java`
- `razarion-server/.../service/tracking/MetaConversionService.java`
- `razarion-server/.../service/tracking/PageRequestService.java` — `findRdtCidByHttpSessionId`, `findTwclidByHttpSessionId`, `findFbclidByHttpSessionId`
