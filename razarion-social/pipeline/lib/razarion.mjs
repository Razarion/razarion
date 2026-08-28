import { env, requireEnv } from '../../src/config.mjs';

// The live server. Everything here is read-only: a login, then two GETs.
const BASE = () => env.RAZARION_BASE_URL || 'https://www.razarion.com';

/**
 * Logs in the way deploy.ps1 does - Basic against /rest/user/auth, which answers with a bearer
 * token for the editor endpoints.
 *
 * The editor endpoints are the only place the unit data is readable: the game itself receives it
 * over the game connection rather than from a public REST endpoint, so there is no anonymous way
 * to ask what a Viper costs.
 */
export async function adminToken() {
  const [user, password] = requireEnv('RAZARION_ADMIN_USER', 'RAZARION_ADMIN_PASSWORD');
  const basic = Buffer.from(`${user}:${password}`).toString('base64');
  const res = await fetch(`${BASE()}/rest/user/auth`, {
    method: 'POST',
    headers: { Authorization: 'Basic ' + basic },
  });
  const body = await res.text();
  if (!res.ok) {
    throw new Error(
      `Login failed (HTTP ${res.status}). Check RAZARION_ADMIN_USER and RAZARION_ADMIN_PASSWORD.\n${body.slice(0, 200)}`
    );
  }
  return body.replace(/^"|"$/g, '').trim();
}

export async function baseItemTypes(token) {
  const res = await fetch(`${BASE()}/rest/editor/base_item_type/read`, {
    headers: { Authorization: 'Bearer ' + token },
  });
  if (!res.ok) throw new Error(`Could not read the unit types (HTTP ${res.status}).`);
  return res.json();
}

// Images are public by id - no token needed, which is why the generated card can be rebuilt later
// without credentials.
export async function fetchImage(id) {
  const res = await fetch(`${BASE()}/rest/image/${id}`);
  if (!res.ok) throw new Error(`Image ${id} came back HTTP ${res.status}.`);
  return Buffer.from(await res.arrayBuffer());
}

/**
 * What a unit actually is, from whichever of the type fields the config filled in.
 *
 * The data models a unit's role by the presence of a sub-config rather than by a label, so the
 * role has to be read off which one is set.
 */
export function roleOf(item) {
  if (item.harvesterType) return 'harvester';
  if (item.builderType) return 'builder';
  if (item.factoryType) return 'factory';
  if (item.generatorType) return 'generator';
  if (item.houseType) return 'house';
  if (item.itemContainerType) return 'container';
  if (item.weaponType) return 'weapon';
  return null;
}
