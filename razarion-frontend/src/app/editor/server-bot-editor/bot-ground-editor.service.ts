import {Injectable} from '@angular/core';
import {BabylonRenderServiceAccessImpl} from '../../game/renderer/babylon-render-service-access-impl.service';
import {Color3, Matrix, Mesh, MeshBuilder} from '@babylonjs/core';
import {BabylonTerrainTileImpl} from '../../game/renderer/babylon-terrain-tile.impl';
import {BotConfig, DecimalPosition} from '../../generated/razarion-share';
import {SimpleMaterial} from '@babylonjs/materials';
import {Observer} from '@babylonjs/core/Misc/observable';
import {Nullable} from '@babylonjs/core/types';
import type {PointerInfo} from '@babylonjs/core/Events/pointerEvents';
import {toRadians} from 'chart.js/helpers';
import {TransformNode} from '@babylonjs/core/Meshes/transformNode';

@Injectable({
  providedIn: 'root'
})
export class BotGroundEditorService {
  private height = 0;
  private groundBoxPositions = new Map<DecimalPosition, Mesh>();
  private container: Nullable<TransformNode> = null;
  private cursor: Nullable<Mesh> = null;
  private botConfig: BotConfig | null = null;
  private pointerObservable: Nullable<Observer<PointerInfo>> = null;
  private slopeMode = false;
  static readonly EDITOR_BOX_Y = 0.22;
  static readonly EDITOR_CURSOR_Y = 0.23;

  constructor(private renderer: BabylonRenderServiceAccessImpl) {
    this.renderer.disableSelectionFrame();
  }

  activate(botConfig: BotConfig) {
    this.height = botConfig.groundBoxHeight === null ? 0 : botConfig.groundBoxHeight;
    this.container = new TransformNode("Bot ground editor");
    this.setupBoxes(botConfig.groundBoxPositions);
    this.botConfig = botConfig;

    this.cursor = MeshBuilder.CreateBox("Cursor", {size: BabylonTerrainTileImpl.BOT_BOX_LENGTH}, this.renderer.getScene());
    this.cursor.parent = this.container;
    this.pointerObservable = this.renderer.getScene().onPointerObservable.add((pointerInfo) => {
      let ray = this.renderer.getScene().createPickingRay(this.renderer.getScene().pointerX, this.renderer.getScene().pointerY, Matrix.Identity(), null);
      const t = (this.height - ray.origin.y) / ray.direction.y;
      const hitPoint = ray.origin.add(ray.direction.scale(t));
      const terrainX = Math.floor(hitPoint.x);
      const terrainY = Math.floor(hitPoint.z);
      if (this.slopeMode) {
        const slopeAngle = toRadians(BabylonTerrainTileImpl.BOT_BOX_Z_ROTATION);
        const h = Math.sqrt(2) / 2 * BabylonTerrainTileImpl.BOT_BOX_LENGTH;
        this.cursor!.position.y = this.height - h * Math.sin(toRadians(45) + slopeAngle) + BotGroundEditorService.EDITOR_CURSOR_Y;
        this.cursor!.rotation.z = slopeAngle;
        const yRot = this.cursor!.rotation.y % (2 * Math.PI);
        if (yRot < toRadians(90)) {
          this.cursor!.position.x = terrainX - (h * Math.cos(toRadians(45) + slopeAngle));
          this.cursor!.position.z = terrainY;
        } else if (yRot < toRadians(180)) {
          this.cursor!.position.x = terrainX;
          this.cursor!.position.z = terrainY + (h * Math.cos(toRadians(45) + slopeAngle));
        } else if (yRot < toRadians(270)) {
          this.cursor!.position.x = terrainX + (h * Math.cos(toRadians(45) + slopeAngle));
          this.cursor!.position.z = terrainY;
        } else {
          this.cursor!.position.x = terrainX;
          this.cursor!.position.z = terrainY - (h * Math.cos(toRadians(45) + slopeAngle));
        }
      } else {
        this.cursor!.position.x = terrainX;
        this.cursor!.position.y = this.height - BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2 + BotGroundEditorService.EDITOR_CURSOR_Y;
        this.cursor!.position.z = terrainY;
        this.cursor!.rotation.z = 0;
      }

      this.cursor!.material = new SimpleMaterial("Bot ground cursor");
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
    this.container!.dispose();
    this.container = null;
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

  public setHeight(botConfig: BotConfig, height: number) {
    if (botConfig.id !== this.botConfig!.id) {
      return;
    }
    this.height = height;
    Array.from(this.groundBoxPositions.values()).forEach((mesh) => {
      mesh.position.y = this.height - BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2 + BotGroundEditorService.EDITOR_BOX_Y;
    });
  }


  setSlopeMode(botConfig: BotConfig, slopeMode: boolean) {
    if (botConfig.id !== this.botConfig!.id) {
      return;
    }
    this.slopeMode = slopeMode;
    this.cursor!.rotationQuaternion = null;
    if (this.slopeMode) {
      const slopeAngle = toRadians(BabylonTerrainTileImpl.BOT_BOX_Z_ROTATION);
      this.cursor!.position.y = this.height - (BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2) * (1 - Math.cos(slopeAngle)) - BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2;
      this.cursor!.rotation.z = slopeAngle;
    } else {
      this.cursor!.position.y = this.height - BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2;
      this.cursor!.rotation.z = 0;
    }
  }

  rotationSlope(botConfig: BotConfig) {
    if (botConfig.id !== this.botConfig!.id) {
      return;
    }
    this.cursor!.rotationQuaternion = null;
    this.cursor!.rotation.y += toRadians(90);
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
    const box = MeshBuilder.CreateBox("Box", {size: BabylonTerrainTileImpl.BOT_BOX_LENGTH}, this.renderer.getScene());
    box.parent = this.container;
    box.position.x = decimalPosition.x;
    box.position.y = this.height - BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2 + BotGroundEditorService.EDITOR_BOX_Y;
    box.position.z = decimalPosition.y;
    return box;
  }
}
