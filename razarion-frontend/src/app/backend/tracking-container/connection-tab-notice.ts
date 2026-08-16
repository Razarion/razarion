/**
 * Puts a connection change into the browser's tab strip.
 *
 * The tracking page is watched rather than read: it is left open next to whatever else is going on,
 * and what matters is the moment a player arrives or drops out. The tab strip is the surface that
 * reaches you there whether or not the page is the one in front of you, so that is where the
 * message goes - the title, and the icon next to it.
 *
 * It only ever speaks to a tab nobody is looking at, and it steps back the moment you look - and on
 * its own once nothing has happened for a while, so a page left open overnight does not still shout
 * about someone who came and went hours ago. Whoever has the page in front of them is served by the
 * connections table instead, which says the same thing in more detail and keeps itself current.
 *
 * The same shape as the game's tab-ready-notice, and for the same reason: cosmetics must never take
 * the view down, so everything is guarded and failure is silent.
 */

/** How long the announcement stays up after the last change it reported. */
const HOLD_MILLIS = 15000;

/** Long names would push the rest of the title out of a tab that is only so wide. */
const MAX_NAME_LENGTH = 14;

/** Kept so the title can be handed back exactly as it was found. */
let originalTitle: string | null = null;
let originalIcon: string | null = null;
let restoreTimer: ReturnType<typeof setTimeout> | null = null;
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
    // and a title they can read anyway would only sit there until the hold runs out.
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
    const badged = badgedIcon(joined.length > 0, left.length > 0);
    if (link && badged) {
      if (originalIcon === null) {
        originalIcon = link.getAttribute('href');
      }
      link.setAttribute('href', badged);
    }

    if (restoreTimer !== null) {
      clearTimeout(restoreTimer);
    }
    restoreTimer = setTimeout(clearConnectionTabNotice, HOLD_MILLIS);

    // Looking at the tab is reading the message; it has no business outliving that.
    if (!listening) {
      listening = true;
      document.addEventListener('visibilitychange', restoreWhenVisible);
    }
  } catch (ignored) {
  }
}

function restoreWhenVisible(): void {
  if (!document.hidden) {
    clearConnectionTabNotice();
  }
}

/** Takes the announcement back at once - when you look at the tab, and when the hold time is up. */
export function clearConnectionTabNotice(): void {
  if (restoreTimer !== null) {
    clearTimeout(restoreTimer);
    restoreTimer = null;
  }
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
 */
function badgedIcon(joined: boolean, left: boolean): string | null {
  const canvas = document.createElement('canvas');
  canvas.width = 32;
  canvas.height = 32;
  const context = canvas.getContext('2d');
  if (!context) {
    return null;
  }
  context.fillStyle = '#0f172a';
  context.fillRect(0, 0, 32, 32);
  if (joined && left) {
    // Both happened: two half-height marks, arrivals on top.
    triangle(context, '#22c55e', 4, 15, true);
    triangle(context, '#ef4444', 17, 28, false);
  } else if (joined) {
    triangle(context, '#22c55e', 5, 27, true);
  } else {
    triangle(context, '#ef4444', 5, 27, false);
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
