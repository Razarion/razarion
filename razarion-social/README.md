# razarion-social

Razarion posts to four networks: YouTube, X, Instagram and Facebook. The normal way there is the
pipeline in [`pipeline/`](pipeline/README.md): it plans, writes and films posts, puts them up for
review, and publishes what was approved to all four, started by `pipeline/Razarion-Social.cmd`.

What this README describes is the older, smaller tool underneath it: one gameplay clip, one JSON
file, published by hand to YouTube, X and Instagram. It has no Facebook support, since Facebook
only ever went through the pipeline. The pipeline reuses its YouTube uploader, OAuth, HTTP layer
and logging. No npm dependencies — Node 20+ only.

```bash
cd razarion-social
cp .env.example .env          # fill in as you obtain each credential
node src/cli.mjs check        # what is configured, what is still missing
node src/cli.mjs auth google  # once per platform
node src/cli.mjs post posts/my-clip.json           # dry run: validates, posts nothing
node src/cli.mjs post posts/my-clip.json --live    # actually publishes
```

`post` is a dry run unless you pass `--live`. Posting is not undoable and X bills per post, so
the safe path is the default one.

## What each platform actually does

| Platform | Result of `--live` | Still manual |
|---|---|---|
| YouTube | video + title, description, tags, thumbnail uploaded as **private** | flip to public in Studio |
| X | posted immediately, optional thread | nothing |
| Instagram | Reel published | nothing — but see the blocker below |

The manual last step on YouTube is deliberate: only an audited API project may upload public
videos, and the audit was requested on 2026-09-27 (see `YOUTUBE-AUDIT.md`). Until it passes, each
upload costs one click in Studio.

TikTok is not supported. Its guidelines rule out "a utility tool to help upload contents to the
account(s) you or your team manages", which is exactly what this is, so an app for it would not
pass review.

## Credentials

### YouTube
1. [console.cloud.google.com](https://console.cloud.google.com) → new project.
2. APIs & Services → Library → enable **YouTube Data API v3**.
3. OAuth consent screen: External, add your own Google account under *Test users*.
4. Credentials → Create → OAuth client ID → **Desktop app**.
5. Client ID and secret into `.env`, then `node src/cli.mjs auth google`.

Quota is not the constraint (`videos.insert` has its own daily bucket of roughly 100 calls). The
constraint is the compliance audit: an unaudited client can only create **private** videos, which
is exactly why `privacy` defaults to `private` in the post spec.

### X
1. [developer.x.com](https://developer.x.com) → project → app.
2. User authentication settings: OAuth 2.0, **Confidential client**, app permissions
   *Read and write*, callback URL `http://127.0.0.1:8724`.
3. Client ID and secret into `.env`, then `node src/cli.mjs auth x`.

**This costs money.** X removed the free tier in February 2026; new developers are on pay-per-use
at roughly **$0.015 per post, or $0.20 if the post contains a link**. Almost every Razarion post
carries a link, so budget ~$0.20 each. The CLI prints the estimate before it spends anything.

### Instagram — the one real blocker
Needs all of:
- an Instagram **Business** account (Creator accounts cannot publish via the API) linked to a
  Facebook Page,
- a Meta app with `instagram_business_basic` + `instagram_business_content_publish` approved
  through **app review**,
- the video hosted at a **publicly reachable URL** — Instagram fetches the file itself and never
  accepts an upload. Hence `instagram.videoUrl` in the spec rather than a local path.

Put the long-lived token and IG user id in `.env`. The token expires after 60 days; refresh it
from the Meta dashboard before then. The limit is 100 API-published posts per 24 hours (a moving
window, shared with anything posted from the phone); `GET /{ig-user-id}/content_publishing_limit`
reports what is left.

## The post spec

See [`posts/example.json`](posts/example.json). Paths are resolved relative to the spec file.
Omit a platform's section to skip that platform; `--only youtube,x` narrows it further.

Caption limits are checked before anything uploads, because discovering that the title was two
characters too long after a 300 MB upload is a bad trade.

## Files

```
src/cli.mjs            commands: check, auth, post
src/spec.mjs           post spec loading + validation
src/config.mjs         .env parsing, refresh-token store
src/auth/              one OAuth flow per platform, shared loopback server
src/platforms/         one module per platform
```

`.env` and `.tokens.json` are gitignored. Neither belongs in the repository.
