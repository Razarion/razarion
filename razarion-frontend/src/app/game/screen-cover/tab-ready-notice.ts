/**
 * Tells a player who switched tabs during the load that the game is up.
 *
 * A quarter of the sessions that reach the engine put the tab in the background while it was
 * still loading, and they do it after about five seconds - less than even a fast load takes.
 * Making the load faster therefore does not reach these players at all; a signal does, whatever
 * the load ends up costing. The tab strip is the only surface a backgrounded page owns, so that
 * is where it goes: the title, and the icon next to it.
 *
 * Cosmetics must never break the game, so everything here is guarded and failure is silent.
 */

/** Kept so the announcement can be taken back the moment the player looks at the tab again. */
let originalTitle: string | null = null;
let originalIcon: string | null = null;
let listening = false;

export function announceReadyIfHidden(): void {
  try {
    // Watching the game load is not something to interrupt.
    if (!document.hidden || originalTitle !== null) {
      return;
    }
    originalTitle = document.title;
    document.title = '▶ Ready – ' + originalTitle;

    const link = iconLink();
    const badged = badgedIcon();
    if (link && badged) {
      originalIcon = link.getAttribute('href');
      link.setAttribute('href', badged);
    }

    if (!listening) {
      listening = true;
      document.addEventListener('visibilitychange', restoreWhenVisible);
    }
  } catch (ignored) {
  }
}

function restoreWhenVisible(): void {
  if (document.hidden) {
    return;
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
  } finally {
    document.removeEventListener('visibilitychange', restoreWhenVisible);
    listening = false;
  }
}

function iconLink(): HTMLLinkElement | null {
  return document.querySelector<HTMLLinkElement>('link[rel~="icon"]');
}

/**
 * A play mark rather than the usual unread dot: the tab is not asking to be read, it is saying
 * the game is running. Drawn instead of shipped as a file so it cannot go missing from a build.
 */
function badgedIcon(): string | null {
  const canvas = document.createElement('canvas');
  canvas.width = 32;
  canvas.height = 32;
  const context = canvas.getContext('2d');
  if (!context) {
    return null;
  }
  context.fillStyle = '#0f172a';
  context.fillRect(0, 0, 32, 32);
  context.fillStyle = '#22c55e';
  context.beginPath();
  context.moveTo(11, 7);
  context.lineTo(26, 16);
  context.lineTo(11, 25);
  context.closePath();
  context.fill();
  return canvas.toDataURL('image/png');
}
