<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Razarion – RTS meets MMO: One World That Never Stops</title>
    <base href="/">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="RTS meets MMO: one shared world that never stops. Inspired by Command &amp; Conquer and StarCraft — persistent shared world, quests and levels. Play free in your browser via WebAssembly, no download. Open-source and community-driven.">
    <link rel="icon" type="image/x-icon" href="/favicon.ico">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/augmented-ui@2/augmented-ui.min.css">

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
        html,body{height:100%;overflow:hidden}
        body{font-family:system-ui,-apple-system,'Segoe UI',Roboto,sans-serif;background:#0a0a12;color:#e0e0e0;line-height:1.6}

        .landing{height:100vh;background:linear-gradient(180deg,rgba(10,10,18,0) 0%,rgba(10,10,18,0) 35%,rgba(10,10,18,0.55) 65%,rgba(10,10,18,0.88) 100%),url('/razarion-bg.webp') center/cover no-repeat;display:flex;flex-direction:column;align-items:center;justify-content:flex-end;text-align:center;padding:2rem 2rem 18vh}

        @keyframes panelGlow{0%,100%{filter:drop-shadow(0 0 28px rgba(255,119,51,0.45)) drop-shadow(0 0 60px rgba(80,200,255,0.12))}50%{filter:drop-shadow(0 0 55px rgba(255,140,60,0.85)) drop-shadow(0 0 100px rgba(80,200,255,0.35))}}
        @keyframes chevPulse{0%,100%{color:#f73;text-shadow:0 0 6px rgba(255,140,60,0.7)}50%{color:#fc6;text-shadow:0 0 14px rgba(255,180,80,1),0 0 4px #fff}}
        @keyframes titleShimmer{0%,100%{text-shadow:0 0 14px rgba(255,140,60,0.9),0 0 4px rgba(255,255,255,0.4)}50%{text-shadow:0 0 22px rgba(255,180,80,1),0 0 8px rgba(255,255,255,0.7),0 0 40px rgba(255,140,60,0.6)}}
        @keyframes btnShine{0%{background-position:-200% 0}100%{background-position:200% 0}}

        /* augmented-ui shape config — tweak these to experiment.
           Try other shapes: tl-2-clip-x, tr-rect, br-round, bl-scoop, t-clip-x (notch), etc.
           See augmented-ui.com builder for live preview. */
        .info-panel{
            --aug-border-all:1px;
            --aug-border-bg:linear-gradient(180deg,#f85 0%,#f73 35%,rgba(80,200,255,0.55) 75%,#5cf 100%);
            --aug-inlay-bg:repeating-linear-gradient(0deg,transparent 0,transparent 3px,rgba(255,255,255,0.03) 3px,rgba(255,255,255,0.03) 4px),repeating-linear-gradient(90deg,transparent 0,transparent 24px,rgba(255,119,51,0.04) 24px,rgba(255,119,51,0.04) 25px),linear-gradient(180deg,rgba(18,28,42,0.92) 0%,rgba(5,10,16,0.96) 100%);
            --aug-tl:14px;
            --aug-tr:36px;
            --aug-br:14px;
            --aug-bl:36px;
            position:relative;
            padding:2rem 2.5rem;
            display:flex;
            flex-direction:column;
            align-items:center;
            max-width:640px;
            animation:panelGlow 3.5s ease-in-out infinite
        }

        .tagline{position:relative;z-index:1;display:flex;flex-direction:column;align-items:center;color:#fff;margin-bottom:1.2rem;padding-bottom:1rem;width:100%;text-transform:uppercase;border-bottom:1px solid rgba(255,119,51,0.5);animation:titleShimmer 2.8s ease-in-out infinite}
        .tagline-main{font-size:clamp(1.4rem,4vw,2rem);letter-spacing:0.22em;font-weight:800;line-height:1.1}
        .tagline-sub{font-size:clamp(0.72rem,1.9vw,0.95rem);letter-spacing:0.16em;font-weight:600;margin-top:0.5rem;opacity:0.85}

        @keyframes pulse{0%{transform:scale(1);box-shadow:0 0 0 0 rgba(255,119,85,0.8),0 0 0 0 rgba(80,200,255,0.4)}70%{transform:scale(1.07);box-shadow:0 0 12px 16px rgba(255,119,85,0),0 0 18px 24px rgba(80,200,255,0)}100%{transform:scale(1);box-shadow:0 0 0 0 rgba(255,119,85,0),0 0 0 0 rgba(80,200,255,0)}}
        .button{all:unset;position:relative;z-index:1;cursor:pointer;background:linear-gradient(110deg,#f85 0%,#f73 40%,#fc6 50%,#f73 60%,#a41 100%);background-size:250% 100%;border:1px solid #c52;padding:14px 44px;color:#fff;font-size:1.3rem;font-weight:900;text-align:center;clip-path:polygon(10px 0,calc(100% - 10px) 0,100% 50%,calc(100% - 10px) 100%,10px 100%,0 50%);transition:transform 0.2s ease,filter 0.2s ease;animation:pulse 1.8s infinite,btnShine 3s linear infinite;letter-spacing:0.18em;text-transform:uppercase;text-shadow:0 1px 2px rgba(0,0,0,0.8),0 0 14px rgba(255,180,80,0.7)}
        .button:hover{transform:scale(1.12);filter:brightness(1.25) drop-shadow(0 0 18px #f85) drop-shadow(0 0 28px rgba(80,200,255,0.5))}

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
        }
    </style>
</head>
<body>
    <section class="landing">
        <h1 class="visually-hidden" style="position:absolute;width:1px;height:1px;padding:0;margin:-1px;overflow:hidden;clip:rect(0,0,0,0);border:0">RAZARION</h1>
        <div class="info-panel" data-augmented-ui="tl-clip tr-clip br-clip bl-clip both">
            <p class="tagline"><span class="tagline-main">RTS meets MMO</span><span class="tagline-sub">One shared world that never stops</span></p>
            <button class="button" onclick="location.href='/game${qs}'">Play Now</button>
            <ul class="features">
                <li>RTS mechanics like Command & Conquer and StarCraft</li>
                <li>Persistent world - always online</li>
                <li>Massive shared map with all players</li>
                <li>Quests, levels, and unlockable units</li>
                <li>No download - plays in your browser via WebAssembly</li>
                <li>Open-source, nonprofit, and community-driven</li>
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
    <#if qs?has_content><img src="/t.gif${qs}" width="1" height="1" alt="" style="position:absolute;opacity:0"></#if>
</body>
</html>
