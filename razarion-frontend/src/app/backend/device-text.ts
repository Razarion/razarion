/**
 * Browser and form factor out of a user agent string.
 *
 * Which browser and which version is what a failed start is usually about - WebAssembly GC needs
 * Chrome 119+, Firefox 120+ or Safari 18.2+ - and whether it was a phone decides how much of the
 * rest of the row is worth believing. The full string stays available wherever this is shown; it is
 * far too long for a table cell and says little that the two words here do not.
 */
export function deviceText(userAgent: string | null | undefined): string {
  if (!userAgent) {
    return '—';
  }
  const mobile = /Mobile|Android|iPhone|iPad/.test(userAgent);
  // Order matters: Edge and Chrome both claim Chrome, Chrome claims Safari.
  const browser = /Edg\//.test(userAgent) ? 'Edge'
    : /OPR\//.test(userAgent) ? 'Opera'
      : /Chrome\//.test(userAgent) ? 'Chrome'
        : /Firefox\//.test(userAgent) ? 'Firefox'
          : /Safari\//.test(userAgent) ? 'Safari'
            : 'other';
  const version = majorVersion(userAgent, browser);
  return `${browser}${version ? ' ' + version : ''}${mobile ? ' mobile' : ''}`;
}

/** Safari is the odd one out: its own token carries the engine build, not the browser version. */
function majorVersion(userAgent: string, browser: string): string {
  const pattern = browser === 'Edge' ? /Edg\/(\d+)/
    : browser === 'Opera' ? /OPR\/(\d+)/
      : browser === 'Chrome' ? /Chrome\/(\d+)/
        : browser === 'Firefox' ? /Firefox\/(\d+)/
          : browser === 'Safari' ? /Version\/(\d+)/
            : null;
  const match = pattern ? userAgent.match(pattern) : null;
  return match ? match[1] : '';
}
