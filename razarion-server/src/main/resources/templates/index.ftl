<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Razarion – RTS meets MMO: One World That Never Stops</title>
    <base href="/">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="RTS meets MMO: one shared world that never stops. Inspired by Command &amp; Conquer and StarCraft — persistent shared world, quests and levels. Play free in your browser via WebAssembly, no download. Open-source and community-driven.">
    <link rel="icon" type="image/x-icon" href="/favicon.ico">
    <!-- The panel frame used to come from augmented-ui on a public CDN. A stylesheet in the head
         blocks the first paint, and a third-party one costs a DNS lookup, a TCP connection and a
         TLS handshake before the browser is allowed to draw anything at all. Most visitors arrive
         from a link inside an app, where that cache is cold every single time - and half of them
         are gone in under two seconds. The frame is drawn in the inline style below instead: no
         request, painted with the first frame. -->
    <!-- One hero per shape, and each viewport fetches only its own. The two media queries are exact
         complements - "not all and (max-aspect-ratio: 3/4)" rather than a min- counterpart, which
         would overlap at exactly 3/4 and make a portrait tablet download both. -->
    <link rel="preload" as="image" href="/razarion-bg.webp" fetchpriority="high"
          media="not all and (max-aspect-ratio: 3/4)">
    <link rel="preload" as="image" href="/razarion-bg-portrait.webp" fetchpriority="high"
          media="(max-aspect-ratio: 3/4)">

    <!-- Open Graph Tags -->
    <meta property="og:title" content="Razarion – RTS meets MMO: One World That Never Stops">
    <meta property="og:description" content="RTS meets MMO: one shared world that never stops. Inspired by Command &amp; Conquer and StarCraft — persistent shared world, quests and levels. Play free in your browser via WebAssembly, no download. Open-source and community-driven.">
    <meta property="og:image" content="https://razarion.com/card.jpg">
    <meta property="og:url" content="https://razarion.com">
    <meta property="og:type" content="website">
    <meta property="og:site_name" content="Razarion">

    <!-- Twitter Card Tags -->
    <meta name="twitter:card" content="summary_large_image">
    <meta name="twitter:site" content="@razariongame">
    <meta name="twitter:title" content="Razarion – RTS meets MMO: One World That Never Stops">
    <meta name="twitter:description" content="RTS meets MMO: one shared world that never stops. Inspired by Command &amp; Conquer and StarCraft — persistent shared world, quests and levels. Play free in your browser via WebAssembly, no download. Open-source and community-driven.">
    <meta name="twitter:image" content="https://razarion.com/card.jpg">

    <style>
        *{margin:0;padding:0;box-sizing:border-box}
        html,body{height:100%}
        body{font-family:system-ui,-apple-system,'Segoe UI',Roboto,sans-serif;background:#0a0a12;color:#e0e0e0;line-height:1.6}

        /* The cockpit's own tokens, copied from _hud.scss. The landing page cannot import that
           stylesheet - it ships one inline <style> on purpose, see the note about the CDN above -
           so the panel below repeats the recipe instead. Keep these in sync with the :root block
           in razarion-frontend/src/app/game/cockpit/_hud.scss. */
        :root{
            --hud-text:#cfe0f0;
            --hud-text-dim:#8ba3ba;
            --hud-cyan:#63c8ff;
            --hud-font:'Segoe UI',system-ui,-apple-system,sans-serif
        }

        /* svh is the viewport with the browser's own bars showing, which is what a phone actually
           displays when the page opens - vh is the taller one it only reaches after a scroll. The
           page used to be exactly 100vh with overflow hidden, so on a phone the lower part of the
           panel sat behind the address bar with no way to scroll to it. min-height rather than
           height, and no overflow rule, so nothing here can ever be out of reach. */
        /* Off screen, read aloud. The portrait rules below give it a body. */
        .brand{position:absolute;width:1px;height:1px;padding:0;margin:-1px;overflow:hidden;clip:rect(0,0,0,0);border:0}

        .landing{position:relative;--hero:url('/razarion-bg.webp');min-height:100vh;min-height:100svh;background:linear-gradient(180deg,rgba(10,10,18,0) 0%,rgba(10,10,18,0) 35%,rgba(10,10,18,0.55) 65%,rgba(10,10,18,0.88) 100%),var(--hero) center/cover no-repeat;display:flex;flex-direction:column;align-items:center;justify-content:flex-end;text-align:center;padding:2rem 2rem 18vh}


        @keyframes chevPulse{0%,100%{color:#f73;text-shadow:0 0 6px rgba(255,140,60,0.7)}50%{color:#fc6;text-shadow:0 0 14px rgba(255,180,80,1),0 0 4px #fff}}
        @keyframes titleShimmer{0%,100%{text-shadow:0 0 14px rgba(255,140,60,0.9),0 0 4px rgba(255,255,255,0.4)}50%{text-shadow:0 0 22px rgba(255,180,80,1),0 0 8px rgba(255,255,255,0.7),0 0 40px rgba(255,140,60,0.6)}}
        @keyframes btnShine{0%{background-position:-200% 0}100%{background-position:200% 0}}

        /* Panel frame - the same plate the cockpit is built from, so the page a visitor sees before
           the click and the instrument panel they see after it are the same object. This is the
           @mixin hud-plate from _hud-panels.scss, one element instead of the nested .hud-frame /
           .hud-frame-inner pair: a transparent 4px border filled by the steel gradient clipped to
           the border box, over the dark well clipped to the padding box. The content padding is
           part of the well, so it simply makes the well bigger. */
        .info-panel{
            padding:2rem 2.5rem;
            border:4px solid transparent;
            border-radius:12px;
            display:flex;
            flex-direction:column;
            align-items:center;
            max-width:640px;
            background-image:
                radial-gradient(120% 180% at 50% 0%,rgba(46,100,148,0.32) 0%,rgba(8,16,27,0) 62%),
                linear-gradient(180deg,#0e1d2f 0%,#081422 55%,#050d17 100%),
                linear-gradient(180deg,
                    #d3dde7 0%,#9aa8b7 5%,#64717f 22%,
                    #3b4551 52%,#242c36 78%,#6f7b89 96%,#b3bfcc 100%);
            background-origin:padding-box,padding-box,border-box;
            background-clip:padding-box,padding-box,border-box;
            box-shadow:
                0 0 0 1px #0b1017,
                0 8px 22px rgba(0,0,0,0.6),
                inset 0 0 0 1px rgba(96,168,224,0.26);
            color:var(--hud-text);
            font-family:var(--hud-font)
        }

        .tagline{display:flex;flex-direction:column;align-items:center;color:#fff;margin-bottom:1.2rem;padding-bottom:1rem;width:100%;text-transform:uppercase;border-bottom:1px solid rgba(99,200,255,0.35);animation:titleShimmer 2.8s ease-in-out infinite}
        .tagline-main{font-size:clamp(1.4rem,4vw,2rem);letter-spacing:0.22em;font-weight:800;line-height:1.1}
        .tagline-sub{font-size:clamp(0.72rem,1.9vw,0.95rem);letter-spacing:0.16em;font-weight:600;margin-top:0.5rem;opacity:0.85}

        @keyframes pulse{0%{transform:scale(1);box-shadow:0 0 0 0 rgba(255,119,85,0.8),0 0 0 0 rgba(80,200,255,0.4)}70%{transform:scale(1.07);box-shadow:0 0 12px 16px rgba(255,119,85,0),0 0 18px 24px rgba(80,200,255,0)}100%{transform:scale(1);box-shadow:0 0 0 0 rgba(255,119,85,0),0 0 0 0 rgba(80,200,255,0)}}
        .button{all:unset;cursor:pointer;background:linear-gradient(110deg,#f85 0%,#f73 40%,#fc6 50%,#f73 60%,#a41 100%);background-size:250% 100%;border:1px solid #c52;padding:14px 44px;color:#fff;font-size:1.3rem;font-weight:900;text-align:center;clip-path:polygon(10px 0,calc(100% - 10px) 0,100% 50%,calc(100% - 10px) 100%,10px 100%,0 50%);transition:transform 0.2s ease,filter 0.2s ease;animation:pulse 1.8s infinite,btnShine 3s linear infinite;letter-spacing:0.18em;text-transform:uppercase;text-shadow:0 1px 2px rgba(0,0,0,0.8),0 0 14px rgba(255,180,80,0.7)}
        /* Only where hovering is a thing. A touch browser applies :hover on tap and leaves it
           applied, so the button stayed blown up and glowing after it had been pressed. */
        @media (hover:hover){
            .button:hover{transform:scale(1.12);filter:brightness(1.25) drop-shadow(0 0 18px #f85) drop-shadow(0 0 28px rgba(80,200,255,0.5))}
        }
        .button:active{transform:scale(0.97);filter:brightness(1.15)}

        .features{list-style:none;margin-top:1.5rem;text-align:left;display:inline-block;padding:0}
        .features li{padding:0.35rem 0;padding-left:1.7rem;position:relative;color:var(--hud-text);font-size:1rem}
        .features li::before{content:'\25B8';position:absolute;left:0.1rem;top:0.32rem;color:#f73;font-size:1.05rem;line-height:1;animation:chevPulse 2.5s ease-in-out infinite}
        .features li:nth-child(2)::before{animation-delay:0.3s}
        .features li:nth-child(3)::before{animation-delay:0.6s}
        .features li:nth-child(4)::before{animation-delay:0.9s}
        .features li:nth-child(5)::before{animation-delay:1.2s}
        .features li:nth-child(6)::before{animation-delay:1.5s}

        /* Five of these now, and the row has to survive a narrow window: wrap rather than push the
           last one off the side. Two gaps - the small one only ever applies to a second line. */
        .social-links{margin-top:1.5rem;display:flex;flex-wrap:wrap;gap:0.6rem 1.5rem;justify-content:center}
        .social-links a{color:#ddd;text-decoration:none;font-size:0.95rem;transition:color 0.3s;text-shadow:0 1px 4px rgba(0,0,0,0.8)}
        .social-links a:hover{color:#f73}
        .social-links svg{width:20px;height:20px;vertical-align:middle;margin-right:0.4rem;fill:currentColor}

        @media (max-height:700px){
            .features{display:none}
            .landing{padding-bottom:8vh}
            .info-panel{padding:1rem 1.5rem}
        }
        @media (max-width:480px){
            .features li{font-size:0.92rem}
            .landing{background-position:center top}
            .info-panel{padding:1.25rem 1.25rem}
        }

        /* Portrait. Two things happen here and they belong together.

           Art direction, not a resize: the 16:9 hero laid over a 0.45 frame by center/cover shows
           27% of its width - a gear fragment reading "ZAR" over empty beach. The brand name is
           unreadable and everything that says "real-time strategy" in half a second is outside the
           crop. The portrait cut carries that content itself. Only the image swaps, which is why
           the hero is a variable: the gradient over it stays the same.

           And the panel gets out of the way. At 412x915 it was 494px - 54% of the screen - and at
           360x740 it was 79% and overflowed the document by 57px. It sat exactly over the base, the
           factory and the tesla coil, so swapping the picture alone only moved the problem. Three
           of the six lines carry the message; the rest stay for the desktop and for crawlers.

           After the max-width:480 block on purpose: both set .info-panel padding, and this one has
           to win. */
        @media (max-aspect-ratio:3/4){
            .landing{--hero:url('/razarion-bg-portrait.webp')}

            /* And the panel goes to the floor. It used to hang 18vh above it - 165px on a 915px
               screen - and what it covered was the tesla coil and the explosion, the one moment in
               the picture that reads as real-time strategy in half a second. Dropping it to the
               bottom edge hands that back without touching the panel itself.

               No safe-area inset needed: without viewport-fit=cover the viewport already ends above
               the gesture bar, so env() would resolve to zero and only look like it did something. */
            .landing{padding-bottom:0.75rem}
            /* Marks only. Five labelled links do not fit on one line at 412px, and the panel is
               already sitting on the bottom edge - a second row would take height the picture
               needs. The name lives on in the aria-label. */
            .social-links{margin-top:0.7rem;gap:0.5rem 1.4rem}
            .social-links a{font-size:0.85rem}
            .social-links .social-label{display:none}
            .social-links svg{width:22px;height:22px;margin-right:0}

            /* Out of the flow, so it cannot push the panel around; the panel keeps the bottom of
               the screen and this keeps the top. pointer-events off - it sits over the picture and
               must not eat a tap meant for the page. */
            .brand{position:absolute;top:1rem;left:0;right:0;width:auto;height:auto;margin:0;padding:0;
                   overflow:visible;clip:auto;z-index:1;pointer-events:none;
                   display:flex;align-items:center;justify-content:center;gap:0.55rem}
            .brand-mark{width:44px;height:auto;filter:drop-shadow(0 2px 5px rgba(0,0,0,0.7))}
            .brand-name{font-size:1.3rem;font-weight:800;letter-spacing:0.2em;text-transform:uppercase;
                        color:#fff;text-shadow:0 2px 6px rgba(0,0,0,0.85),0 0 16px rgba(80,200,255,0.4)}

            .info-panel{padding:1rem 1.1rem}
            .tagline{margin-bottom:0.8rem;padding-bottom:0.7rem}
            .features{margin-top:1rem}
            .features li{padding:0.2rem 0;padding-left:1.5rem;line-height:1.35}
            .features li::before{top:0.18rem}
            .features .secondary{display:none}
        }

        /* Six pulsing chevrons, a shimmering title and a pulsing button is a lot to ask of someone
           who has told their system they do not want it. The plate itself no longer moves. */
        @media (prefers-reduced-motion:reduce){
            .tagline,.button,.features li::before{animation:none}
        }
    </style>
</head>
<body>
    <section class="landing">
        <!-- One heading, two appearances. On the desktop hero the logo is part of the picture, so
             this stays off-screen for screen readers only. In portrait the picture has no logo -
             burning one in would not survive the crop - so the same heading becomes the visible
             brand: the gear cut out of the logo art, and the name as text, which stays crisp at any
             pixel density and costs no bytes. -->
        <h1 class="brand">
            <img class="brand-mark" src="/razarion-mark.webp" width="44" height="44" alt="" aria-hidden="true">
            <span class="brand-name">Razarion</span>
        </h1>
        <div class="info-panel">
            <p class="tagline"><span class="tagline-main">RTS meets MMO</span><span class="tagline-sub">One shared world that never stops</span></p>
            <!-- The navigation lives in the attribute because a visitor who arrives without
                 campaign parameters gets no script at all - the tracking block below is inside a
                 FreeMarker conditional. The flag is what keeps this from firing a second time
                 after the script has already sent them on their way; see goToGame there. -->
            <button class="button" id="playButton"
                    onclick="if(!window.RAZ_navigating){window.RAZ_navigating=1;location.href='/game${qs}'}">Play Now</button>
            <ul class="features">
                <!-- The three without "secondary" are the ones a phone keeps: what it is, what is
                     different about it, and what it costs to try. The others are true, but they can
                     be read after the click. -->
                <li>RTS mechanics like Command & Conquer and StarCraft</li>
                <li>Persistent world - always online</li>
                <li class="secondary">Massive shared map with all players</li>
                <li class="secondary">Quests, levels, and unlockable units</li>
                <li>No download - plays in your browser via WebAssembly</li>
                <li class="secondary">Open-source, nonprofit, and community-driven</li>
            </ul>
        </div>
        <!-- The label is a span so the portrait rules can drop it and leave the mark; aria-label
             carries the name either way, and the svg is decoration once the link is named. -->
        <div class="social-links">
            <a href="https://github.com/Razarion/razarion" target="_blank" rel="noopener" aria-label="Razarion on GitHub">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z"/></svg><span class="social-label">GitHub</span>
            </a>
            <a href="https://x.com/razariongame" target="_blank" rel="noopener" aria-label="Razarion on X">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z"/></svg><span class="social-label">X</span>
            </a>
            <a href="https://www.instagram.com/razariongame/" target="_blank" rel="noopener" aria-label="Razarion on Instagram">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.849-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zM12 0C8.741 0 8.333.014 7.053.072 2.695.272.273 2.69.073 7.052.014 8.333 0 8.741 0 12c0 3.259.014 3.668.072 4.948.2 4.358 2.618 6.78 6.98 6.98C8.333 23.986 8.741 24 12 24c3.259 0 3.668-.014 4.948-.072 4.354-.2 6.782-2.618 6.979-6.98.059-1.28.073-1.689.073-4.948 0-3.259-.014-3.667-.072-4.947-.196-4.354-2.617-6.78-6.979-6.98C15.668.014 15.259 0 12 0zm0 5.838a6.162 6.162 0 100 12.324 6.162 6.162 0 000-12.324zM12 16a4 4 0 110-8 4 4 0 010 8zm6.406-11.845a1.44 1.44 0 100 2.881 1.44 1.44 0 000-2.881z"/></svg><span class="social-label">Instagram</span>
            </a>
            <a href="https://www.youtube.com/@Razarion" target="_blank" rel="noopener" aria-label="Razarion on YouTube">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M23.498 6.186a3.016 3.016 0 00-2.122-2.136C19.505 3.545 12 3.545 12 3.545s-7.505 0-9.377.505A3.017 3.017 0 00.502 6.186C0 8.07 0 12 0 12s0 3.93.502 5.814a3.016 3.016 0 002.122 2.136c1.871.505 9.376.505 9.376.505s7.505 0 9.377-.505a3.015 3.015 0 002.122-2.136C24 15.93 24 12 24 12s0-3.93-.502-5.814zM9.545 15.568V8.432L15.818 12l-6.273 3.568z"/></svg><span class="social-label">YouTube</span>
            </a>
            <a href="https://www.facebook.com/Razarion" target="_blank" rel="noopener" aria-label="Razarion on Facebook">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/></svg><span class="social-label">Facebook</span>
            </a>
        </div>
    </section>
    <#if qs?has_content><img src="/t.gif${qs}" width="1" height="1" alt="" style="position:absolute;opacity:0">
    <script>
        // Reports what happened on this page, for visitors carrying campaign parameters - the same
        // population the pixel above counts, so the numbers can be put next to each other.
        //
        // The image tag says a visit happened; four out of five of those visits never reach the
        // game, and from the visit alone there is no way to tell why. These two events split that
        // group up: whether the button was pressed at all, and how long the page was looked at
        // before it was left.
        //
        // The dwell time alone still cannot say WHY a visit was short, and on a phone most of them
        // are: half the paid arrivals are gone inside a second. A second of nothing looks the same
        // whether the page was never on screen, whether it had not painted yet, or whether someone
        // looked and turned away. The remaining fields separate those three: was anyone looking at
        // all, how long the page took to arrive and to paint, and whether the visitor ever touched
        // it.
        //
        // Nothing here touches the button's own navigation. If any of this fails, "Play Now" still
        // works exactly as it did - telemetry must never be able to break the one thing the page
        // is for.
        (function () {
            // Already stripped of quotes, backslashes and whitespace by IndexController, and it
            // starts with "?" - so appending parameters to it is safe here.
            var query = '${qs}';
            var timing = typeof performance !== 'undefined' && !!performance.now;
            // Monotonic where available: a clock that the operating system adjusts mid-visit would
            // otherwise produce negative or absurd durations. This is also the time the document
            // took to reach its last line, measured from the navigation - the part of the wait the
            // server cannot see.
            var start = timing ? performance.now() : Date.now();
            var playClicked = false;
            var exitReported = false;
            var interacted = false;
            // Milliseconds until the Play button was at least half on screen; null means it never
            // was. See the observer below.
            var buttonSeenMillis = null;
            // A finger landed on the button, whether or not a click came of it.
            var buttonPressed = false;
            // Why the last touch on the button did not become a game. One letter, because it rides
            // on a url that already carries the whole campaign query string: 'c' the browser took
            // the gesture away, 'd' the finger travelled too far to be a tap, 'h' it rested too
            // long, 'o' it never came up at all. Null while no touch has failed - and a touch that
            // succeeds is the only thing that clears it again.
            var tapFailure = null;
            // What that failure measured: pixels for 'd', milliseconds for 'c', 'h' and 'o'. The
            // letter says which. Without the number a drag of fourteen pixels and one of two
            // hundred look the same, and only the first of them says the limit is set too tight.
            var tapFailureMeasure = null;
            // Furthest point reached, in percent of what there was to scroll.
            var maxScroll = 0;
            // The viewport as it was on arrival, before any address bar collapsed. The device
            // width is in the user agent; the height an app's browser leaves over is not, and it
            // is the half that decides whether the button is above the fold.
            var viewport = null;
            try {
                viewport = Math.round(innerWidth) + 'x' + Math.round(innerHeight);
            } catch (e) {
                // Nothing to report rather than nothing at all.
            }
            // A tap on an ad that opens the page behind the app, and a browser that loaded it on
            // spec, both produce a visit with a dwell time - and in neither case did a person see
            // anything. Read once, at the start: later the answer would always be "hidden".
            var visibleAtStart = document.visibilityState === 'visible';
            var prerendering = document.prerendering === true;

            function send(params) {
                var url = '/t.gif' + query + params;
                // sendBeacon is queued by the browser and survives the page going away; an image
                // request started at that moment would just be cancelled.
                if (navigator.sendBeacon) {
                    navigator.sendBeacon(url);
                } else {
                    new Image().src = url;
                }
            }

            // How far a finger may travel and how long it may rest and still be a tap rather than
            // a drag or a hold. Twelve pixels is about a millimetre of wobble on these screens.
            var TAP_SLOP = 12;
            var TAP_MILLIS = 800;

            // The one thing this page is for. Reached from the pointer coming up, from the click
            // that may or may not follow it, and from the button's own attribute - so the guard
            // has to be the same one the attribute reads, which is why it lives on window.
            function goToGame() {
                if (window.RAZ_navigating) {
                    return;
                }
                window.RAZ_navigating = 1;
                playClicked = true;
                // However the button was reached in the end, it was reached - a gesture the pointer
                // handlers had already given up on must not be reported as the reason it failed.
                tapFailure = null;
                tapFailureMeasure = null;
                send('&e=play');
                location.href = '/game' + query;
            }

            var button = document.getElementById('playButton');
            if (button) {
                // Two of every three taps that reached this button produced no click at all. That
                // is measured, not guessed: in one day 43 against 20, almost all of them in an
                // app's own browser, and the people who tapped then stayed a median of eleven
                // seconds waiting for something to happen before switching back to the app.
                // Whatever cancels the gesture there - a few pixels of drift, the webview's own
                // swipe handling - happens between the finger going down and a click that never
                // comes, so the navigation cannot be left waiting for one.
                //
                // The inline onclick stays as it is. It is what makes the button work for a
                // visitor who arrives without campaign parameters, for whom this whole script is
                // not rendered at all; and a click that does arrive is welcome. Both paths lead
                // through goToGame, which navigates once.
                var downX = 0;
                var downY = 0;
                var downMillis = 0;
                var downOnButton = false;

                function pointerDown(x, y) {
                    buttonPressed = true;
                    downOnButton = true;
                    downX = x;
                    downY = y;
                    downMillis = timing ? performance.now() : Date.now();
                    // Assumed the moment the finger lands, so that a page which goes away while it
                    // is still down reports that instead of nothing. Every way out of here
                    // overwrites it.
                    tapFailure = 'o';
                    tapFailureMeasure = null;
                }

                function heldMillis() {
                    return Math.round((timing ? performance.now() : Date.now()) - downMillis);
                }

                // A tap, or something else? Lifting far from where the finger landed is a drag,
                // and resting on the button is a long press - neither is a press of the button,
                // and treating them as one would send people into the game who did not ask.
                function pointerUp(x, y) {
                    if (!downOnButton) {
                        return;
                    }
                    downOnButton = false;
                    var dx = x - downX;
                    var dy = y - downY;
                    var travelled = Math.sqrt(dx * dx + dy * dy);
                    var held = heldMillis();
                    if (travelled > TAP_SLOP) {
                        tapFailure = 'd';
                        tapFailureMeasure = Math.round(travelled);
                    } else if (held > TAP_MILLIS) {
                        tapFailure = 'h';
                        tapFailureMeasure = held;
                    } else {
                        tapFailure = null;
                        tapFailureMeasure = null;
                        goToGame();
                    }
                }

                // The webview took the gesture for itself - a scroll, a swipe, a back navigation.
                // Nothing to do but forget the finger. How long it had been down when that
                // happened is the half worth keeping: taken away within fifty milliseconds is a
                // scroll starting under a finger that never meant to press, taken away after four
                // hundred is a press the browser decided against.
                function cancelled() {
                    if (!downOnButton) {
                        return;
                    }
                    downOnButton = false;
                    tapFailure = 'c';
                    tapFailureMeasure = heldMillis();
                }

                if (typeof PointerEvent === 'function') {
                    button.addEventListener('pointerdown', function (event) {
                        pointerDown(event.clientX, event.clientY);
                    }, {passive: true});
                    button.addEventListener('pointerup', function (event) {
                        pointerUp(event.clientX, event.clientY);
                    }, {passive: true});
                    button.addEventListener('pointercancel', function () {
                        cancelled();
                    }, {passive: true});
                } else {
                    button.addEventListener('touchstart', function (event) {
                        var touch = event.changedTouches[0];
                        pointerDown(touch.clientX, touch.clientY);
                    }, {passive: true});
                    button.addEventListener('touchend', function (event) {
                        var touch = event.changedTouches[0];
                        pointerUp(touch.clientX, touch.clientY);
                    }, {passive: true});
                    button.addEventListener('touchcancel', function () {
                        cancelled();
                    }, {passive: true});
                }

                button.addEventListener('click', function () {
                    goToGame();
                });

                // When the button first stood in front of them, if it ever did. Nine in ten
                // visitors here arrive in an app's own browser, which takes height away at the top
                // and the bottom; whether the call to action was on screen at all cannot be read
                // off the layout, only off the viewport it actually got.
                try {
                    if (typeof IntersectionObserver === 'function') {
                        var seen = new IntersectionObserver(function (entries) {
                            for (var i = 0; i < entries.length; i++) {
                                if (entries[i].isIntersecting) {
                                    buttonSeenMillis = Math.round((timing ? performance.now() : Date.now()) - start);
                                    seen.disconnect();
                                }
                            }
                        }, {threshold: 0.5});
                        seen.observe(button);
                    }
                } catch (e) {
                    // An optional measurement. The button works whether or not it is watched.
                }
            }

            // How far down they got. The panel is meant to fit without scrolling, so anything
            // above zero says it did not fit on that device - and a visitor who scrolled looked
            // for something rather than leaving at once.
            addEventListener('scroll', function () {
                var doc = document.documentElement;
                var scrollable = doc.scrollHeight - doc.clientHeight;
                if (scrollable > 0) {
                    var depth = Math.round(100 * (window.pageYOffset || doc.scrollTop) / scrollable);
                    if (depth > maxScroll) {
                        maxScroll = depth;
                    }
                }
            }, {passive: true});

            // Any sign of a hand: a finger down, a key, a scroll. Not "did they click Play" - the
            // question is whether the page was ever addressed at all, which is what separates a
            // visitor who looked and declined from one who never arrived in front of it. Passive
            // and once, so this cannot cost a frame or block a scroll.
            function markInteracted() {
                interacted = true;
            }

            ['pointerdown', 'touchstart', 'keydown', 'wheel', 'scroll'].forEach(function (type) {
                addEventListener(type, markInteracted, {once: true, passive: true, capture: true});
            });

            // When the browser first put something on the screen. Absent on browsers without paint
            // timing, and absent when the visit ended before the first frame - which is itself the
            // answer to "did they see anything".
            function paintMillis() {
                try {
                    var entries = performance.getEntriesByType('paint');
                    for (var i = 0; i < entries.length; i++) {
                        if (entries[i].name === 'first-contentful-paint') {
                            return Math.round(entries[i].startTime);
                        }
                    }
                } catch (e) {
                    // Paint timing is optional; a visit is still worth reporting without it.
                }
                return null;
            }

            // When the hero picture was fully there. It is the heaviest thing on the page by far,
            // and until it lands the panel sits on an empty background - so "was it loaded before
            // they left" is the one question that says whether the picture is worth its bytes.
            function heroMillis() {
                try {
                    var entries = performance.getEntriesByType('resource');
                    for (var i = 0; i < entries.length; i++) {
                        if (entries[i].name.indexOf('razarion-bg') >= 0 && entries[i].responseEnd > 0) {
                            return Math.round(entries[i].responseEnd);
                        }
                    }
                } catch (e) {
                    // Same as above - resource timing is a bonus, not a precondition.
                }
                return null;
            }

            function reportExit(reason) {
                if (exitReported) {
                    return;
                }
                exitReported = true;
                var now = timing ? performance.now() : Date.now();
                var params = '&e=exit&d=' + Math.round(now - start)
                    + '&r=' + reason
                    + '&v=' + (visibleAtStart ? '1' : '0')
                    + (interacted ? '&i=1' : '')
                    + (prerendering ? '&pr=1' : '')
                    + (playClicked ? '&p=1' : '');
                if (timing) {
                    params += '&l=' + Math.round(start);
                }
                var painted = paintMillis();
                if (painted !== null) {
                    params += '&fp=' + painted;
                }
                var hero = heroMillis();
                if (hero !== null) {
                    params += '&hb=' + hero;
                }
                if (buttonSeenMillis !== null) {
                    params += '&bs=' + buttonSeenMillis;
                }
                if (buttonPressed) {
                    params += '&bp=1';
                }
                if (tapFailure !== null) {
                    params += '&tf=' + tapFailure;
                    if (tapFailureMeasure !== null) {
                        params += '&tm=' + tapFailureMeasure;
                    }
                }
                if (maxScroll > 0) {
                    params += '&sd=' + maxScroll;
                }
                if (viewport !== null) {
                    params += '&vp=' + viewport;
                }
                send(params);
            }

            addEventListener('pagehide', function () {
                reportExit('u');
            });
            // Safari on iOS often discards a tab without ever firing pagehide; going hidden is the
            // only signal that arrives there. Reporting at the first of the two means a visitor who
            // switches away and comes back is measured up to the moment they switched - the
            // question this answers is whether people leave at once or after reading, and for that
            // the first departure is the honest number. Which of the two fired is sent along: a tab
            // switched away and a page navigated off are the same duration and not the same event.
            addEventListener('visibilitychange', function () {
                if (document.visibilityState === 'hidden') {
                    reportExit('h');
                }
            });
        })();
    </script></#if>
</body>
</html>
