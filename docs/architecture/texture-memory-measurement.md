# Texture memory: what the model actually costs, and what compression would buy

Measured 2026-09-10 against `razarion-glb-1.glb` as served by PROD. Nothing here is committed as a
pipeline change; this is the evidence for deciding one.

## The starting point

One GLTF entity exists. All 53 `Model3D` rows point at nodes inside it. It carries **101 images**,
100 of them 1024×1024 and one 512×512. The file is **10.83 MB on disk**, of which the images are
7.64 MB, and those images become **535 MB of GPU memory** once the browser has decoded them to
RGBA8 and generated mipmaps — an inflation factor of 70× on the image bytes. The
separate `IMAGE_LIBRARY` adds 50 images, 15.1 MB on disk, 110 MB on the GPU.

A typical session touches 76 of the 101 images (403 MB); a session that never builds anything
still touches 24 (127 MB). Splitting the GLB per model saves little, because the models share
heavily — sharing factor 3.46, and 70 of the 101 images are used by more than one model. That is
why the work below is about the images themselves and not about the file they sit in.

## What ETC2 does to these textures

The route measured is the real one: PNG → Basis (basisu 1.16.4, with mipmaps) → transcode to the
format a Mali-G52 actually receives. Twelve representative images: four normal maps, five albedo
(one with a real alpha channel), two metallic/smoothness, one AO.

For normal maps PSNR is the wrong metric — what matters is how far the reconstructed normal tilts,
because that goes straight into the lighting. Both are reported.

| class | mode | PSNR RGB | download / image | GPU / image |
|---|---|---|---|---|
| albedo | ETC1S q128 | 34.0 dB | 139 kB | 0.67 MB |
| albedo | ETC1S q255 | 36.9 dB | 193 kB | 0.67 MB |
| albedo | UASTC | 47.4 dB | 822 kB | 1.33 MB |
| normal | ETC1S q128 | 33.8 dB | 138 kB | 0.67 MB |
| normal | UASTC | 44.3 dB | 826 kB | 1.33 MB |
| metal/smooth | ETC1S q128 | 34.9 dB | 68 kB | 0.67 MB |
| AO | ETC1S q128 | 36.1 dB | 120 kB | 0.67 MB |

Angular error on the normal maps, ETC1S → ETC2:

| image | mean | p99 | max | share over 2° |
|---|---|---|---|---|
| Pads_NRMLl | 2.43° | 17.5° | 70.7° | 32.3 % |
| Main_forms_Normal | 2.37° | 18.2° | 96.9° | 29.4 % |
| Rock1_nmp | 4.48° | 32.0° | 170.3° | 55.4 % |
| fern1_Normal | 1.34° | 7.0° | 25.7° | 17.6 % |

That is not acceptable for the rock normals and marginal for the buildings. Basis has a path for
exactly this — `-separate_rg_to_color_alpha` puts X in the colour slice and Y in the alpha slice,
the device transcodes to ETC2 EAC RG11 (two independent 11-bit channels) and the shader
reconstructs Z. It roughly halves the error, at double the GPU cost (8 bpp instead of 4):

| image | ETC1S RGB | ETC1S → RG11 | UASTC |
|---|---|---|---|
| Pads_NRMLl | 2.34° | 1.03° | 0.48° |
| Main_forms_Normal | 2.21° | 0.99° | 0.48° |
| Rock1_nmp | 4.50° | 2.45° | 1.32° |
| fern1_Normal | 1.30° | 0.61° | 0.31° |

The worst single number in the whole set is `Pads_MS` at **25.0 dB** under ETC1S. ETC1S assumes
three correlated colour channels sharing one codebook; metallic and smoothness are unrelated data
packed into R and G, so the assumption is simply false there.

UASTC is clean everywhere (0.3–1.3° on normals, 47 dB on albedo) — the transcode measured through
BC7, which comes out of the same 128-bit UASTC block as the ASTC the phone would get.

## The finding that changes the answer

**ETC1S makes the download bigger, not smaller.** The 12 sample images weigh 979 kB as the WebP
that ships today and 1.47 MB as ETC1S KTX2 — **1.5× larger**, and that ratio is what the decision
rests on.

Extrapolating the sample to all 101 images gives 8.3 MB today, 12.4 MB as ETC1S and 74.3 MB as
UASTC. Those absolute figures run high: the twelve images were picked to be representative of the
*kinds* of texture, not of the size distribution, and they are heavier than average. Counting the
bufferViews that images actually own puts the shipped texture payload at **7.64 MB**, against
2.99 MB of geometry and 0.19 MB of JSON, summing to the 10.83 MB file with no bytes unaccounted.
Scaled by the same correction, ETC1S would be about 11.4 MB. Use the ratios from the sample and
the measured 7.64 MB for anything absolute.

KTX2 is a GPU-memory format, not a download format. Given that the startup byte budget is already
the measured bottleneck on mobile (patience 6.4 s against a 9.0 s load), buying GPU memory with
50 % more download is the wrong direction on its own.

## The lever that beats it: the textures are far too large for the screen

Texel density computed straight from the GLB — for every triangle, UV area × resolution against
world area — combined with the camera (FreeCamera at 46.1 units slant distance, 0.8 rad vertical
FOV, so 39.0 world units of view height at the default zoom).

- phone, 360 px tall: 9.2 pixels per world unit
- desktop, 1080 px tall: 27.7 pixels per world unit

| model | size (world units) | texels/unit | texels per pixel, phone | desktop |
|---|---|---|---|---|
| Palm plant | 1.5 | 1322 | 143.1 | 47.7 |
| Harvester | 2.1 | 590 | 63.9 | 21.3 |
| Factory | 3.9 | 240 | 26.0 | 8.7 |
| Powerplant | 3.9 | 179 | 19.3 | 6.4 |
| Rock2 | 15.3 | 26 | 2.8 | 0.9 |

**Median over all 53 models: 20.9 texels per pixel on a phone, 7.0 on desktop.** Mip level 0 is
never sampled at the default zoom. Zoom clamps at a terrain distance of 5 against a default of 30,
so at maximum zoom-in the median falls to 3.5 on a phone and 1.2 on desktop — 1024² is one full
mip level above what the closest possible view needs, and three to four levels above the normal
one.

## The options

Download is the texture payload only — the 2.99 MB of geometry and 0.19 MB of JSON are the same in
every row. The two rows marked *measured* are counted off the actual files; the rest are the
twelve-image sample extrapolated to 101, which runs about 8 % high in absolute terms (see above)
but is sound for comparing the variants against each other.

| variant | download | GPU | quality |
|---|---|---|---|
| 1024 WebP → RGBA8 (today) | **7.64 MB** *measured* | **535 MB** | reference |
| 1024 ETC1S → ETC2 | 12.4 MB | 67 MB | normals 2.2–4.5° |
| 1024 UASTC → ASTC | 74.3 MB | 135 MB | normals 0.3–1.3° |
| **512 WebP → RGBA8** | **2.87 MB** *measured* | **135 MB** | still 3–10× oversampled at default zoom |
| 256 WebP → RGBA8 | 1.4 MB | 34 MB | fine on a phone, thin for a zoomed-in desktop |
| 512 ETC1S → ETC2 | 4.4 MB | 17 MB | normals 2.2–4.5° |
| 512 ETC1S, normals as RG11 | 5.2 MB | 22 MB | normals 1.0–2.5° |

Note what the two measured rows say about ETC1S: at 512 it is extrapolated at 4.4 MB against the
2.87 MB that WebP actually delivers. Compression loses to resizing on the download axis at every
resolution.

Resizing is the only lever that moves both numbers the same way, and it needs no transcoder, no
renderer change, and no new file format. It is one argument in the Unity pipeline, which already
runs `resize --width 1024 --height 1024`.

KTX2 keeps its value as a second step — after a resize to 512 it still takes GPU memory from
135 MB to 17 MB — but it should be decided on its own merits once the cheap factor of four is in.

## Reproducing

Scripts live in the session scratchpad, not in the repo: `glb-texturen.js` (inventory),
`glb-modellnutzung.js` (what a session touches), `texeldichte.js` (texel density),
`messen.js` / `auswerten.js` (ETC1S and UASTC quality), `normal-rg.js` (the RG11 path),
`resize.js` and `kombiniert.js` (the option table). They need `sharp` and the npm package
`basis_universal`, which ships the real `basisu.exe`.

The WebP quality the Unity pipeline uses was calibrated rather than assumed: re-encoding the twelve
images at q=75/80/85/90 and comparing against the bytes actually embedded in the GLB puts it at
**q≈85** (1008 kB against the 979 kB in the file).

## Why the model was never cached at the edge, and why the resize fixed that too

Found while measuring what the download actually costs. Over 72 hours the load balancer logged
**685 requests for the model at its digest url** — the url that carries
`Cache-Control: max-age=31536000, public, immutable`, exactly the header that exists to let Cloud
CDN hold it. Of those, 458 were complete deliveries.

**Cloud CDN filled its cache zero times.** `cacheLookup` was true for 100 % of requests, so the CDN
looked every time, went to the origin in us-central1, served the bytes through, and stored nothing.
Every player, everywhere, was downloading eleven megabytes from a single US region.

The cause is a size threshold, not a misconfiguration. Cloud CDN caches a response larger than
**10 MiB** only if the origin supports byte-range requests, so it can fill in chunks. The origin
does not: a `Range: bytes=0-1023` against `/rest/gltf/glb/1` returns `200` with the whole body, no
`Content-Range`, and the responses carry no `Accept-Ranges: bytes` — `GltfController` returns
`ResponseEntity<byte[]>`, and Spring MVC only handles ranges for a `Resource`.

The old model was **10.83 MiB**. Twenty-nine hundredths of a mebibyte over the line, and caching
silently stopped.

The evidence is a clean split, same url shape, same headers, same everything else:

| object | requests | complete | cache fills | cache hits |
|---|---|---|---|---|
| 10.83 MiB (old) | 685 over 72 h | 458 | **0** | 0 |
| 6.05 MiB (new) | 9 | 9 | **6** | 4 |

The nine are test requests made minutes after the upload; the first filled, the rest were served
from the edge with an `Age` header. Nothing about the delivery path changed — only the size.

Two consequences worth keeping:

- **The model must stay under 10 MiB.** This is an invisible cliff: nothing fails, no error is
  logged, the file simply stops being cached and every player pays the trip to us-central1 again.
  A future model that grows past it would undo this without any signal.
- **Better, remove the cliff.** Returning a `Resource` instead of `byte[]` from `getGlb` and
  `getGlbByDigest` makes Spring MVC answer range requests and advertise `Accept-Ranges: bytes`,
  after which Cloud CDN fills large objects in chunks and the size limit stops mattering. Small
  change, and it makes the caching robust rather than lucky.

## A census of everything the GPU actually holds

The work above shrank the model and left 666 MB reported from the field, which raised the obvious
question: 666 MB of what? Counted in the running game by walking
`engine.getLoadedTexturesCache()` on a 2533×1232 screen — every entry there occupies GPU memory
whoever created it.

| source | memory | textures |
|---|---|---|
| **NodeMaterial textures** | **448 MB** | 20, at 2048² and 4096² |
| **Shadow map + its depth buffer** | **192 MB** | 2, at 4096² |
| glb, environment, sprites | 148 MB | ~110, mostly 512² |
| **total** | **787 MB** | 132 |

The model everything had been about was the smallest of the three.

**The materials carried their own copies.** The same images appeared twice in memory:
`Main_forms_Red` at 2048² from `NodeMaterial: Main 3 'OWN'` and at 512² from the glb, and the same
for the normal, the metallic-roughness and the occlusion map. The NodeMaterial copies are the ones
that render, so yesterday's resize had shrunk mostly the copy that does not. Two of them were
4096².

The material data is a Babylon node-material JSON with its textures embedded as `data:` URIs, so
the same texel-density argument applies unchanged - and four times harder, because 2048 is two mip
levels above the 1024 that was already 21 texels per screen pixel on a phone.

| material | images | data | GPU |
|---|---|---|---|
| Vehicle main | 10 | 2540 kB | 341 MB |
| Building main | 4 | 502 kB | 85 MB |
| Asphalt | 2 | 477 kB | 11 MB |
| rest | 4 | 160 kB | 4 MB |

Capped at 512: **441 MB → 25 MB**, and the payload 3.59 MB → 0.98 MB.

One trap in doing it: the four Building-main images were PNG, 2048² in about 100 kB each because
they are nearly flat. Resampling puts noise into flat areas and PNG loses exactly the compression
that made them small - as 512² PNGs they came out *twice the size of the 2048² originals*. Written
as WebP they behave normally.

**The shadow map was RGBA for a value with one channel.** An exponential shadow map stores one
number per texel; Babylon allocates four channels unless told otherwise. `useRedTextureType`, the
fifth constructor argument, takes 4096² half-float from 128 MB to 32 MB. In the field a third of
all reported periods run a 4096 map, and their median texture total was 1237 MB against 657 MB for
the devices on a 1024 map.

Together: **787 MB → 275 MB**, measured the same way in the same scene.

The duplication was then not worth removing on its own. Once the material copies are 512 as well,
the second copy of each image costs 8 MB rather than the 90 it cost before - not enough to justify
touching how models resolve their materials.

### Reproducing the material resize

`C:\dev\tmp\mat-ab\` holds the originals, the shrunk versions and the two scripts.
`mat-inventur.js` lists what is embedded and what it weighs; `mat-resize.js <max>` writes the
shrunk copies. They read the files fetched from `GET /rest/babylon-material/data/{id}` and the
result goes back with `POST /rest/babylon-material/upload/{id}` (ADMIN). The scripts walk the whole
block tree for `data:` URIs rather than looking in an expected place - where a texture sits differs
by block type.
