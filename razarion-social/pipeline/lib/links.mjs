// Tags the links of the account's own posts, so a visit they bring can be told from a bought one.
//
//   utm_source    social-ig | social-fb | social-x | social-yt   - the network it was clicked on
//   utm_medium    social                                          - unpaid, always
//   utm_campaign  the post id, or "bio" for a profile link        - which post it was
//
// The server files anything with a utm_source starting "social-" under its own channel, "Own
// posts" in the backend's Daily tab (TrackingPlatforms.isOwnPost), and lets it outrank the click
// id. That is the point of the prefix: Meta's apps append an fbclid to every link that leaves
// them, so without it every organic visitor from Instagram or Facebook counted as paid - which
// is how the posts ended up with no measurable effect at all.
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

export function trackedLink(url, network, campaign) {
  if (!url || !isOurs(url)) return url;
  if (!NETWORKS.includes(network)) throw new Error(`Unknown network "${network}" for a tracked link.`);
  const tagged = new URL(url);
  tagged.searchParams.set('utm_source', `social-${network}`);
  tagged.searchParams.set('utm_medium', 'social');
  tagged.searchParams.set('utm_campaign', campaign || 'untagged');
  return tagged.toString();
}

/** What goes into each profile's link field - set by hand, once, in each network's settings. */
export function bioLinks(base = 'https://www.razarion.com/') {
  return Object.fromEntries(NETWORKS.map((n) => [n, trackedLink(base, n, 'bio')]));
}
