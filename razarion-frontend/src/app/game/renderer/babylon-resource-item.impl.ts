import {BabylonItemImpl} from "./babylon-item.impl";
import {
  BabylonResourceItem,
  Diplomacy,
  MarkerConfig,
  ResourceItemType,
  Vertex
} from "../../gwtangular/GwtAngularFacade";
import {BabylonRenderServiceAccessImpl} from "./babylon-render-service-access-impl.service";
import {BabylonModelService} from "./babylon-model.service";
import {ActionService} from "../action.service";
import {UiConfigCollectionService} from "../ui-config-collection.service";
import {SelectionService as TsSelectionService} from "../selection.service";
import {Color4, ParticleSystem, RawTexture, Vector3} from "@babylonjs/core";

export class BabylonResourceItemImpl extends BabylonItemImpl implements BabylonResourceItem {
  private sparkleParticleSystem: ParticleSystem | null = null;
  private sparkleEmitter: Vector3 = new Vector3(0, 0, 0);
  private resourceRadius: number = 1;

  constructor(id: number,
              resourceItemType: ResourceItemType,
              rendererService: BabylonRenderServiceAccessImpl,
              actionService: ActionService,
              tsSelectionService: TsSelectionService,
              babylonModelService: BabylonModelService,
              uiConfigCollectionService: UiConfigCollectionService,
              disposeCallback: ((permanent: boolean) => void) | null) {
    super(id,
      resourceItemType,
      Diplomacy.RESOURCE,
      rendererService,
      babylonModelService,
      uiConfigCollectionService,
      actionService,
      tsSelectionService,
      rendererService.resourceItemContainer,
      disposeCallback);
    this.resourceRadius = resourceItemType.getRadius();
    this.updateItemCursor();
    this.createSparkleEffect(rendererService);
  }

  override setPosition(position: Vertex): void {
    super.setPosition(position);
    if (position) {
      this.sparkleEmitter.x = position.getX();
      this.sparkleEmitter.y = position.getZ();
      this.sparkleEmitter.z = position.getY();
    }
  }

  private createSparkleEffect(rendererService: BabylonRenderServiceAccessImpl): void {
    const scene = rendererService.getScene();
    const radius = this.resourceRadius;
    const ps = new ParticleSystem("ResourceSparkle", 200, scene);

    // Create tiny sparkle dust texture via RawTexture (synchronous)
    const size = 32;
    const data = new Uint8Array(size * size * 4);
    const center = size / 2;
    for (let y = 0; y < size; y++) {
      for (let x = 0; x < size; x++) {
        const dx = (x - center) / center;
        const dy = (y - center) / center;
        const dist = Math.sqrt(dx * dx + dy * dy);
        // Sharp bright core with rapid falloff — tiny glinting speck
        const core = Math.exp(-dist * dist * 12.0);
        // Faint cross flare for sparkle glint
        const flareX = Math.exp(-dy * dy * 20.0) * Math.exp(-dx * dx * 2.0);
        const flareY = Math.exp(-dx * dx * 20.0) * Math.exp(-dy * dy * 2.0);
        const flare = Math.max(flareX, flareY) * 0.4;
        const brightness = Math.min(1, core + flare);
        const alpha = brightness * (dist < 1.0 ? 1 : 0);
        const idx = (y * size + x) * 4;
        data[idx] = Math.round(220 + 35 * core);
        data[idx + 1] = Math.round(230 + 25 * core);
        data[idx + 2] = 255;
        data[idx + 3] = Math.round(alpha * 255);
      }
    }
    ps.particleTexture = RawTexture.CreateRGBATexture(data, size, size, scene, false, false);

    ps.emitter = this.sparkleEmitter;
    ps.minEmitBox = new Vector3(-radius * 0.8, 0.2, -radius * 0.8);
    ps.maxEmitBox = new Vector3(radius * 0.8, radius * 1.2, radius * 0.8);

    ps.direction1 = new Vector3(-1.0, 0.2, -1.0);
    ps.direction2 = new Vector3(1.0, 0.8, 1.0);
    ps.gravity = new Vector3(0, -0.1, 0);

    ps.color1 = new Color4(0.85, 0.92, 1.0, 1.0);
    ps.color2 = new Color4(0.7, 0.85, 1.0, 1.0);
    ps.colorDead = new Color4(0.6, 0.75, 1.0, 0.0);

    ps.minSize = 0.08;
    ps.maxSize = 0.3;
    ps.minLifeTime = 1.5;
    ps.maxLifeTime = 3.5;

    ps.emitRate = 30;
    ps.blendMode = ParticleSystem.BLENDMODE_ADD;

    ps.minEmitPower = 0.3;
    ps.maxEmitPower = 0.8;

    ps.start();
    this.sparkleParticleSystem = ps;
  }

  override dispose(): void {
    this.disposeSparkle();
    super.dispose();
  }

  override removeFromView(): void {
    this.disposeSparkle();
    super.removeFromView();
  }

  private disposeSparkle(): void {
    if (this.sparkleParticleSystem) {
      this.sparkleParticleSystem.stop();
      this.sparkleParticleSystem.dispose();
      this.sparkleParticleSystem = null;
    }
  }

  public static createDummy(id: number): BabylonResourceItem {
    return new class implements BabylonResourceItem {
      dispose(): void {
      }

      removeFromView(): void {
      }

      getAngle(): number {
        return 0;
      }

      getId(): number {
        return id;
      }

      getPosition(): Vertex | null {
        return null;
      }

      hover(active: boolean): void {
      }

      mark(markerConfig: MarkerConfig | null): void {
      }

      select(active: boolean): void {
      }

      setAngle(angle: number): void {
      }

      setPosition(position: Vertex): void {
      }

      isEnemy(): boolean {
        return false;
      }
    };
  }
}
