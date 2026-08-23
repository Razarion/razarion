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
(`CAMERA_PAN_TOUCH`, `CAMERA_PINCH`, `CAMERA_KEYBOARD`, `CAMERA_WHEEL`, `SELECT`, `COMMAND`).
Reported once per session and kind by `FirstInteractionTrackerService`, stored by
`FirstInteractionService` with a 180-day TTL — the only tracking collection that expires.

**Absence is the signal.** The funnel counts how many stop before the first quest but cannot say
whether they could steer at all. A session that never reports `CAMERA_PAN_TOUCH` never found the
gesture. Every share is therefore measured against the sessions that reached `RUN_GAME`, not
against the sessions that reported something — measuring the reporters against themselves hides
exactly the players the collection exists to find.

The **Controls** tab in the backend view shows, per device class:

- one row per control: sessions, share of the running games, median time to first use;
- a nested funnel: game running → touched anything → selected → gave an order → passed a quest.

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
networks so campaigns can optimise and attribute. Two networks are wired up, both following the
same funnel:

| Funnel event | Fired from | Reddit | X (Twitter) |
|--------------|-----------|--------|-------------|
| Page visit | `RequestInfoLoggingFilter` (`/game` with query string) | `GamePageVisit` | ✓ |
| Client startup | `ClientGameConnectionService.afterConnectionEstablished` (WebSocket connect) | `GameClientStartup` | ✓ |
| Builder deployed | `ServerGameEngineControl.onBaseCreated` (first base) | `GameBuilderDeployed` | ✓ |
| Quest passed | `ServerLevelQuestService.onQuestPassed` | `GameQuestPassed_level{n}_Quest{id}` | ✓ (`GameQuestPassed`, detail in `description`) |
| Level up | `ServerLevelQuestService.onQuestPassed` | `GameLevelUp_level{n}` | ✓ (`GameLevelUp`, detail in `description`) |

Both services are **fire-and-forget** (`@Async`, failures only logged) and fall back to a **MOCK
mode** (log, don't send) when their credentials are not configured.

## Click-ID persistence — IN-MEMORY ONLY (GDPR-conservative)

The click ids (`rdt_cid` for Reddit, `twclid` for X) are **never persisted on the user record**.
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

### Conversion-API key files

- `razarion-server/.../service/tracking/RedditConversionService.java`
- `razarion-server/.../service/tracking/XConversionService.java`
- `razarion-server/.../service/tracking/PageRequestService.java` — `findRdtCidByHttpSessionId`, `findTwclidByHttpSessionId`
