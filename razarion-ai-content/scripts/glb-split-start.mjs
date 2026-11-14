// Cuts the start glb out of the large one: only the named top-level models, nothing else changed.
// See docs/assets/3d-model-pipeline.md "Start glb" (2026-11-13).
//
//   npm install @gltf-transform/core@4.1.1 @gltf-transform/extensions@4.1.1 @gltf-transform/functions@4.1.1 draco3dgltf@1.5.7
//   curl -o all.glb https://www.razarion.com/rest/gltf/glb/1
//   node glb-split-start.mjs all.glb start.glb "Builder,Factory,Harvester,Resource 1"
//
// Not installed in razarion-ai-content on purpose - run it from a scratch folder with the packages above.
// No optimize/dedup/flatten/join: those strip the RAZ_ marker nodes and TEXCOORD_0 that the runtime
// material assignment needs. Only what no kept node references any more is pruned, and every kept
// model is compared against the source afterwards (nodes, RAZ_ markers, primitives, UVs, vertices,
// materials); the script exits non-zero if one differs.
import {NodeIO} from '@gltf-transform/core';
import {ALL_EXTENSIONS} from '@gltf-transform/extensions';
import {prune} from '@gltf-transform/functions';
import draco3d from 'draco3dgltf';
import fs from 'fs';

const [, , input, output, keepArg] = process.argv;
const keep = new Set(keepArg.split(',').map(s => s.trim()));
const io = new NodeIO().registerExtensions(ALL_EXTENSIONS).registerDependencies({
  'draco3d.decoder': await draco3d.createDecoderModule(),
  'draco3d.encoder': await draco3d.createEncoderModule()
});

const walk = (n, f) => { f(n); n.listChildren().forEach(c => walk(c, f)); };
function fingerprint(doc) {
  const out = {};
  for (const n of doc.getRoot().listScenes()[0].listChildren()) {
    const fp = {nodes: 0, raz: [], prims: 0, uv0: 0, verts: 0, mats: []};
    walk(n, x => {
      fp.nodes++;
      if (x.getName().startsWith('RAZ_')) fp.raz.push(x.getName());
      const m = x.getMesh();
      if (m) for (const p of m.listPrimitives()) {
        fp.prims++;
        if (p.getAttribute('TEXCOORD_0')) fp.uv0++;
        fp.verts += p.getAttribute('POSITION')?.getCount() ?? 0;
        fp.mats.push(p.getMaterial()?.getName() ?? '-');
      }
    });
    fp.raz = fp.raz.sort().join(','); fp.mats = fp.mats.join(',');
    out[n.getName()] = JSON.stringify(fp);
  }
  return out;
}

const source = await io.read(input);
const before = fingerprint(source);
const missing = [...keep].filter(k => !(k in before));
if (missing.length) throw new Error('Nicht in der GLB: ' + missing.join(', '));

const doc = await io.read(input);
const scene = doc.getRoot().listScenes()[0];
for (const n of [...scene.listChildren()]) {
  if (!keep.has(n.getName())) {
    walk(n, x => x.dispose());
  }
}
await doc.transform(prune({keepLeaves: true, keepAttributes: true, keepIndices: true, keepSolidTextures: true, keepExtras: true}));
await io.write(output, doc);

const after = fingerprint(await io.read(output));
let ok = true;
for (const k of keep) {
  const same = before[k] === after[k];
  ok &&= same;
  console.log(same ? 'gleich ' : 'ANDERS ', k, same ? '' : '\n  vorher ' + before[k] + '\n  nachher ' + after[k]);
}
const extra = Object.keys(after).filter(k => !keep.has(k));
if (extra.length) { ok = false; console.log('Unerwartete Knoten', extra); }
const r = (await io.read(output)).getRoot();
console.log(`Grösse ${Math.round(fs.statSync(output).size / 1024)} KB (Quelle ${Math.round(fs.statSync(input).size / 1024)} KB), ` +
  `Materialien ${r.listMaterials().map(m => m.getName()).join(',')}, Texturen ${r.listTextures().length}`);
console.log(ok ? 'VERIFIKATION OK' : 'VERIFIKATION FEHLGESCHLAGEN');
process.exit(ok ? 0 : 1);
