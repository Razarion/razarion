<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Razarion - Free Browser RTS Game</title>
    <base href="/">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="Free browser-based RTS game. Build armies, command units, fight in a persistent multiplayer world. No download required.">
    <link rel="icon" type="image/x-icon" href="/favicon.ico">

    <!-- Open Graph Tags -->
    <meta property="og:title" content="Razarion - Free Browser RTS Game">
    <meta property="og:description" content="Free browser-based RTS game. Build armies, command units, fight in a persistent multiplayer world. No download required.">
    <meta property="og:image" content="https://razarion.com/card.jpg">
    <meta property="og:url" content="https://razarion.com">
    <meta property="og:type" content="website">

    <!-- Twitter Card Tags -->
    <meta name="twitter:card" content="summary_large_image">
    <meta name="twitter:title" content="Razarion - Free Browser RTS Game">
    <meta name="twitter:description" content="Free browser-based RTS game. Build armies, command units, fight in a persistent multiplayer world. No download required.">
    <meta name="twitter:image" content="https://razarion.com/card.jpg">

    <style>
        *{margin:0;padding:0;box-sizing:border-box}
        html,body{height:100%;overflow:hidden}
        body{font-family:system-ui,-apple-system,'Segoe UI',Roboto,sans-serif;background:#0a0a12;color:#e0e0e0;line-height:1.6}

        .landing{height:100vh;background:linear-gradient(180deg,rgba(10,10,18,0.2) 0%,rgba(10,10,18,0.5) 50%,rgba(10,10,18,0.8) 100%),url('/razarion-bg.webp') center/cover no-repeat;display:flex;flex-direction:column;align-items:center;justify-content:center;text-align:center;padding:2rem}

        .logo{font-size:clamp(2.5rem,8vw,4.5rem);font-weight:800;color:#fff;text-shadow:0 0 40px rgba(255,119,51,0.5),0 0 80px rgba(255,119,51,0.3);margin-bottom:0.5rem;animation:glow 3s ease-in-out infinite alternate}
        @keyframes glow{from{text-shadow:0 0 40px rgba(255,119,51,0.5),0 0 80px rgba(255,119,51,0.3)}to{text-shadow:0 0 60px rgba(255,119,51,0.7),0 0 100px rgba(255,119,51,0.4)}}

        .tagline{font-size:clamp(1rem,3vw,1.4rem);color:#ddd;margin-bottom:2rem;max-width:550px}

        @keyframes pulse{0%{transform:scale(1);box-shadow:0 0 0 0 rgba(255,119,85,0.7)}70%{transform:scale(1.05);box-shadow:0 0 10px 10px rgba(255,119,85,0)}100%{transform:scale(1);box-shadow:0 0 0 0 rgba(255,119,85,0)}}
        .button{all:unset;cursor:pointer;background:linear-gradient(180deg,#f75 0%,#a41 100%);border:1px solid #931;padding:10px 25px;color:#fff;font-size:larger;font-weight:900;text-align:center;border-radius:4px;transition:transform 0.2s ease,box-shadow 0.2s ease;animation:pulse 2s infinite}
        .button:hover{transform:scale(1.1);box-shadow:0 0 10px #f75}

        .features{list-style:none;margin-top:2rem;text-align:left;display:inline-block;background-color:#0000004f;padding:1rem 1.5rem;border-radius:8px}
        .features li{padding:0.4rem 0;padding-left:1.5rem;position:relative;color:#ccc;font-size:1.05rem}
        .features li::before{content:'';position:absolute;left:0;top:50%;transform:translateY(-50%);width:8px;height:8px;background:#f73;border-radius:50%}

        .social-links{margin-top:2rem;display:flex;gap:1.5rem;justify-content:center}
        .social-links a{color:#aaa;text-decoration:none;font-size:0.95rem;transition:color 0.3s}
        .social-links a:hover{color:#f73}
        .social-links svg{width:20px;height:20px;vertical-align:middle;margin-right:0.4rem;fill:currentColor}
    </style>
</head>
<body>
    <section class="landing">
        <h1 class="logo">RAZARION</h1>
        <p class="tagline">Open world browser RTS game</p>
        <button class="button" onclick="location.href='/game${qs}'">Play Now</button>
        <ul class="features">
            <li>RTS mechanics like Command & Conquer and StarCraft</li>
            <li>Persistent world - always online</li>
            <li>Massive shared map with all players</li>
            <li>Quests, levels, and unlockable units</li>
            <li>No download - plays in your browser</li>
            <li>Open-source, nonprofit, and community-driven</li>
        </ul>
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
