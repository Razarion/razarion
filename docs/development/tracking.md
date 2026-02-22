# Page Request Tracking

## Overview

`RequestInfoLoggingFilter` tracks page visits with campaign parameters. It captures landing page and game page requests and forwards them to `PageRequestService`.

## Tracked Endpoints

| Path | Method | Description |
|------|--------|-------------|
| `/t.gif` | `onHome()` | Landing page tracking pixel (loaded as invisible 1x1 image) |
| `/game` or `/game/index.html` | `onGame()` | Game page visit |

## Query Parameters

| Parameter | Description |
|-----------|-------------|
| `utm_campaign` | Campaign name |
| `utm_source` | Traffic source |
| `rdt_cid` | Reddit click ID |

A session ID is also captured automatically via `HttpSession`.

## Test URL

```
http://localhost:8080/?utm_campaign=test_campaign&utm_source=test_source&rdt_cid=0123456789
```

## How It Works

1. The landing page (`index.ftl`) appends query parameters to a hidden tracking pixel:
   ```html
   <img src="/t.gif${qs}" width="1" height="1" alt="" style="position:absolute;opacity:0">
   ```
2. When the user clicks "Play Now", query parameters are forwarded to `/game`.
3. `RequestInfoLoggingFilter` intercepts both requests and calls `PageRequestService`.

## Key Files

- `razarion-server/.../web/RequestInfoLoggingFilter.java` - Servlet filter
- `razarion-server/.../service/tracking/PageRequestService.java` - Tracking service
- `razarion-server/src/main/resources/templates/index.ftl` - Landing page template
