import {Injectable} from '@angular/core';
import {BabylonRenderServiceAccessImpl} from '../../game/renderer/babylon-render-service-access-impl.service';
import {Color3, Matrix, Mesh, MeshBuilder} from '@babylonjs/core';
import {BabylonTerrainTileImpl} from '../../game/renderer/babylon-terrain-tile.impl';
import {BotConfig, BotGroundSlopeBox, DecimalPosition} from '../../generated/razarion-share';
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
  private groundSlopeBoxes = new Map<BotGroundSlopeBox, Mesh>();
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
    this.disposeBoxes();
    this.height = botConfig.groundBoxHeight === null ? 0 : botConfig.groundBoxHeight;
    this.container = new TransformNode("Bot ground editor");
    this.setupBoxes(botConfig.groundBoxPositions);
    this.setupSlopeBoxes(botConfig.botGroundSlopeBoxes);
    this.botConfig = botConfig;

    this.cursor = MeshBuilder.CreateBox("Cursor", {size: BabylonTerrainTileImpl.BOT_BOX_LENGTH}, this.renderer.getScene());
    this.cursor.position.y = -BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2.0;
    this.cursor.bakeCurrentTransformIntoVertices();
    this.cursor.position.y = 0;
    this.cursor.parent = this.container;
    this.pointerObservable = this.renderer.getScene().onPointerObservable.add((pointerInfo) => {
      let ray = this.renderer.getScene().createPickingRay(this.renderer.getScene().pointerX, this.renderer.getScene().pointerY, Matrix.Identity(), null);
      const t = (this.height - ray.origin.y) / ray.direction.y;
      const hitPoint = ray.origin.add(ray.direction.scale(t));
      let terrainX = Math.floor(hitPoint.x);
      let terrainY = Math.floor(hitPoint.z);
      let slopeHeight;
      if (this.slopeMode) {
        const slopeAngle = toRadians(BabylonTerrainTileImpl.BOT_BOX_Z_ROTATION);
        slopeHeight = this.height - BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2.0 * Math.sin(slopeAngle);
        const yRot = this.cursor!.rotation.y % (2 * Math.PI);
        if (yRot < toRadians(90)) {
          terrainX -= BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2 * Math.cos(slopeAngle) - BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2;
        } else if (yRot < toRadians(180)) {
          terrainY += BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2 * Math.cos(slopeAngle) - BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2;
        } else if (yRot < toRadians(270)) {
          terrainX += BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2 * Math.cos(slopeAngle) - BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2;
        } else {
          terrainY -= BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2 * Math.cos(slopeAngle) - BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2;
        }
        this.cursor!.position.x = terrainX;
        this.cursor!.position.y = slopeHeight + BotGroundEditorService.EDITOR_CURSOR_Y;
        this.cursor!.position.z = terrainY;
        this.cursor!.rotation.z = slopeAngle;
      } else {
        this.cursor!.position.x = terrainX;
        this.cursor!.position.y = this.height + BotGroundEditorService.EDITOR_CURSOR_Y;
        this.cursor!.position.z = terrainY;
        this.cursor!.rotation.z = 0;
      }

      this.cursor!.material = new SimpleMaterial("Bot ground cursor");
      (<SimpleMaterial>this.cursor!.material).diffuseColor = new Color3(0.8, 0.6, 0);

      const newDecimalPosition = {x: terrainX, y: terrainY};
      if (pointerInfo.event.buttons & 1) {
        if (this.slopeMode) {
          const existing = Array.from(this.groundSlopeBoxes.keys())
            .find((groundSlopeBox) => this.checkSquaresOverlap({
              x: groundSlopeBox.xPos,
              y: groundSlopeBox.yPos
            }, newDecimalPosition));
          if (!existing) {
            const botGroundSlopeBox = {
              xPos: terrainX,
              yPos: terrainY,
              height: slopeHeight!,
              yRot: this.cursor!.rotation.y,
              zRot: toRadians(BabylonTerrainTileImpl.BOT_BOX_Z_ROTATION)
            };
            this.groundSlopeBoxes.set(botGroundSlopeBox, this.setupSlopeBox(
              terrainX,
              terrainY,
              slopeHeight!,
              this.cursor!.rotation.y,
              toRadians(BabylonTerrainTileImpl.BOT_BOX_Z_ROTATION)));
            this.updateBotConfig();
          }
        } else {
          const existing = Array.from(this.groundBoxPositions.entries())
            .find(([pos]) => this.checkSquaresOverlap(pos, newDecimalPosition));
          if (!existing) {
            this.groundBoxPositions.set(newDecimalPosition, this.setupBox(newDecimalPosition));
            this.updateBotConfig();
          }
        }
      } else if (pointerInfo.event.buttons & 2) {
        if (this.slopeMode) {
          const existing = Array.from(this.groundSlopeBoxes.keys())
            .find((groundSlopeBox) => this.checkSquaresOverlap({
              x: groundSlopeBox.xPos,
              y: groundSlopeBox.yPos
            }, newDecimalPosition));
          if (existing) {
            this.groundSlopeBoxes.get(existing)!.dispose();
            this.groundSlopeBoxes.delete(existing);
            this.updateBotConfig();
          }
        } else {
          const existing = Array.from(this.groundBoxPositions.entries())
            .find(([pos]) => this.checkSquaresOverlap(pos, newDecimalPosition));
          if (existing) {
            const [pos, mesh] = existing;
            mesh.dispose();
            this.groundBoxPositions.delete(pos);
            this.updateBotConfig();
          }
        }
      }
    });
  }

  deactivate(botConfig: BotConfig) {
    if (!this.botConfig) {
      return;
    }
    if (botConfig.id === this.botConfig.id) {
      this.container!.dispose();
      this.container = null;
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
    if (!groundBoxPositions) {
      return;
    }
    groundBoxPositions.forEach(groundBoxPosition => {
      this.groundBoxPositions.set(groundBoxPosition, this.setupBox(groundBoxPosition));
    })
  }

  private setupSlopeBoxes(botGroundSlopeBoxes: BotGroundSlopeBox[]) {
    if (!botGroundSlopeBoxes) {
      return;
    }
    botGroundSlopeBoxes.forEach(botGroundSlopeBox => {
      return this.groundSlopeBoxes.set(botGroundSlopeBox, this.setupSlopeBox(
        botGroundSlopeBox.xPos,
        botGroundSlopeBox.yPos,
        botGroundSlopeBox.height,
        botGroundSlopeBox.yRot,
        botGroundSlopeBox.zRot));
    })
  }

  private disposeBoxes() {
    Array.from(this.groundBoxPositions.values()).forEach(box => box.dispose());
    this.groundBoxPositions.clear();
    Array.from(this.groundSlopeBoxes.values()).forEach(slopeBox => slopeBox.dispose());
    this.groundSlopeBoxes.clear();
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
    this.botConfig!.botGroundSlopeBoxes = Array.from(this.groundSlopeBoxes.keys());
  }

  private setupBox(decimalPosition: DecimalPosition) {
    const box = MeshBuilder.CreateBox("Box", {size: BabylonTerrainTileImpl.BOT_BOX_LENGTH}, this.renderer.getScene());
    box.position.y = -BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2.0;
    box.bakeCurrentTransformIntoVertices();
    box.position.y = 0;
    box.parent = this.container;
    box.position.x = decimalPosition.x;
    box.position.y = this.height + BotGroundEditorService.EDITOR_BOX_Y;
    box.position.z = decimalPosition.y;
    return box;
  }

  private setupSlopeBox(xPos: number,
                        yPos: number,
                        height: number,
                        yRot: number,
                        zRot: number
  ) {
    const slopeBox = MeshBuilder.CreateBox("Slope", {size: BabylonTerrainTileImpl.BOT_BOX_LENGTH}, this.renderer.getScene());
    slopeBox.position.y = -BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2.0;
    slopeBox.bakeCurrentTransformIntoVertices();
    slopeBox.position.y = 0;
    slopeBox.parent = this.container;
    slopeBox.position.x = xPos;
    slopeBox.position.y = height + BotGroundEditorService.EDITOR_BOX_Y;
    slopeBox.position.z = yPos;
    slopeBox.rotation.y = yRot;
    slopeBox.rotation.z = zRot;
    return slopeBox;
  }
}
