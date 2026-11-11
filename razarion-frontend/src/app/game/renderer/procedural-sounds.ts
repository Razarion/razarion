/**
 * Unit sounds synthesised at runtime instead of loaded from the audio library: the builder arm
 * (servo out, build hum, servo in) and the drive loops. Each sound is computed sample by sample
 * into a mono Float32Array once and then handed to Babylon as an AudioBuffer, so it plays through
 * the same spatial pipeline as the sounds from the server - no download, no bytes at boot.
 *
 * Loops are made seamless in two ways: tonal parts use frequencies that complete a whole number
 * of cycles in the loop length, and the noise parts are cross-faded tail-into-head.
 */

export const PROCEDURAL_SAMPLE_RATE = 44100;

export type DriveSoundVariant = 'ground' | 'water';

/** Deterministic noise so the same sound comes out on every load. */
class Noise {
  private state: number;

  constructor(seed: number) {
    this.state = seed >>> 0 || 1;
  }

  next(): number {
    // xorshift32
    let x = this.state;
    x ^= x << 13;
    x ^= x >>> 17;
    x ^= x << 5;
    this.state = x >>> 0;
    return (this.state / 0xffffffff) * 2 - 1;
  }
}

/** Chamberlin state-variable filter, one sample at a time. */
class StateVariableFilter {
  private low = 0;
  private band = 0;
  private f = 0;
  private readonly q: number;

  constructor(frequency: number, resonance: number) {
    this.q = 1 / Math.max(resonance, 0.5);
    this.setFrequency(frequency);
  }

  setFrequency(frequency: number): void {
    this.f = 2 * Math.sin(Math.PI * Math.min(frequency, PROCEDURAL_SAMPLE_RATE / 6) / PROCEDURAL_SAMPLE_RATE);
  }

  process(input: number): { low: number, band: number, high: number } {
    this.low += this.f * this.band;
    const high = input - this.low - this.q * this.band;
    this.band += this.f * high;
    return {low: this.low, band: this.band, high};
  }
}

class OnePoleLowPass {
  private readonly a: number;
  private y = 0;

  constructor(frequency: number, sampleRate = PROCEDURAL_SAMPLE_RATE) {
    this.a = 1 - Math.exp(-2 * Math.PI * frequency / sampleRate);
  }

  process(input: number): number {
    this.y += this.a * (input - this.y);
    return this.y;
  }
}

/** Smooth random drift: low-passed noise scaled to the given standard deviation. */
class SlowWander {
  private readonly filter: OnePoleLowPass;
  private readonly scale: number;

  constructor(private readonly noise: Noise, cutoffHz: number, standardDeviation: number,
              sampleRate = PROCEDURAL_SAMPLE_RATE) {
    this.filter = new OnePoleLowPass(cutoffHz, sampleRate);
    const a = 1 - Math.exp(-2 * Math.PI * cutoffHz / sampleRate);
    // Uniform noise in [-1, 1] has variance 1/3; a one-pole keeps a / (2 - a) of it.
    this.scale = standardDeviation / Math.sqrt(a / (2 - a) / 3);
  }

  next(): number {
    return this.filter.process(this.noise.next()) * this.scale;
  }
}

// The arm sound sums ~30 partials per sample; Math.sin made a 4 s render take ~400 ms on the
// main thread. A linearly interpolated table is ~-100 dB off, far below the hiss.
const SINE_TABLE_SIZE = 4096;
const SINE_TABLE = (() => {
  const table = new Float32Array(SINE_TABLE_SIZE + 1);
  for (let i = 0; i <= SINE_TABLE_SIZE; i++) {
    table[i] = Math.sin(2 * Math.PI * i / SINE_TABLE_SIZE);
  }
  return table;
})();

/** sin(2*PI*phase) for phase in [0, 1). */
function tableSin(phase: number): number {
  const position = phase * SINE_TABLE_SIZE;
  const index = position | 0;
  const fraction = position - index;
  return SINE_TABLE[index] + (SINE_TABLE[index + 1] - SINE_TABLE[index]) * fraction;
}

function saw(phase: number): number {
  return 2 * (phase - Math.floor(phase + 0.5));
}

function normalize(samples: Float32Array, peak: number): Float32Array {
  let max = 0;
  for (let i = 0; i < samples.length; i++) {
    max = Math.max(max, Math.abs(samples[i]));
  }
  if (max > 0) {
    const gain = peak / max;
    for (let i = 0; i < samples.length; i++) {
      samples[i] *= gain;
    }
  }
  return samples;
}

/**
 * Renders `length + fade` samples with `render` and folds the extra tail over the head with an
 * equal-power cross-fade, so the last sample flows into the first.
 */
function seamless(length: number, fade: number, render: (out: Float32Array) => void): Float32Array {
  const raw = new Float32Array(length + fade);
  render(raw);
  const out = raw.slice(0, length);
  for (let i = 0; i < fade; i++) {
    const t = i / fade;
    out[i] = raw[length + i] * Math.cos(t * Math.PI / 2) + raw[i] * Math.sin(t * Math.PI / 2);
  }
  return out;
}

/**
 * Builder arm servo. `extend` sweeps up and ends in a lock clack, retract sweeps down and starts
 * with the unlock clack. The length follows the arm animation so the sound ends with the motion.
 */
// Noise levels of the arm sound, fitted band by band to the recording.
const HISS_LOW_PASS_HZ = 2500;
const HISS_GAIN = 0.9;
const RUMBLE_GAIN = 0.5;
const AIR_GAIN = 1.0;
const CONTROL_BLOCK = 32;
// Pump tone while the arm extends; retracting runs at 0.7 of it.
const ARM_EXTEND_HZ = 360;

interface ArmPartial {
  ratio: number;            // multiple of the pump tone
  fixedHz?: number;         // or a fixed frequency: housing resonances don't follow the pump
  offsetHz: number;
  level: number;            // linear, main tone = 1
  jitterHz: number;         // random drift; widens the line like in the recording
}

// The recording's "ringing", line by line (Welch spectrum 1.2-7.2 s): every harmonic is a bundle
// of lines ~8 Hz apart that beat against each other, the pump sings again at its 8th-12th
// harmonic around 6.2 kHz, and two housing resonances sit at 1238 and 1392 Hz.
const ARM_PARTIALS: ArmPartial[] = [
  {ratio: 1, offsetHz: 0, level: 1, jitterHz: 0},
  {ratio: 1, offsetHz: 4.1, level: 0.063, jitterHz: 1},
  {ratio: 1, offsetHz: -9.4, level: 0.032, jitterHz: 1},
  // Pump side lines one vane rate (tone/11) below the tone and its 2nd harmonic.
  {ratio: 10 / 11, offsetHz: 0, level: 0.04, jitterHz: 1},
  {ratio: 2 - 1 / 11, offsetHz: 0, level: 0.04, jitterHz: 1},
  {ratio: 2, offsetHz: -11, level: 0.05, jitterHz: 1},
  {ratio: 2, offsetHz: 0, level: 0.09, jitterHz: 1},
  {ratio: 2, offsetHz: 9, level: 0.06, jitterHz: 1.5},
  {ratio: 3, offsetHz: 0, level: 0.063, jitterHz: 1},
  {ratio: 3, offsetHz: 8, level: 0.028, jitterHz: 2},
  {ratio: 4, offsetHz: 0, level: 0.045, jitterHz: 1.5},
  {ratio: 4, offsetHz: 6, level: 0.035, jitterHz: 2},
  // The high singing: harmonics 8-14, each a small bundle, strongest at 11.
  {ratio: 8, offsetHz: 0, level: 0.036, jitterHz: 1},
  {ratio: 8, offsetHz: -11, level: 0.024, jitterHz: 3},
  {ratio: 9, offsetHz: 0, level: 0.048, jitterHz: 1},
  {ratio: 9, offsetHz: -13, level: 0.036, jitterHz: 3},
  {ratio: 10, offsetHz: 0, level: 0.026, jitterHz: 2},
  {ratio: 10.98, offsetHz: -16, level: 0.036, jitterHz: 3},
  {ratio: 10.98, offsetHz: -8, level: 0.044, jitterHz: 2},
  {ratio: 10.98, offsetHz: 0, level: 0.064, jitterHz: 1},
  {ratio: 10.98, offsetHz: 16, level: 0.032, jitterHz: 3},
  {ratio: 11.98, offsetHz: 0, level: 0.032, jitterHz: 1},
  {ratio: 11.98, offsetHz: 16, level: 0.036, jitterHz: 1},
  {ratio: 13, offsetHz: 0, level: 0.016, jitterHz: 1},
  {ratio: 14, offsetHz: 0, level: 0.02, jitterHz: 2},
  {ratio: 0, fixedHz: 1238, offsetHz: 0, level: 0.024, jitterHz: 1},
  {ratio: 0, fixedHz: 1386, offsetHz: 0, level: 0.04, jitterHz: 1},
  {ratio: 0, fixedHz: 1392, offsetHz: 0, level: 0.04, jitterHz: 1},
];

export function renderServo(durationSeconds: number, extend: boolean): Float32Array {
  // Hydraulic pump whine, modelled on a hydraulic press recording the player liked (analysed
  // 2026-11-06, not sampled): a nearly pure 566 Hz tone while lifting and 396 Hz while lowering,
  // weak harmonics (-25/-29/-30 dB), a faint pulse at tone/11 (an 11-vane pump, ~51 Hz) and
  // broadband noise fitted band by band (within ~2 dB up to 10 kHz). The recording's start-up and
  // valve clunk are left out; a short spin-up and spin-down keep the edges click-free.
  const duration = Math.min(Math.max(durationSeconds, 0.25), 4);
  const length = Math.round(duration * PROCEDURAL_SAMPLE_RATE);
  const out = new Float32Array(length);
  const noise = new Noise(extend ? 0x5e7f0 : 0x5e7f1);
  // Two one-pole stages: the state-variable filter turns unstable this close to Nyquist/6.
  const hissLowPassA = new OnePoleLowPass(HISS_LOW_PASS_HZ);
  const hissLowPassB = new OnePoleLowPass(HISS_LOW_PASS_HZ);
  const hissHighPass = new StateVariableFilter(600, 0.7);
  const rumbleFilter = new StateVariableFilter(260, 0.8);
  const airTop = new OnePoleLowPass(9000);
  const airTopB = new OnePoleLowPass(9000);
  const airBottom = new OnePoleLowPass(4500);
  // Above 10 kHz the recording falls off steeply.
  const topCutA = new OnePoleLowPass(11000);
  const topCutB = new OnePoleLowPass(11000);
  // Tuned down from the recording (566 / 396 Hz) on request: lifting just below the recording's
  // lowest tone, lowering in the recording's ratio (396 / 566 = 0.7).
  const f0 = extend ? ARM_EXTEND_HZ : ARM_EXTEND_HZ * 0.7;
  const pumpRate = f0 / 11;
  const spinUp = 0.08;
  const spinDown = 0.15;
  // The pump's speed wanders (+-2.3 Hz, mostly 0.5-2 Hz) and its level swells slowly (~14 %).
  // Pitch, level and line drift change slowly, so they are updated at a control rate only
  // (every CONTROL_BLOCK samples); per sample only the oscillators run.
  const controlRate = PROCEDURAL_SAMPLE_RATE / CONTROL_BLOCK;
  const pitchWander = new SlowWander(noise, 0.8, 0.004, controlRate);
  const levelWander = new SlowWander(noise, 0.4, 0.22, controlRate);
  const partialCount = ARM_PARTIALS.length;
  const partialWanders = ARM_PARTIALS.map(p => new SlowWander(noise, 4, p.jitterHz, controlRate));
  const phases = new Float64Array(partialCount);
  const increments = new Float64Array(partialCount);
  const levels = Float64Array.from(ARM_PARTIALS, p => p.level);
  let pitch = 1;
  let envelope = 1;
  let level = 1;
  for (let i = 0; i < length; i++) {
    const t = i / PROCEDURAL_SAMPLE_RATE;
    if (i % CONTROL_BLOCK === 0) {
      const tEnd = duration - t;
      // Spin-up from 75 % of the pitch (the recording climbs 415 -> 563 Hz), spin-down by 6 %.
      pitch = 1;
      envelope = 1;
      if (t < spinUp) {
        const k = t / spinUp;
        pitch = 0.75 + 0.25 * (1 - Math.pow(1 - k, 3));
        envelope = k * k;
      } else if (tEnd < spinDown) {
        const k = tEnd / spinDown;
        pitch = 0.94 + 0.06 * k;
        envelope = k;
      }
      const frequency = f0 * pitch * (1 + pitchWander.next());
      for (let p = 0; p < partialCount; p++) {
        const partial = ARM_PARTIALS[p];
        const partialFrequency = (partial.fixedHz ?? frequency * partial.ratio) + partial.offsetHz + partialWanders[p].next();
        increments[p] = partialFrequency / PROCEDURAL_SAMPLE_RATE;
      }
      level = 1 + levelWander.next();
    }
    let tone = 0;
    for (let p = 0; p < partialCount; p++) {
      let phase = phases[p] + increments[p];
      if (phase >= 1) {
        phase -= 1;
      }
      phases[p] = phase;
      tone += tableSin(phase) * levels[p];
    }
    // The pump pulsation is felt more than heard: its side lines stay below -30 dB.
    const pulse = (1 + 0.06 * Math.sin(2 * Math.PI * pumpRate * pitch * t)) * level;
    const hiss = hissHighPass.process(hissLowPassB.process(hissLowPassA.process(noise.next()))).high * HISS_GAIN;
    const rumble = rumbleFilter.process(noise.next()).low * RUMBLE_GAIN;
    // "Air": the recording stays flat up to 10 kHz and only then falls off.
    const airNoise = noise.next();
    const air = (airTop.process(airTopB.process(airNoise)) - airBottom.process(airNoise)) * AIR_GAIN;
    out[i] = topCutB.process(topCutA.process((tone * pulse + hiss + rumble + air) * envelope));
  }
  return normalize(out, 0.85);
}

/**
 * Build hum while the beam is on: a mains-like 100 Hz drone, an electric crackle band that swells
 * three times a second and the odd spark.
 */
export function renderBuildLoop(): Float32Array {
  const loopSeconds = 2;
  const length = loopSeconds * PROCEDURAL_SAMPLE_RATE;
  const fade = Math.round(0.25 * PROCEDURAL_SAMPLE_RATE);
  const out = seamless(length, fade, raw => {
    const noise = new Noise(0xb017d);
    const crackleFilter = new StateVariableFilter(3200, 2.5);
    const sparkFilter = new StateVariableFilter(5200, 6);
    const sparks: number[] = [];
    for (let s = 0; s < 7; s++) {
      sparks.push(Math.abs(noise.next()) * (raw.length / PROCEDURAL_SAMPLE_RATE));
    }
    for (let i = 0; i < raw.length; i++) {
      const t = i / PROCEDURAL_SAMPLE_RATE;
      // 100 and 200 Hz complete whole cycles in 2 s, so the drone loops on its own.
      const drone = Math.sin(2 * Math.PI * 100 * t) * 0.35 + saw((200 * t) % 1) * 0.12;
      const swell = 0.55 + 0.45 * Math.sin(2 * Math.PI * 3 * t);
      const crackle = crackleFilter.process(noise.next()).band * swell * 0.9;
      let spark = 0;
      const sparkNoise = sparkFilter.process(noise.next()).band;
      for (const start of sparks) {
        const dt = t - start;
        if (dt >= 0 && dt < 0.06) {
          spark += sparkNoise * Math.exp(-dt * 60) * 1.8;
        }
      }
      raw[i] = drone + crackle + spark;
    }
  });
  return normalize(out, 0.8);
}

/**
 * Drive loop. Ground: a deep, soft engine rumble with gravel crunch under the wheels - no track
 * clatter, that read as a kitchen mixer. Water: a deeper engine under hull rush that swells with
 * the waves. Pitch follows the unit's speed and size through the sound's playbackRate, not here.
 */
export function renderDriveLoop(variant: DriveSoundVariant): Float32Array {
  return variant === 'water' ? renderWaterDriveLoop() : renderGroundDriveLoop();
}

function renderGroundDriveLoop(): Float32Array {
  const loopSeconds = 2;
  const length = loopSeconds * PROCEDURAL_SAMPLE_RATE;
  const fade = Math.round(0.3 * PROCEDURAL_SAMPLE_RATE);
  const out = seamless(length, fade, raw => {
    const noise = new Noise(0xd71e1);
    const engineFilter = new StateVariableFilter(150, 1.1);
    const rumbleFilter = new StateVariableFilter(120, 0.8);
    const crunchFilter = new StateVariableFilter(900, 0.9);
    const crunchEnvelope = new StateVariableFilter(6, 0.7);
    // Whole cycles in 2 s, so the tonal part loops on its own.
    const base = 38;
    for (let i = 0; i < raw.length; i++) {
      const t = i / PROCEDURAL_SAMPLE_RATE;
      // Mostly sine with a little saw on top: body without the buzz.
      const firing = Math.sin(2 * Math.PI * base * t) * 0.8
        + Math.sin(2 * Math.PI * base * 2 * t) * 0.25
        + saw((base * t) % 1) * 0.2;
      // A slow, irregular-feeling throb (1.5 and 2.5 Hz -> 3 and 5 cycles per loop).
      const throb = 0.8 + 0.12 * Math.sin(2 * Math.PI * 1.5 * t) + 0.08 * Math.sin(2 * Math.PI * 2.5 * t + 1);
      const engine = engineFilter.process(firing).low * throb;
      const rumble = rumbleFilter.process(noise.next()).low * 2.2;
      // Gravel: mid noise whose loudness wanders slowly.
      const crunchLevel = 0.5 + Math.abs(crunchEnvelope.process(noise.next()).low) * 40;
      const crunch = crunchFilter.process(noise.next()).band * Math.min(crunchLevel, 1.2) * 0.18;
      raw[i] = engine * 1.3 + rumble + crunch;
    }
  });
  return normalize(out, 0.8);
}

function renderWaterDriveLoop(): Float32Array {
  const loopSeconds = 1;
  const length = loopSeconds * PROCEDURAL_SAMPLE_RATE;
  const fade = Math.round(0.15 * PROCEDURAL_SAMPLE_RATE);
  const out = seamless(length, fade, raw => {
    const noise = new Noise(0xd71e2);
    const engineFilter = new StateVariableFilter(260, 1.5);
    const rushFilter = new StateVariableFilter(700, 0.8);
    const base = 42; // whole cycles per second -> loops on its own
    for (let i = 0; i < raw.length; i++) {
      const t = i / PROCEDURAL_SAMPLE_RATE;
      const firing = saw((base * t) % 1) * 0.8 + saw((base * 2 * t) % 1) * 0.3
        + Math.sin(2 * Math.PI * base / 2 * t) * 0.35;
      const engine = engineFilter.process(firing + noise.next() * 0.15).low;
      const waves = 0.6 + 0.4 * Math.sin(2 * Math.PI * 2 * t);
      raw[i] = engine * 1.4 + rushFilter.process(noise.next()).band * waves * 1.2;
    }
  });
  return normalize(out, 0.8);
}
