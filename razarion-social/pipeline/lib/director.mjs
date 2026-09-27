// The Director's REST side, shared by record_director.mjs (which films a battle) and the battle
// format (which decides what to film). Both have to agree on how far from the players a battle
// stays, so the rule lives here once.

/** Kept clear of every player base, on top of what the server itself insists on. */
export const MIN_HUMAN_CLEARANCE = 400;

export function directorApi(origin, token) {
  const call = async (method, path, body) => {
    const res = await fetch(`${origin}/rest/director${path}`, {
      method,
      headers: { Authorization: `Bearer ${token}`, ...(body ? { 'Content-Type': 'application/json' } : {}) },
      body: body ? JSON.stringify(body) : undefined,
    });
    const text = await res.text();
    if (!res.ok) throw new Error(`${method} ${path}: HTTP ${res.status} ${text.slice(0, 300)}`);
    return text ? JSON.parse(text) : null;
  };
  return {
    get: (p) => call('GET', p),
    post: (p, b) => call('POST', p, b),
    del: (p) => call('DELETE', p),
  };
}

/** Edge-to-edge distance from a point to the nearest player base. */
export function clearance(x, y, humans) {
  return Math.min(Infinity, ...humans.map((b) => Math.hypot(b.centreX - x, b.centreY - y) - (b.radius ?? 0)));
}

/** GET /bases split into the players' bases and the bot bases that still have something in them. */
export function splitBases(bases) {
  return {
    humans: bases.filter((b) => b.character === 'HUMAN' && b.centreX != null),
    bots: bases.filter((b) => b.character !== 'HUMAN' && b.centreX != null && b.itemCount > 0),
  };
}

/**
 * Camera styles, so that not every clip is the same view from above.
 *
 * Every key follows the fighting (followWhat 'combat'); a style only says where around it the
 * camera sits and how it moves. `beta` is the elevation (pi/2 straight down), `radius` the
 * distance, `alpha` the side the camera sits on, relative to the side the strike force comes from
 * (0 = behind the attackers, looking where they drive). A style is a list of stops across the
 * take; the client eases between them, so a list of stops is a camera move.
 *
 *   overhead   the September clips: steep, square to the grid, still
 *   low-orbit  low behind the attackers, circling a third of the way round as the fight goes on
 *   push-in    starts wide and high, closes in on the fighting
 *   side       low and square to the attack, the impact crossing the frame
 */
export const CAMERA_STYLES = {
  overhead: { absoluteAlpha: true, stops: [{ alpha: 0, beta: 1.0, radius: 80 }] },
  'low-orbit': {
    stops: [
      { alpha: 0, beta: 0.5, radius: 60 },
      { alpha: 0.6, beta: 0.45, radius: 55 },
      { alpha: 1.2, beta: 0.45, radius: 55 },
    ],
  },
  'push-in': {
    stops: [
      { alpha: -0.3, beta: 1.1, radius: 130 },
      { alpha: 0, beta: 0.8, radius: 70 },
      { alpha: 0.2, beta: 0.65, radius: 45 },
    ],
  },
  side: { stops: [{ alpha: Math.PI / 2, beta: 0.55, radius: 60 }] },
};
