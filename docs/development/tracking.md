# Page Request Tracking

## Overview

`RequestInfoLoggingFilter` tracks page visits with campaign parameters. It captures landing page and game page requests and forwards them to `PageRequestService`.

## Tracked Endpoints

| Path | Method | Description |
|------|--------|-------------|
| `/t.gif` | `onHome()` | Landing page tracking pixel (loaded as invisible 1x1 image) |
| `/game` or `/game/index.html` | `onGame()` | Game page visit |

## Query Parameters

| Parameter | Stored field | Description |
|-----------|--------------|-------------|
| `utm_campaign` | `utmCampaign` | Campaign name |
| `utm_source` | `utmSource` | Traffic source |
| `utm_medium` | `utmMedium` | Campaign medium |
| `rdt_cid` | `rdtCid` | Reddit click ID |
| `twclid` | `twclid` | X (Twitter) click ID |
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
4. `RequestInfoLoggingFilter` intercepts `/t.gif` and `/game`, extracts the named parameters
   plus the raw query string, and stores a `PageRequest` document via `PageRequestService`.

## Key Files

- `razarion-server/.../web/IndexController.java` - Forwards the raw query string to the landing page
- `razarion-server/.../web/RequestInfoLoggingFilter.java` - Servlet filter
- `razarion-server/.../service/tracking/PageRequestService.java` - Tracking service
- `razarion-server/.../model/tracking/PageRequest.java` - MongoDB document (`page_request` collection)
- `razarion-server/src/main/resources/templates/index.ftl` - Landing page template

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
