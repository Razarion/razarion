import {AbstractMesh, Mesh, Ray, Vector3} from "@babylonjs/core";
import {Scene} from "@babylonjs/core/scene";
import {BabylonRenderServiceAccessImpl, RazarionMetadataType} from "./babylon-render-service-access-impl.service";

// Kinematic "blown apart" simulation — each building chunk gets its own outward+upward
// impulse, angular velocity, gravity, and ground bounce, so parts tumble naturally
// instead of translating linearly (which is what Babylon's MeshExploder does).
// Modelled loosely on Command & Conquer Generals Zero Hour's CreateDebris behavior:
// ~40 chunks, Disposition=SEND_IT_FLYING, Mass=40, random direction, ground bounce.
//
// The caller supplies already-detached world-space meshes. After the tumble duration
// elapses, `onSettled()` is invoked once with the final (post-physics) meshes so the
// caller can hand them off to BabylonWreckage for scorching/fade-out.
interface DebrisBody {
  mesh: Mesh;
  vel: Vector3;
  angVel: Vector3;
  landed: boolean;
}

export class BabylonBuildingDebris {
  private static readonly TUMBLE_DURATION_MS = 1600;
  private static readonly GRAVITY = -40;          // units / s²
  private static readonly MIN_UPWARD_VEL = 6;
  private static readonly MAX_UPWARD_VEL = 14;
  private static readonly OUTWARD_VEL_BASE = 3;   // minimum horizontal speed
  private static readonly OUTWARD_VEL_RADIUS_SCALE = 1.4; // per unit of offset
  private static readonly ANGULAR_VEL_MAX = Math.PI * 5;  // rad / s on each axis
  private static readonly HORIZONTAL_DRAG = 0.82; // per-second factor
  private static readonly GROUND_BOUNCE = 0.28;   // vertical restitution
  private static readonly GROUND_FRICTION = 0.55; // horizontal speed scale on bounce

  static tumble(
    scene: Scene,
    meshes: Mesh[],
    center: Vector3,
    onSettled: (meshes: Mesh[]) => void,
  ): void {
    if (meshes.length === 0) {
      onSettled(meshes);
      return;
    }

    const groundY = BabylonBuildingDebris.findGroundY(scene, center);
    const bodies: DebrisBody[] = meshes.map(m => BabylonBuildingDebris.seedBody(m, center));

    const startMs = Date.now();
    const onBeforeRender = () => {
      const dt = Math.min(scene.getEngine().getDeltaTime() / 1000, 0.05); // clamp: avoid huge step on stutter
      const elapsed = Date.now() - startMs;

      for (const b of bodies) {
        if (b.landed) continue;

        b.mesh.position.addInPlace(b.vel.scale(dt));
        b.vel.y += BabylonBuildingDebris.GRAVITY * dt;

        const drag = Math.pow(BabylonBuildingDebris.HORIZONTAL_DRAG, dt);
        b.vel.x *= drag;
        b.vel.z *= drag;

        if (b.mesh.position.y <= groundY + 0.05) {
          b.mesh.position.y = groundY + 0.05;
          if (b.vel.y < 0) b.vel.y = -b.vel.y * BabylonBuildingDebris.GROUND_BOUNCE;
          b.vel.x *= BabylonBuildingDebris.GROUND_FRICTION;
          b.vel.z *= BabylonBuildingDebris.GROUND_FRICTION;
          b.angVel.scaleInPlace(0.6);

          // Flag as landed once motion is negligible — stops integrating for cheap perf.
          if (b.vel.lengthSquared() < 0.4 && Math.abs(b.vel.y) < 0.5) {
            b.landed = true;
          }
        }

        b.mesh.rotation.x += b.angVel.x * dt;
        b.mesh.rotation.y += b.angVel.y * dt;
        b.mesh.rotation.z += b.angVel.z * dt;
      }

      if (elapsed >= BabylonBuildingDebris.TUMBLE_DURATION_MS) {
        scene.onBeforeRenderObservable.removeCallback(onBeforeRender);
        onSettled(meshes);
      }
    };
    scene.onBeforeRenderObservable.add(onBeforeRender);
  }

  // Give a single mesh an outward+upward initial velocity based on its offset from the
  // explosion centre, plus random angular velocity. Meshes directly above the centre
  // (zero horizontal offset) get a random lateral direction so they don't shoot straight up.
  private static seedBody(mesh: Mesh, center: Vector3): DebrisBody {
    // Quaternion → Euler conversion: physics uses rotation.xyz, so discard any quaternion.
    mesh.rotationQuaternion = null;
    if (!mesh.rotation) mesh.rotation = Vector3.Zero();

    const offset = mesh.absolutePosition.subtract(center);
    offset.y = 0;
    const dist = offset.length();

    let dirX: number, dirZ: number;
    if (dist > 0.01) {
      dirX = offset.x / dist;
      dirZ = offset.z / dist;
    } else {
      const a = Math.random() * Math.PI * 2;
      dirX = Math.cos(a);
      dirZ = Math.sin(a);
    }

    const horiz =
      BabylonBuildingDebris.OUTWARD_VEL_BASE +
      dist * BabylonBuildingDebris.OUTWARD_VEL_RADIUS_SCALE +
      Math.random() * 2;

    // Jitter the direction slightly so the debris doesn't fan out too neatly.
    const jitter = (Math.random() - 0.5) * 0.6;
    const cj = Math.cos(jitter), sj = Math.sin(jitter);
    const jx = dirX * cj - dirZ * sj;
    const jz = dirX * sj + dirZ * cj;

    const upSpeed =
      BabylonBuildingDebris.MIN_UPWARD_VEL +
      Math.random() * (BabylonBuildingDebris.MAX_UPWARD_VEL - BabylonBuildingDebris.MIN_UPWARD_VEL);

    const vel = new Vector3(jx * horiz, upSpeed, jz * horiz);
    const angVel = new Vector3(
      (Math.random() - 0.5) * 2 * BabylonBuildingDebris.ANGULAR_VEL_MAX,
      (Math.random() - 0.5) * 2 * BabylonBuildingDebris.ANGULAR_VEL_MAX,
      (Math.random() - 0.5) * 2 * BabylonBuildingDebris.ANGULAR_VEL_MAX,
    );

    return {mesh, vel, angVel, landed: false};
  }

  private static findGroundY(scene: Scene, center: Vector3): number {
    const ray = new Ray(new Vector3(center.x, 100, center.z), new Vector3(0, -1, 0), 200);
    const pick = scene.pickWithRay(ray, (m: AbstractMesh) => {
      const meta = BabylonRenderServiceAccessImpl.getRazarionMetadata(m);
      if (!meta) return false;
      return meta.type === RazarionMetadataType.GROUND || meta.type === RazarionMetadataType.BOT_GROUND;
    });
    return pick?.pickedPoint?.y ?? center.y - 1;
  }
}
