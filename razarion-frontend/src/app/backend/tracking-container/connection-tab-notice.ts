/**
 * Puts a connection change into the browser's tab strip.
 *
 * The tracking page is watched rather than read: it is left open next to whatever else is going on,
 * and what matters is the moment a player arrives or drops out. The tab strip is the surface that
 * reaches you there whether or not the page is the one in front of you, so that is where the
 * message goes - the title, and the icon next to it.
 *
 * It only ever speaks to a tab nobody is looking at, and it stays up until somebody does. There is
 * no timeout: a notice that took itself down after a while would drop exactly the arrival you were
 * away for, which is the one it exists to report. Looking at the tab is what clears it, and that is
 * also the moment the connections table below takes over and says the same thing in more detail.
 *
 * The icon pulses while it waits, because a tab strip is scanned rather than read and a still icon
 * that changed ten minutes ago looks the same as one that was always like that.
 *
 * The same shape as the game's tab-ready-notice, and for the same reason: cosmetics must never take
 * the view down, so everything is guarded and failure is silent.
 */

/**
 * How long each phase of the pulse lasts.
 *
 * A hidden tab is exactly where a browser throttles timers, and one second is the floor Chrome
 * allows a background page - asking for less only gets rounded up to this. Once the page has been
 * hidden for five minutes it drops further, to roughly once a minute, and a page cannot honestly
 * talk its way out of that. Which is why both frames below are unmistakably a notice: whichever one
 * the pulse happens to be resting on, it still reads as "something happened", so the throttling
 * costs liveliness and never the message.
 */
const BLINK_MILLIS = 1000;

/** Long names would push the rest of the title out of a tab that is only so wide. */
const MAX_NAME_LENGTH = 14;

/** Kept so the title and the icon can be handed back exactly as they were found. */
let originalTitle: string | null = null;
let originalIcon: string | null = null;
/** The two icons the pulse alternates between, bright first. Null while nothing is announced. */
let blinkFrames: [string, string] | null = null;
let blinkTimer: ReturnType<typeof setInterval> | null = null;
let blinkPhase = 0;
let listening = false;

/**
 * @param joined labels of the players whose connection appeared since the last look
 * @param left   labels of those whose connection is gone
 */
export function announceConnectionChange(joined: string[], left: string[]): void {
  if (joined.length === 0 && left.length === 0) {
    return;
  }
  try {
    // Nothing to announce to someone who is already looking at the page: the table below has it,
    // and a title they can read anyway would only sit there.
    if (!document.hidden) {
      return;
    }
    // Only the first announcement may take the original; a second one would otherwise remember an
    // announcement as the title and never be able to restore.
    if (originalTitle === null) {
      originalTitle = document.title;
    }
    document.title = `${headline(joined, left)} – ${originalTitle}`;

    const link = iconLink();
    const bright = badgedIcon(joined.length > 0, left.length > 0, true);
    const dim = badgedIcon(joined.length > 0, left.length > 0, false);
    if (link && bright && dim) {
      if (originalIcon === null) {
        originalIcon = link.getAttribute('href');
      }
      // A later change re-aims the pulse rather than starting a second one of its own, and puts it
      // back on the bright frame so the new news shows in the same instant it is reported.
      blinkFrames = [bright, dim];
      blinkPhase = 0;
      link.setAttribute('href', bright);
      if (blinkTimer === null) {
        blinkTimer = setInterval(pulse, BLINK_MILLIS);
      }
    }

    // Looking at the tab is reading the message; it has no business outliving that.
    if (!listening) {
      listening = true;
      document.addEventListener('visibilitychange', restoreWhenVisible);
    }
  } catch (ignored) {
  }
}

/**
 * One step of the pulse. Guarded like everything else here - a timer that throws every second would
 * fill the console of a page whose whole job is to be left alone.
 */
function pulse(): void {
  try {
    const link = iconLink();
    if (link === null || blinkFrames === null) {
      return;
    }
    blinkPhase = 1 - blinkPhase;
    link.setAttribute('href', blinkFrames[blinkPhase]);
  } catch (ignored) {
  }
}

function restoreWhenVisible(): void {
  if (!document.hidden) {
    clearConnectionTabNotice();
  }
}

/**
 * Takes the announcement back. Reached when somebody looks at the tab, and when the page watching
 * the connections goes away - nothing else ends a notice.
 */
export function clearConnectionTabNotice(): void {
  if (blinkTimer !== null) {
    clearInterval(blinkTimer);
    blinkTimer = null;
  }
  blinkFrames = null;
  blinkPhase = 0;
  if (listening) {
    document.removeEventListener('visibilitychange', restoreWhenVisible);
    listening = false;
  }
  try {
    if (originalTitle !== null) {
      document.title = originalTitle;
      originalTitle = null;
    }
    const link = iconLink();
    if (link && originalIcon !== null) {
      link.setAttribute('href', originalIcon);
      originalIcon = null;
    }
  } catch (ignored) {
  }
}

/**
 * A single arrival is worth naming - that is the whole news. More than one, and the count says more
 * than a list that no longer fits, so the names stay in the table.
 */
function headline(joined: string[], left: string[]): string {
  const parts: string[] = [];
  if (joined.length > 0) {
    parts.push(joined.length === 1 ? `▲ ${shorten(joined[0])}` : `▲ ${joined.length}`);
  }
  if (left.length > 0) {
    parts.push(left.length === 1 ? `▼ ${shorten(left[0])}` : `▼ ${left.length}`);
  }
  return parts.join(' ');
}

function shorten(name: string): string {
  return name.length > MAX_NAME_LENGTH ? name.substring(0, MAX_NAME_LENGTH - 1) + '…' : name;
}

function iconLink(): HTMLLinkElement | null {
  return document.querySelector<HTMLLinkElement>('link[rel~="icon"]');
}

/**
 * Arrivals point up and departures point down, so the direction reads at icon size where a colour
 * alone would not - green and red are the one pair that half of red-green colour vision deficiency
 * cannot tell apart. Drawn rather than shipped as a file so it cannot go missing from a build.
 *
 * Two brightnesses of one drawing, which is what the pulse alternates between. Blinking the badge
 * against the plain favicon was the obvious other option and is the wrong one: a throttled pulse
 * can rest on a frame for a minute, and for that minute a tab showing the plain icon is a tab
 * saying nothing happened.
 */
function badgedIcon(joined: boolean, left: boolean, bright: boolean): string | null {
  const canvas = document.createElement('canvas');
  canvas.width = 32;
  canvas.height = 32;
  const context = canvas.getContext('2d');
  if (!context) {
    return null;
  }
  const green = bright ? '#22c55e' : '#15803d';
  const red = bright ? '#ef4444' : '#991b1b';
  context.fillStyle = '#0f172a';
  context.fillRect(0, 0, 32, 32);
  if (joined && left) {
    // Both happened: two half-height marks, arrivals on top.
    triangle(context, green, 4, 15, true);
    triangle(context, red, 17, 28, false);
  } else if (joined) {
    triangle(context, green, 5, 27, true);
  } else {
    triangle(context, red, 5, 27, false);
  }
  return canvas.toDataURL('image/png');
}

function triangle(context: CanvasRenderingContext2D, color: string,
                  top: number, bottom: number, pointingUp: boolean): void {
  context.fillStyle = color;
  context.beginPath();
  if (pointingUp) {
    context.moveTo(16, top);
    context.lineTo(27, bottom);
    context.lineTo(5, bottom);
  } else {
    context.moveTo(16, bottom);
    context.lineTo(27, top);
    context.lineTo(5, top);
  }
  context.closePath();
  context.fill();
}
