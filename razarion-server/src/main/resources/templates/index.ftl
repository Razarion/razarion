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
         are gone in under two seconds. The frame is drawn with clip-path below instead: same
         shape, no request, painted with the first frame. -->
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
    <meta name="twitter:site" content="@AloRtsDev">
    <meta name="twitter:title" content="Razarion – RTS meets MMO: One World That Never Stops">
    <meta name="twitter:description" content="RTS meets MMO: one shared world that never stops. Inspired by Command &amp; Conquer and StarCraft — persistent shared world, quests and levels. Play free in your browser via WebAssembly, no download. Open-source and community-driven.">
    <meta name="twitter:image" content="https://razarion.com/card.jpg">

    <style>
        *{margin:0;padding:0;box-sizing:border-box}
        html,body{height:100%}
        body{font-family:system-ui,-apple-system,'Segoe UI',Roboto,sans-serif;background:#0a0a12;color:#e0e0e0;line-height:1.6}

        /* svh is the viewport with the browser's own bars showing, which is what a phone actually
           displays when the page opens - vh is the taller one it only reaches after a scroll. The
           page used to be exactly 100vh with overflow hidden, so on a phone the lower part of the
           panel sat behind the address bar with no way to scroll to it. min-height rather than
           height, and no overflow rule, so nothing here can ever be out of reach. */
        /* Off screen, read aloud. The portrait rules below give it a body. */
        .brand{position:absolute;width:1px;height:1px;padding:0;margin:-1px;overflow:hidden;clip:rect(0,0,0,0);border:0}

        .landing{position:relative;--hero:url('/razarion-bg.webp');min-height:100vh;min-height:100svh;background:linear-gradient(180deg,rgba(10,10,18,0) 0%,rgba(10,10,18,0) 35%,rgba(10,10,18,0.55) 65%,rgba(10,10,18,0.88) 100%),var(--hero) center/cover no-repeat;display:flex;flex-direction:column;align-items:center;justify-content:flex-end;text-align:center;padding:2rem 2rem 18vh}


        @keyframes panelGlow{0%,100%{filter:drop-shadow(0 0 28px rgba(255,119,51,0.45)) drop-shadow(0 0 60px rgba(80,200,255,0.12))}50%{filter:drop-shadow(0 0 55px rgba(255,140,60,0.85)) drop-shadow(0 0 100px rgba(80,200,255,0.35))}}
        @keyframes chevPulse{0%,100%{color:#f73;text-shadow:0 0 6px rgba(255,140,60,0.7)}50%{color:#fc6;text-shadow:0 0 14px rgba(255,180,80,1),0 0 4px #fff}}
        @keyframes titleShimmer{0%,100%{text-shadow:0 0 14px rgba(255,140,60,0.9),0 0 4px rgba(255,255,255,0.4)}50%{text-shadow:0 0 22px rgba(255,180,80,1),0 0 8px rgba(255,255,255,0.7),0 0 40px rgba(255,140,60,0.6)}}
        @keyframes btnShine{0%{background-position:-200% 0}100%{background-position:200% 0}}

        /* Panel frame. The element itself carries the gradient and is cut to the notched shape;
           ::before is the same shape inset by one pixel and holds the fill, so what shows through
           along the edge is a 1px gradient border. Corner sizes are the four variables - clockwise
           from the top left, same as the shape this replaces. */
        .info-panel{
            --corner-tl:14px;
            --corner-tr:36px;
            --corner-br:14px;
            --corner-bl:36px;
            --panel-shape:polygon(
                var(--corner-tl) 0,
                calc(100% - var(--corner-tr)) 0,
                100% var(--corner-tr),
                100% calc(100% - var(--corner-br)),
                calc(100% - var(--corner-br)) 100%,
                var(--corner-bl) 100%,
                0 calc(100% - var(--corner-bl)),
                0 var(--corner-tl)
            );
            position:relative;
            padding:2rem 2.5rem;
            display:flex;
            flex-direction:column;
            align-items:center;
            max-width:640px;
            background:linear-gradient(180deg,#f85 0%,#f73 35%,rgba(80,200,255,0.55) 75%,#5cf 100%);
            clip-path:var(--panel-shape);
            animation:panelGlow 3.5s ease-in-out infinite
        }
        .info-panel::before{
            content:'';
            position:absolute;
            inset:1px;
            clip-path:var(--panel-shape);
            background:repeating-linear-gradient(0deg,transparent 0,transparent 3px,rgba(255,255,255,0.03) 3px,rgba(255,255,255,0.03) 4px),repeating-linear-gradient(90deg,transparent 0,transparent 24px,rgba(255,119,51,0.04) 24px,rgba(255,119,51,0.04) 25px),linear-gradient(180deg,rgba(18,28,42,0.92) 0%,rgba(5,10,16,0.96) 100%)
        }

        .tagline{position:relative;z-index:1;display:flex;flex-direction:column;align-items:center;color:#fff;margin-bottom:1.2rem;padding-bottom:1rem;width:100%;text-transform:uppercase;border-bottom:1px solid rgba(255,119,51,0.5);animation:titleShimmer 2.8s ease-in-out infinite}
        .tagline-main{font-size:clamp(1.4rem,4vw,2rem);letter-spacing:0.22em;font-weight:800;line-height:1.1}
        .tagline-sub{font-size:clamp(0.72rem,1.9vw,0.95rem);letter-spacing:0.16em;font-weight:600;margin-top:0.5rem;opacity:0.85}

        @keyframes pulse{0%{transform:scale(1);box-shadow:0 0 0 0 rgba(255,119,85,0.8),0 0 0 0 rgba(80,200,255,0.4)}70%{transform:scale(1.07);box-shadow:0 0 12px 16px rgba(255,119,85,0),0 0 18px 24px rgba(80,200,255,0)}100%{transform:scale(1);box-shadow:0 0 0 0 rgba(255,119,85,0),0 0 0 0 rgba(80,200,255,0)}}
        .button{all:unset;position:relative;z-index:1;cursor:pointer;background:linear-gradient(110deg,#f85 0%,#f73 40%,#fc6 50%,#f73 60%,#a41 100%);background-size:250% 100%;border:1px solid #c52;padding:14px 44px;color:#fff;font-size:1.3rem;font-weight:900;text-align:center;clip-path:polygon(10px 0,calc(100% - 10px) 0,100% 50%,calc(100% - 10px) 100%,10px 100%,0 50%);transition:transform 0.2s ease,filter 0.2s ease;animation:pulse 1.8s infinite,btnShine 3s linear infinite;letter-spacing:0.18em;text-transform:uppercase;text-shadow:0 1px 2px rgba(0,0,0,0.8),0 0 14px rgba(255,180,80,0.7)}
        /* Only where hovering is a thing. A touch browser applies :hover on tap and leaves it
           applied, so the button stayed blown up and glowing after it had been pressed. */
        @media (hover:hover){
            .button:hover{transform:scale(1.12);filter:brightness(1.25) drop-shadow(0 0 18px #f85) drop-shadow(0 0 28px rgba(80,200,255,0.5))}
        }
        .button:active{transform:scale(0.97);filter:brightness(1.15)}

        .features{position:relative;z-index:1;list-style:none;margin-top:1.5rem;text-align:left;display:inline-block;padding:0}
        .features li{padding:0.35rem 0;padding-left:1.7rem;position:relative;color:#e8e8e8;font-size:1rem}
        .features li::before{content:'\25B8';position:absolute;left:0.1rem;top:0.32rem;color:#f73;font-size:1.05rem;line-height:1;animation:chevPulse 2.5s ease-in-out infinite}
        .features li:nth-child(2)::before{animation-delay:0.3s}
        .features li:nth-child(3)::before{animation-delay:0.6s}
        .features li:nth-child(4)::before{animation-delay:0.9s}
        .features li:nth-child(5)::before{animation-delay:1.2s}
        .features li:nth-child(6)::before{animation-delay:1.5s}

        .social-links{margin-top:1.5rem;display:flex;gap:1.5rem;justify-content:center}
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
            /* panelGlow animates two drop-shadows, which means re-rasterising the whole panel and
               its blur every frame. On a phone that is the most expensive thing on the page and it
               competes with the paint we are trying to make fast. The glow stays, it just stops
               breathing. */
            .info-panel{animation:none;filter:drop-shadow(0 0 28px rgba(255,119,51,0.45))}
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

        /* Six pulsing chevrons, a shimmering title, a pulsing button and a breathing panel is a
           lot to ask of someone who has told their system they do not want it. */
        @media (prefers-reduced-motion:reduce){
            .info-panel,.tagline,.button,.features li::before{animation:none}
            .info-panel{filter:drop-shadow(0 0 28px rgba(255,119,51,0.45))}
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
            <button class="button" id="playButton" onclick="location.href='/game${qs}'">Play Now</button>
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
        <div class="social-links">
            <a href="https://github.com/Razarion/razarion" target="_blank" rel="noopener">
                <svg viewBox="0 0 24 24"><path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z"/></svg>
                GitHub
            </a>
            <a href="https://x.com/AloRtsDev" target="_blank" rel="noopener">
                <svg viewBox="0 0 24 24"><path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z"/></svg>
                X
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
        // Nothing here touches the button's own navigation. If any of this fails, "Play Now" still
        // works exactly as it did - telemetry must never be able to break the one thing the page
        // is for.
        (function () {
            // Already stripped of quotes, backslashes and whitespace by IndexController, and it
            // starts with "?" - so appending parameters to it is safe here.
            var query = '${qs}';
            // Monotonic where available: a clock that the operating system adjusts mid-visit would
            // otherwise produce negative or absurd durations.
            var start = typeof performance !== 'undefined' && performance.now ? performance.now() : Date.now();
            var playClicked = false;
            var exitReported = false;

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

            var button = document.getElementById('playButton');
            if (button) {
                button.addEventListener('click', function () {
                    playClicked = true;
                    send('&e=play');
                });
            }

            function reportExit() {
                if (exitReported) {
                    return;
                }
                exitReported = true;
                var now = typeof performance !== 'undefined' && performance.now ? performance.now() : Date.now();
                send('&e=exit&d=' + Math.round(now - start) + (playClicked ? '&p=1' : ''));
            }

            addEventListener('pagehide', reportExit);
            // Safari on iOS often discards a tab without ever firing pagehide; going hidden is the
            // only signal that arrives there. Reporting at the first of the two means a visitor who
            // switches away and comes back is measured up to the moment they switched - the
            // question this answers is whether people leave at once or after reading, and for that
            // the first departure is the honest number.
            addEventListener('visibilitychange', function () {
                if (document.visibilityState === 'hidden') {
                    reportExit();
                }
            });
        })();
    </script></#if>
</body>
</html>
