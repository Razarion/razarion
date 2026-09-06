/*
 * Re-encodes the textures embedded in the material and particle payloads to WebP.
 *
 * Six materials and a particle system are read on every game start and weigh 9,145 KB between
 * them - 67% of everything a player downloads before the first frame. 8,925 KB of that is
 * textures, carried as base64 data URIs inside the JSON, as JPEG and PNG.
 *
 * Two rules, both conservative:
 *
 *   - A JPEG has already spent its loss, so it is re-encoded lossily (q85). Encoding it losslessly
 *     stores the artefacts as if they were detail: the vehicle normal map goes from 490 KB to
 *     3,951 KB that way. Measured, not feared.
 *   - A PNG here carries alpha and usually data rather than colour - decal masks, the water
 *     normals, the particle sprite - so it goes to lossless WebP and comes out smaller anyway.
 *
 * And one guard over both: if the result is not smaller, the original is kept. All four textures
 * of the building material are already efficiently encoded and every one of them grows.
 *
 * Dimensions are left alone. Halving them is worth another 2.4 MB and is a decision about how the
 * game looks, not one a script should take on its own.
 *
 * Measured on the content of 2026-09-04: 9,145 KB -> 5,621 KB, and 6,618 -> 4,073 KB on the wire
 * once the endpoints compress (see BabylonMaterialController.getData). Verified in a running
 * client: terrain, vehicles, buildings and the placer all render unchanged.
 *
 *   npm i sharp        # not a repository dependency; this is a one-off migration
 *   node recompress-material-textures.js                       # dry run against localhost
 *   node recompress-material-textures.js --apply               # write
 *   RAZARION_BASE_URL=https://www.razarion.com \
 *   RAZARION_ADMIN_USER=... RAZARION_ADMIN_PASSWORD=... \
 *     node recompress-material-textures.js --apply
 *
 * Reading needs nothing; writing goes through the ADMIN upload endpoints, which set the content
 * digest in the same transaction as the bytes. Do not write the blob column directly - a payload
 * whose digest still describes the previous content is served to every browser holding the old
 * copy as "unchanged".
 */
const sharp = require('sharp');

const BASE = process.env.RAZARION_BASE_URL || 'http://localhost:8080';
const USER = process.env.RAZARION_ADMIN_USER;
const PASSWORD = process.env.RAZARION_ADMIN_PASSWORD;
const APPLY = process.argv.includes('--apply');

const TARGETS = [
  {name: 'Material', ids: [1, 2, 3, 8, 11, 12], method: 'POST', path: 'rest/babylon-material'},
  {name: 'Partikel ', ids: [6], method: 'PUT', path: 'rest/editor/particle-system'}
];
const dataUrl = (t, id) => `${BASE}/${t.path}/data/${id}`;
const uploadUrl = (t, id) => `${BASE}/${t.path}/upload/${id}`;
const kb = n => Math.round(n / 1024);

async function reencode(dataUri) {
  const original = Buffer.from(dataUri.slice(dataUri.indexOf(',') + 1), 'base64');
  let meta;
  try {
    meta = await sharp(original).metadata();
  } catch (unreadable) {
    // Not every data URI in here is a picture: some blocks carry a truncated or empty one beside
    // the real texture. Passed through untouched - dropping one would take a texture off a model
    // and nothing would say so.
    return {uri: dataUri, kept: true, skipped: true};
  }
  if (meta.format === 'webp') {
    // Already migrated. Running this twice must be a no-op, and it was not: a WebP is not a PNG,
    // so the second pass took the lossless water normals down the lossy branch and lost 55% of
    // them quietly. Once converted, the origin is no longer visible in the file - so nothing here
    // may touch it again.
    return {uri: dataUri, kept: true, already: true};
  }
  const encoded = meta.format === 'png'
    ? await sharp(original).webp({lossless: true, effort: 4}).toBuffer()
    : await sharp(original).webp({quality: 85}).toBuffer();
  if (encoded.length >= original.length) {
    return {uri: dataUri, kept: true};
  }
  return {uri: 'data:image/webp;base64,' + encoded.toString('base64'), kept: false};
}

async function convert(node, stats) {
  if (!node || typeof node !== 'object') {
    return;
  }
  for (const [key, value] of Object.entries(node)) {
    if (typeof value === 'string' && value.startsWith('data:')) {
      const result = await reencode(value);
      node[key] = result.uri;
      stats.push(result);
    } else {
      await convert(value, stats);
    }
  }
}

(async () => {
  if (APPLY && !(USER && PASSWORD)) {
    console.error('Zum Schreiben braucht es RAZARION_ADMIN_USER und RAZARION_ADMIN_PASSWORD.');
    process.exit(1);
  }
  // Derselbe Weg, den k8s/scripts/deploy.ps1 gegen PROD geht: Basic gilt an /rest/user/auth,
  // alles andere will den Bearer. Direkt mit Basic gegen den Upload holt ein 401.
  let auth = null;
  if (APPLY) {
    const basic = 'Basic ' + Buffer.from(USER + ':' + PASSWORD).toString('base64');
    const token = await fetch(BASE + '/rest/user/auth', {method: 'POST', headers: {Authorization: basic}});
    if (!token.ok) {
      console.error('Anmeldung fehlgeschlagen: HTTP ' + token.status);
      process.exit(1);
    }
    auth = 'Bearer ' + (await token.text()).trim();
  }
  let totalBefore = 0;
  let totalAfter = 0;
  for (const target of TARGETS) {
    for (const id of target.ids) {
      const response = await fetch(dataUrl(target, id));
      if (!response.ok) {
        console.log(`  ${target.name} ${id}: HTTP ${response.status}, uebersprungen`);
        continue;
      }
      const before = Buffer.from(await response.arrayBuffer());
      const json = JSON.parse(before.toString('utf8'));
      const stats = [];
      await convert(json, stats);
      const after = Buffer.from(JSON.stringify(json), 'utf8');
      totalBefore += before.length;
      totalAfter += after.length;
      const kept = stats.filter(s => s.kept && !s.skipped && !s.already).length;
      const already = stats.filter(s => s.already).length;
      const skipped = stats.filter(s => s.skipped).length;
      console.log(`  ${target.name} ${String(id).padStart(2)}  ${String(kb(before.length)).padStart(5)} KB -> `
        + `${String(kb(after.length)).padStart(5)} KB   ${stats.length} Texturen`
        + (kept ? `, ${kept} behalten (waeren groesser)` : '')
        + (already ? `, ${already} schon WebP` : '')
        + (skipped ? `, ${skipped} unlesbar und unveraendert` : ''));
      if (APPLY) {
        const put = await fetch(uploadUrl(target, id), {
          method: target.method,
          headers: {'Content-Type': 'application/octet-stream', Authorization: auth},
          body: after
        });
        if (!put.ok) {
          console.error(`  ${target.name} ${id}: Upload fehlgeschlagen, HTTP ${put.status}`);
          process.exit(1);
        }
      }
    }
  }
  console.log(`\nSumme  ${kb(totalBefore)} KB -> ${kb(totalAfter)} KB   `
    + `(${Math.round(100 * totalAfter / totalBefore)}%, gespart ${kb(totalBefore - totalAfter)} KB)`);
  console.log(APPLY ? `Geschrieben nach ${BASE}.` : 'Trockenlauf - mit --apply schreiben.');
})().catch(e => { console.error(e); process.exit(1); });
