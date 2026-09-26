// Tags the links of the account's own posts, so a visit they bring can be told from a bought one.
//
//   https://www.razarion.com/fb/own-20260925180000     a link inside a post
//   https://www.razarion.com/ig                        the profile link
//
// The server answers these short paths (ProfileLinkController) with a redirect to the landing
// page carrying
//
//   utm_source    social-ig | social-fb | social-x | social-yt   - the network it was clicked on
//   utm_medium    social                                          - unpaid, always
//   utm_campaign  the post id, or "bio" for a profile link        - which post it was
//
// Short because a profile shows its link, and the tagged form was cut off at
// "razarion.com/?utm_source=so..." - which reads like spam.
//
// The server files anything with a utm_source starting "social-" under its own channel, "Own
// posts" in the backend's Daily tab (TrackingPlatforms.isOwnPost), and lets it outrank the click
// id. That is the point of the prefix: Meta's apps append an fbclid to every link that leaves
// them, so without it every organic visitor from Instagram or Facebook counted as paid.
//
// Only links to razarion.com are touched. A link to GitHub or anywhere else goes out as it was.

export const NETWORKS = ['ig', 'fb', 'x', 'yt'];

export function isOurs(url) {
  try {
    const host = new URL(url).hostname.toLowerCase();
    return host === 'razarion.com' || host.endsWith('.razarion.com');
  } catch {
    return false;
  }
}

/**
 * A razarion.com link turned into the short tagged one. Only a link to the site itself is
 * rewritten: the short path always lands on the landing page, so a deeper link keeps its target.
 */
export function trackedLink(url, network, campaign) {
  if (!url || !isOurs(url)) return url;
  if (!NETWORKS.includes(network)) throw new Error(`Unknown network "${network}" for a tracked link.`);
  const parsed = new URL(url);
  if (parsed.pathname !== '/' || parsed.search) return url;
  return `${parsed.origin}/${network}${campaign && campaign !== 'bio' ? '/' + campaign : ''}`;
}

/** What goes into each profile's link field - set by hand, once, in each network's settings. */
export function bioLinks(base = 'https://www.razarion.com/') {
  return Object.fromEntries(NETWORKS.map((n) => [n, trackedLink(base, n, 'bio')]));
}
