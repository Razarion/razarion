import {Color3, Mesh, MeshBuilder, Nullable, StandardMaterial, TransformNode} from "@babylonjs/core";
import {Observer} from "@babylonjs/core/Misc/observable";
import {Scene} from "@babylonjs/core/scene";

/**
 * Which command the marker acknowledges. Only the colour differs - the pulse itself is the same
 * gesture, so the player learns one shape instead of two.
 */
export type CommandTargetKind = 'attack' | 'harvest';

/**
 * The short pulse that answers "yes, that one" when a command is sent at a target: two rings
 * that snap inwards onto the clicked enemy unit or razarion spot and fade out.
 *
 * Attack and harvest orders used to be acknowledged by sound alone. The unit itself often needs
 * seconds to react - it first has to drive there - so until it moved there was nothing on screen
 * saying the click had been understood, or which of several overlapping targets had been hit.
 *
 * The rings hang off the target's own node, so they follow a target that is moving away and die
 * with a target that is destroyed.
 */
export class CommandTargetMarker {
  private static readonly RING_COUNT = 2;
  /** Head start of the first ring over the second, giving the pulse its double beat. */
  private static readonly RING_DELAY_MS = 160;
  private static readonly LIFETIME_MS = 620;
  /** Rings start well outside the target and close in on it. Multiples of the target radius. */
  private static readonly START_SCALE = 2.1;
  private static readonly END_SCALE = 0.95;
  /** Fraction of the lifetime a ring stays at full strength before it starts fading. */
  private static readonly OPAQUE_FRACTION = 0.35;
  private static readonly MAX_ALPHA = 0.9;
  /** Lifted off the target's origin so the ring does not fight the ground it sits on. */
  private static readonly HEIGHT = 0.1;

  private readonly rings: Mesh[] = [];
  private readonly materials: StandardMaterial[] = [];
  private observer: Nullable<Observer<Scene>> = null;
  private elapsed = 0;
  private disposed = false;

  constructor(private readonly scene: Scene,
              private readonly target: TransformNode,
              radius: number,
              kind: CommandTargetKind) {
    const color = CommandTargetMarker.color(kind);
    // Scales with the target so a razarion spot gets a fine ring and a building a bolder one,
    // capped before it turns into a doughnut around the biggest bases.
    const thickness = Math.min(0.22, Math.max(0.12, radius * 0.09));
    for (let i = 0; i < CommandTargetMarker.RING_COUNT; i++) {
      const ring = MeshBuilder.CreateTorus(`Command target ring ${i}`,
        {diameter: radius * 2, thickness, tessellation: 40}, scene);
      const material = new StandardMaterial(`Command target ring material ${i}`, scene);
      // Emissive only: the marker has to read the same at night, in shadow and on bot ground.
      material.disableLighting = true;
      material.emissiveColor = color;
      material.diffuseColor = Color3.Black();
      material.specularColor = Color3.Black();
      material.backFaceCulling = false;
      material.alpha = 0;
      ring.material = material;
      ring.isPickable = false;
      ring.parent = target;
      ring.position.y = CommandTargetMarker.HEIGHT;
      // Same reasoning as the selection brackets: drawn after the scene so the ring survives
      // terrain bumps and the target's own hull instead of disappearing into them.
      ring.renderingGroupId = 1;
      ring.isVisible = false;
      this.rings.push(ring);
      this.materials.push(material);
    }
    this.observer = scene.onBeforeRenderObservable.add(() => this.update());
  }

  private static color(kind: CommandTargetKind): Color3 {
    return kind === 'attack' ? new Color3(1, 0.25, 0.18) : new Color3(0.3, 0.8, 1);
  }

  getTarget(): TransformNode {
    return this.target;
  }

  isDisposed(): boolean {
    return this.disposed;
  }

  private update(): void {
    if (this.disposed) {
      return;
    }
    // The target can die under the marker - it is an enemy being shot at, after all. Its node
    // takes the rings with it, so stop touching them.
    if (this.target.isDisposed()) {
      this.dispose();
      return;
    }
    this.elapsed += this.scene.getEngine().getDeltaTime();
    let anyAlive = false;
    for (let i = 0; i < this.rings.length; i++) {
      const ring = this.rings[i];
      const t = (this.elapsed - i * CommandTargetMarker.RING_DELAY_MS) / CommandTargetMarker.LIFETIME_MS;
      if (t < 0) {
        anyAlive = true;
        continue;
      }
      if (t >= 1) {
        ring.isVisible = false;
        continue;
      }
      anyAlive = true;
      const eased = 1 - Math.pow(1 - t, 3);
      const scale = CommandTargetMarker.START_SCALE
        + (CommandTargetMarker.END_SCALE - CommandTargetMarker.START_SCALE) * eased;
      ring.scaling.set(scale, scale, scale);
      const fade = t < CommandTargetMarker.OPAQUE_FRACTION
        ? 1
        : 1 - (t - CommandTargetMarker.OPAQUE_FRACTION) / (1 - CommandTargetMarker.OPAQUE_FRACTION);
      this.materials[i].alpha = CommandTargetMarker.MAX_ALPHA * fade;
      ring.isVisible = true;
    }
    if (!anyAlive) {
      this.dispose();
    }
  }

  dispose(): void {
    if (this.disposed) {
      return;
    }
    this.disposed = true;
    if (this.observer) {
      this.scene.onBeforeRenderObservable.remove(this.observer);
      this.observer = null;
    }
    this.rings.forEach(ring => ring.dispose());
    this.materials.forEach(material => material.dispose());
    this.rings.length = 0;
    this.materials.length = 0;
  }
}
