# TeaVM 0.16.0-dev: what the upgrade costs, measured

Measured 2026-09-10 against this checkout at `664095393`. Nothing here is committed as a version
change — the module poms are still on `0.15.0`.

## The question

`0.16.0-dev-4` produces a bigger WASM than `0.15.0`, consistently. Worth knowing where the bytes
come from, whether they cost runtime, and whether the upgrade is worth it — the other side being
about a dozen WASM-GC codegen fixes since 0.15.0, among them two type-stack bugs in the coroutine
transformation and an allocation bug on a full heap.

## Where the bytes come from: entirely between dev-1 and dev-2

Both modules, dev profile (SIMPLE) and prod profile (FULL + minify), built in this checkout:

| Version | client prod | Δ | worker prod | Δ |
|---|---|---|---|---|
| 0.15.0 | 613'020 | – | 728'395 | – |
| 0.16.0-dev-1 | 613'020 | **+0** | 728'395 | **+0** |
| 0.16.0-dev-2 | 665'391 | +52'371 | 774'307 | +45'912 |
| 0.16.0-dev-4 | 665'851 | +460 | 777'191 | +2'884 |

**dev-1 is byte-identical to 0.15.0** — same SHA, both profiles, both modules. dev-3 and dev-4
together add under 3.5 kB. The whole step is dev-1 → dev-2.

The version override does take effect: `mvn help:evaluate -Dexpression=teavm.version` answers
`0.16.0-dev-1`, and the dev-1 plugin jar is in `.m2`. dev-1 genuinely emits identical code.

## What changed: not the class library

Comparing every TeaVM artifact between dev-1 and dev-2:

```
teavm-wasm-gc-deobfuscator   +2'452     (not compiled into our WASM)
teavm-classlib                 +799
teavm-core                     +143
everything else                   0
```

And inside `teavm-classlib`: **1575 entries before, 1575 after. Zero new classes, zero removed.**
Seven changed, led by `TArrays.class` at +1057 bytes.

So 799 bytes of library change produce 135'657 bytes of WASM. A factor of 170. That is not a
bigger library — it is code generation.

**The "new classlib surface (System.Logger)" hypothesis is refuted**: there is no new classlib
surface at all.

## What it actually is: per-lambda type metadata

Both dev builds carry the WASM name section, so the emitted function names can be counted
directly. dev-1 → dev-2, distinct identifiers:

| | client | worker |
|---|---|---|
| `lambda$` methods | +342 | +210 |
| `com.btxtech` total | +549 | +327 |
| `java.util.function` | +8 | +12 |
| `java.util.stream` | +16 | +5 |
| **new identifiers total** | **830** | **606** |
| of those lambda-shaped | 368 (44 %) | 253 (42 %) |
| `arraycopy` | **+0** | **+0** |

830 new functions for 135'657 bytes is about 165 bytes each — an ordinary function. And the new
names say what they are:

```
com.btxtech.client.bridge.DtoConverter$lambda$convertColdGameUiContext$41$lambda$_157_0@isSupertypes
com.btxtech.client.bridge.DtoConverter$lambda$convertColdGameUiContext$41$lambda$_157_1@isSupertypes
... _157_2 through _157_5
```

`@isSupertypes` is a per-class type-check helper. **dev-2 emits one for every lambda class where
dev-1 did not**, and nested lambdas multiply — six from a single method above.

That is an inference from the name section, not from reading TeaVM's source. What is certain: the
growth is lambda type metadata, not library code, and not `System.arraycopy` — the arraycopy count
is unchanged at 1, so the `TArrays` change (the one the release notes advertise as a performance
fix) is *not* what grew the output.

## What it costs over the wire

gzip level 9, which is what the server uses for `application/wasm`:

```
0.15.0        1'341'415 B raw   ->   462'556 B gzipped
0.16.0-dev-4  1'443'042 B raw   ->   492'928 B gzipped
                    +101'627           +30'372   (+6.6 %)
```

The method checks out against production: PROD currently delivers 209'165 + 250'808 = 459'973 B,
against 462'556 B computed here — 0.6 % apart.

**Against what a start actually downloads, +30 kB is noise.** One model
(`/rest/gltf/glb/1`) is delivered at 11'351'892 B, uncompressed. The whole upgrade costs 0.27 % of
that one file, and the startup budget is around 19 MB of content across eight endpoints. The
startup work of the last weeks found the cost in content, not in code — see
`project_startup_byte_budget_2026_09`. This does not move that needle in either direction.

## Runtime: not measured, and here is what it would take

Deliberately left open rather than guessed at.

**A trap worth naming first**: `PlanetServiceTracker` and `PathingServiceTracker` live in
`PlanetService`, which runs in *both* engine modes. Measuring them on the server measures the
**JVM** and is completely blind to a TeaVM change. Only the worker (SLAVE) path is WASM — and the
load there is not comparable either, since the slave never replans and only predicts.

The usable client-side number already exists on every telemetry line: **`tickGapP50`**, the
interval at which the worker delivers ticks. Target 100 ms; PROD median today is 110 ms. That is
worker throughput and therefore directly TeaVM. `tickApplyP50` sits beside it but measures the
main thread, not the worker.

Two changes since 0.15.0 could plausibly help the engine — `System.arraycopy` array-to-array, and
EnumSet iteration plus `getEnumConstants`, which is behind every `enum.values()`. The game engine
has plenty of those. None of it is measured here.

## A risk the size question hides

The client already fails to compile on some browsers. Seven days of PROD, 179 dead starts in
`WASM_LOAD`:

```
66  CompileError
    40  the browser rejects the GC types        (probe already predicts these)
    26  the browser rejects the GC instructions (Invalid opcode 0xfb)
```

An upgrade changes the emitted instruction mix — 830 new functions is not a small change to it.
It could win those 26 back or lose new ones, and nothing in a size comparison sees it.

**This is the one dimension with real downside, and it is measurable after the fact**: the rate of
`WASM_LOAD` with `CompileError` against total sessions. A week of it settles the question.

## Recommendation

**Size is not a reason to decline.** +30 kB over the wire, 0.27 % of one model, on a startup whose
cost is content. Anyone weighing this against the codegen fixes should treat the size column as
approximately zero.

**Do it staged, and watch the right number.** Deploy, then read `WASM_LOAD` CompileError rate for a
week against the 66/1039 baseline above, and `tickGapP50` against today's 110 ms median. Both come
from instrumentation that already exists; neither needs anything built.

**One caveat that is not technical.** The dev builds are unsigned artifacts on the project's own
host, not Central. Putting that repository in the resolution path of a production build is a
deliberate decision, not a side effect.

## Reproducing this

The repository block is not in `pom.xml` — it was added for the measurement and reverted. To
repeat, add to the root pom and bump `<teavm.version>` in both TeaVM module poms:

```xml
<repositories>
    <repository>
        <id>teavm-dev</id>
        <url>https://teavm.org/maven/repository</url>
        <releases><enabled>true</enabled></releases>
        <snapshots><enabled>false</enabled></snapshots>
    </repository>
</repositories>
<!-- and the same block as <pluginRepositories>/<pluginRepository> -->
```

Then, per version — no `clean`, no frontend, about 15 s each:

```
mvn install -DskipTests -B -pl razarion-client-teavm,razarion-client-worker-teavm "-Dteavm.version=0.16.0-dev-2"
```

Add `-P prod` for the numbers that matter.

**Quote every `-D` argument.** PowerShell parses `-Dteavm.version=x` as `-Dteavm` plus a lifecycle
phase `.version=x` and the build dies.

**Rebuild 0.15.0 without `-P prod` afterwards.** `deploy.ps1` builds from the working tree, so
leaving a dev-4 prod artifact in `generated/teavm-*` ships it. Target sizes: client 1'425'671,
worker 1'345'204.

Scripts used are in this session's scratchpad: `teavm-messreihe.ps1` (build + archive per version),
`teavm-auswertung.js` (sizes and gzip), `teavm-artefakt-diff.js` (which TeaVM jar grew, and which
classlib entries), `wasm-namen-diff.js` and `wasm-lambda.js` (the name-section comparison).
