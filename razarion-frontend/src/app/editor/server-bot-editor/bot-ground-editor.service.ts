import {Injectable} from '@angular/core';
import {BabylonRenderServiceAccessImpl} from '../../game/renderer/babylon-render-service-access-impl.service';
import {Color3, Matrix, Mesh, MeshBuilder} from '@babylonjs/core';
import {BabylonTerrainTileImpl} from '../../game/renderer/babylon-terrain-tile.impl';
import {BotConfig, DecimalPosition} from '../../generated/razarion-share';
import {SimpleMaterial} from '@babylonjs/materials';
import {Observer} from '@babylonjs/core/Misc/observable';
import {Nullable} from '@babylonjs/core/types';
import type {PointerInfo} from '@babylonjs/core/Events/pointerEvents';

@Injectable({
  providedIn: 'root'
})
export class BotGroundEditorService {
  private height = 0;
  private groundBoxPositions = new Map<DecimalPosition, Mesh>();
  private cursor: Nullable<Mesh> = null;
  private botConfig: BotConfig | null = null;
  private pointerObservable: Nullable<Observer<PointerInfo>> = null;

  constructor(private renderer: BabylonRenderServiceAccessImpl) {
    this.renderer.disableSelectionFrame();
  }

  activate(botConfig: BotConfig) {
    this.height = botConfig.groundBoxHeight === null ? 0 : botConfig.groundBoxHeight;
    this.setupBoxes(botConfig.groundBoxPositions);
    this.botConfig = botConfig;

    this.cursor = MeshBuilder.CreateBox("Bot ground editor", {size: BabylonTerrainTileImpl.BOT_BOX_LENGTH}, this.renderer.getScene());
    this.pointerObservable = this.renderer.getScene().onPointerObservable.add((pointerInfo) => {
      let ray = this.renderer.getScene().createPickingRay(this.renderer.getScene().pointerX, this.renderer.getScene().pointerY, Matrix.Identity(), null);
      const t = (this.height - ray.origin.y) / ray.direction.y;
      const hitPoint = ray.origin.add(ray.direction.scale(t));
      const terrainX = Math.floor(hitPoint.x);
      const terrainY = Math.floor(hitPoint.z);
      this.cursor!.position.x = terrainX;
      this.cursor!.position.y = this.height - BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2 + 0.23;
      this.cursor!.position.z = terrainY;
      this.cursor!.material = new SimpleMaterial("Bot ground editor");
      (<SimpleMaterial>this.cursor!.material).diffuseColor = new Color3(0.8, 0.6, 0);

      const newDecimalPosition = {x: terrainX, y: terrainY};
      if (pointerInfo.event.buttons & 1) {
        const existing = Array.from(this.groundBoxPositions.entries())
          .find(([pos]) => this.checkSquaresOverlap(pos, newDecimalPosition));
        if (!existing) {
          this.groundBoxPositions.set(newDecimalPosition, this.setupBox(newDecimalPosition));
          this.updateBotConfig();
        }
      } else if (pointerInfo.event.buttons & 2) {
        const existing = Array.from(this.groundBoxPositions.entries())
          .find(([pos]) => this.checkSquaresOverlap(pos, newDecimalPosition));
        if (existing) {
          const [pos, mesh] = existing;
          mesh.dispose();
          this.groundBoxPositions.delete(pos);
          this.updateBotConfig();
        }
      }
    });
  }

  deactivate(botConfig: BotConfig) {
    if (!this.botConfig) {
      return;
    }
    if (botConfig.id === this.botConfig.id) {
      this.disposeBoxes();
      if (this.cursor) {
        this.cursor.dispose();
        this.cursor = null;
      }
      if (this.pointerObservable) {
        this.pointerObservable.remove();
        this.pointerObservable = null;
      }
      this.botConfig = null;
    }
  }

  public setHeight(height: number) {
    this.height = height;
    Array.from(this.groundBoxPositions.values()).forEach((mesh) => {
      mesh.position.y = this.height - BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2 + 0.22;
    });
  }

  private setupBoxes(groundBoxPositions: DecimalPosition[]) {
    this.disposeBoxes();
    if (!groundBoxPositions) {
      return;
    }
    groundBoxPositions.forEach(groundBoxPosition => {
      this.groundBoxPositions.set(groundBoxPosition, this.setupBox(groundBoxPosition));
    })
  }

  private disposeBoxes() {
    Array.from(this.groundBoxPositions.values()).forEach(box => box.dispose());
    this.groundBoxPositions.clear();
  }

  private checkSquaresOverlap(middleA: DecimalPosition, middleB: DecimalPosition): boolean {
    const aEndX = middleA.x + BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2;
    const aEndY = middleA.y + BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2;
    const bEndX = middleB.x + BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2;
    const bEndY = middleB.y + BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2;

    const noOverlap =
      aEndX <= middleB.x - BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2 ||
      bEndX <= middleA.x - BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2 ||
      aEndY <= middleB.y - BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2 ||
      bEndY <= middleA.y - BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2;

    return !noOverlap;
  }

  private updateBotConfig() {
    this.botConfig!.groundBoxPositions = Array.from(this.groundBoxPositions.keys());
  }

  private setupBox(decimalPosition: DecimalPosition) {
    const box = MeshBuilder.CreateBox("Bot ground editor", {size: BabylonTerrainTileImpl.BOT_BOX_LENGTH}, this.renderer.getScene());
    box.position.x = decimalPosition.x;
    box.position.y = this.height - BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2 + 0.22;
    box.position.z = decimalPosition.y;
    return box;
  }
}
