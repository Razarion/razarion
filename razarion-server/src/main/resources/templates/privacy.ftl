<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Privacy Policy – Razarion</title>
    <base href="/">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="What Razarion stores, why, and how to have it removed.">
    <link rel="icon" type="image/x-icon" href="/favicon.ico">
    <style>
        /* The same tokens as the landing page (index.ftl), inline for the same reason: no request
           in front of the first paint. Kept plain on purpose - a reviewer reads this page, nobody
           plays it, so none of the cockpit's animation is repeated here. */
        *{margin:0;padding:0;box-sizing:border-box}
        body{font-family:'Segoe UI',system-ui,-apple-system,sans-serif;background:#0a0a12;color:#cfe0f0;line-height:1.7}
        .wrap{max-width:760px;margin:0 auto;padding:3rem 1.5rem 5rem}
        a{color:#63c8ff}
        a:hover{color:#f73}
        h1{font-size:clamp(1.6rem,4vw,2.2rem);color:#fff;letter-spacing:0.06em;text-transform:uppercase;line-height:1.2}
        h2{font-size:1.15rem;color:#fff;margin:2.4rem 0 0.7rem;letter-spacing:0.04em}
        h3{font-size:1rem;color:#63c8ff;margin:1.4rem 0 0.4rem;font-weight:600}
        p{margin:0.7rem 0}
        ul{margin:0.6rem 0 0.6rem 1.3rem}
        li{padding:0.15rem 0}
        code{font-family:ui-monospace,'Cascadia Mono',Consolas,monospace;font-size:0.9em;color:#8ba3ba}
        .updated{color:#8ba3ba;font-size:0.9rem;margin-top:0.6rem}
        .lead{border-bottom:1px solid rgba(99,200,255,0.35);padding-bottom:1.2rem}
        .note{border-left:3px solid #f73;padding:0.6rem 0 0.6rem 1rem;margin:1.2rem 0;color:#e6eef7}
        footer{margin-top:3rem;padding-top:1.2rem;border-top:1px solid rgba(99,200,255,0.2);font-size:0.9rem;color:#8ba3ba}
        footer a{margin-right:1.2rem}
    </style>
</head>
<body>
<div class="wrap">
    <div class="lead">
        <h1>Privacy Policy</h1>
        <p class="updated">Razarion &middot; last updated 4 September 2026</p>
    </div>

    <p>Razarion is a free, nonprofit, open-source browser game. This page says what it stores about
        you, why, and how to have it removed. It is short because there is not much to describe: the
        site carries no advertising trackers and no third-party analytics.</p>

    <h2>1. Who is responsible</h2>
    <p>Beat Keller, operator of Razarion (<a href="https://razarion.com">razarion.com</a>).<br>
        Contact: <a href="mailto:beat.keller@btxtech.com">beat.keller@btxtech.com</a></p>

    <h2>2. What is stored</h2>

    <h3>Account data &mdash; only if you register</h3>
    <ul>
        <li>Your email address, used to verify the account and to reset a forgotten password</li>
        <li>Your password, stored only as a bcrypt hash &mdash; never in readable form</li>
        <li>Your player name, and the dates the account was created and verified</li>
    </ul>
    <p>You can play without registering. An unregistered visit stores no account data at all.</p>

    <h3>Game data</h3>
    <p>The state of your base and your progress: level, experience, crystals, active and completed
        quests, inventory, unlocked units, and when your client connected and disconnected. This is
        the game itself &mdash; without it there is nothing to return to.</p>

    <h3>Visit data</h3>
    <p>When a page is opened, one record is written describing the visit:</p>
    <ul>
        <li>Which page, and the time the server received the request</li>
        <li>Your browser's user-agent string and the page you arrived from</li>
        <li>A session identifier, so several requests from one visit are counted as one visit</li>
        <li>Campaign parameters carried by the link you clicked &mdash; <code>utm_source</code>,
            <code>utm_medium</code>, <code>utm_campaign</code>, <code>fbclid</code>,
            <code>twclid</code>, <code>rdt_cid</code> &mdash; and the raw query string</li>
        <li>How the page performed and how it was used: load time, first paint, image load, time on
            page, viewport size, scroll depth, whether the page was ever visible, whether it was
            touched, whether the button was pressed, and how the page was left</li>
    </ul>

    <div class="note">
        <strong>Your IP address is not stored.</strong> It is not written to the visit record and
        not kept anywhere else. Neither is anything else that would identify you personally to us,
        if you have not registered.
    </div>

    <h2>3. Why it is stored</h2>
    <ul>
        <li><strong>Account data</strong> &mdash; to give you an account and keep it yours. Without
            an email address a lost password cannot be reset.</li>
        <li><strong>Game data</strong> &mdash; to run the game and let you continue where you left
            off.</li>
        <li><strong>Visit data</strong> &mdash; to see where visitors come from and why most of them
            leave before the game has loaded. It is read as counts, not as individual behaviour, and
            it is what tells us which parts of the game are too slow to reach.</li>
    </ul>

    <h2>4. Cookies</h2>
    <p>One session cookie, set by the web server so that a series of requests is recognised as one
        session. It expires when the browser session ends.</p>
    <p>There are no advertising cookies, no analytics cookies, and no third-party scripts, pixels or
        fonts on this site. The links to GitHub, X, Instagram, YouTube and Facebook are ordinary
        links: they load nothing until you click them.</p>

    <h2>5. Who else can see it</h2>
    <ul>
        <li><strong>Hosting.</strong> The servers and databases run on Google Cloud Platform, which
            processes the data on our behalf and for no other purpose.</li>
        <li><strong>Email delivery.</strong> Verification and password-reset messages are sent
            through an SMTP provider, which sees the recipient address and the message.</li>
        <li><strong>Advertising platforms.</strong> The campaign parameters above come from links we
            place on X, Meta and Reddit. We read what the link carries; nothing is sent back to
            those platforms from this site.</li>
    </ul>
    <p>Your data is never sold, never traded, and never used to build an advertising profile.</p>

    <h2>6. Google account data</h2>
    <p>Razarion publishes its own gameplay clips to its own YouTube channel using a tool built on
        YouTube Data API v3. That tool authorises with the operator's Google account and no other.
        It uses the scopes <code>youtube.upload</code> and <code>youtube</code> for one purpose:
        uploading a video and its thumbnail to the Razarion channel. It reads no other channel, and
        the refresh token it receives is stored locally on the operator's own machine.</p>
    <p>No visitor's Google account is ever accessed, and no visitor data is sent to Google as part
        of it.</p>
    <p>Razarion's use of information received from Google APIs adheres to the
        <a href="https://developers.google.com/terms/api-services-user-data-policy" target="_blank"
           rel="noopener">Google API Services User Data Policy</a>, including the Limited Use
        requirements.</p>

    <h2>7. How long it is kept</h2>
    <ul>
        <li><strong>Account and game data</strong> &mdash; until you ask for the account to be
            deleted.</li>
        <li><strong>Visit data</strong> &mdash; kept as long as it is useful for the traffic
            statistics it was collected for, and deleted once it is not.</li>
    </ul>

    <h2>8. Your rights</h2>
    <p>You can ask what is stored about you, have it corrected, have it deleted, object to its use,
        or receive a copy. Write to
        <a href="mailto:beat.keller@btxtech.com">beat.keller@btxtech.com</a> and you will get an
        answer. Deleting your account removes your account data and your game data.</p>

    <h2>9. Changes</h2>
    <p>If this policy changes, the date at the top changes with it. Razarion is open source &mdash;
        the code that does the storing described here can be read at
        <a href="https://github.com/Razarion/razarion" target="_blank" rel="noopener">github.com/Razarion/razarion</a>.</p>

    <footer>
        <a href="/">Home</a><a href="/terms">Terms of Service</a>
    </footer>
</div>
</body>
</html>
