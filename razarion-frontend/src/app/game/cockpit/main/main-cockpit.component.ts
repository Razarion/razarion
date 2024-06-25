import { Component, NgZone } from '@angular/core';
import { MainCockpit, RadarState, Rectangle } from "../../../gwtangular/GwtAngularFacade";
import { GameComponent } from '../../game.component';
import { Nullable, Observer, PointerEventTypes, PointerInfo } from '@babylonjs/core';
import { BabylonRenderServiceAccessImpl } from '../../renderer/babylon-render-service-access-impl.service';


@Component({
  selector: 'main-cockpit',
  templateUrl: 'main-cockpit.component.html',
  styleUrls: ['main-cockpit.component.scss']
})
export class MainCockpitComponent implements MainCockpit {
  showCockpit: boolean = false;
  admin: boolean = false;
  editorDialog: boolean = false;
  showCursorPosition: boolean = false;
  cursorPosition?: string;
  private mouseObservable: Nullable<Observer<PointerInfo>> = null;
  levelNumber!: number;
  xp!: number;
  xp2LevelUp!: number;
  resources: number = 0;
  itemCount: number = 0;
  houseSpace: number = 0;
  consuming: number = 0;
  generating: number = 0;
  radarState!: RadarState;
  WORKING = RadarState.WORKING;
  NO_POWER = RadarState.NO_POWER;
  blinkUnlockEnabled = false;

  constructor(private zone: NgZone,
    private gameComponent: GameComponent,
    private renderService: BabylonRenderServiceAccessImpl) {
  }

  show(admin: boolean): void {
    this.zone.run(() => {
      this.admin = admin;
      this.showCockpit = true;
    });
  }

  hide(): void {
    this.zone.run(() => {
      this.showCockpit = false;
    });
  }

  showRadar(radarState: RadarState): void {
    this.zone.run(() => {
      this.radarState = radarState;
    });
  }

  clean(): void {
    // TODO
  }

  displayEnergy(consuming: number, generating: number): void {
    this.zone.run(() => {
      this.consuming = consuming;
      this.generating = generating;
    });
  }

  displayItemCount(itemCount: number, houseSpace: number): void {
    this.zone.run(() => {
      this.itemCount = itemCount;
      this.houseSpace = houseSpace;
    });
  }

  displayLevel(levelNumber: number): void {
    this.zone.run(() => {
      this.levelNumber = levelNumber;
    });
  }

  displayResources(resources: number): void {
    this.zone.run(() => {
      this.resources = resources;
    });
  }

  displayXps(xp: number, xp2LevelUp: number): void {
    this.zone.run(() => {
      this.xp = xp;
      this.xp2LevelUp = xp2LevelUp;
    });
  }

  getInventoryDialogButtonLocation(): Rectangle {
    return {};
  }

  getScrollHomeButtonLocation(): Rectangle {
    return {};
  }

  onInventory(): void {
    this.gameComponent.openInventory();
  }

  onUnlock(): void {
    this.gameComponent.openUnlock();
  }

  blinkAvailableUnlock(show: boolean): void {
    this.zone.run(() => {
      this.blinkUnlockEnabled = show;
    });
  }


  onShowCursorPosition(): void {
    if (this.showCursorPosition) {
      this.cursorPosition = " x:---.-- y:---.-- height:---.--";
      this.mouseObservable = this.renderService.getScene().onPointerObservable.add((pointerInfo) => {
        if (pointerInfo.type === PointerEventTypes.POINTERMOVE) {
          let terrainPisckInfo = this.renderService.setupTerrainPickPoint();
          if (terrainPisckInfo.pickedPoint) {
            this.cursorPosition = ` x: ${terrainPisckInfo.pickedPoint.x.toFixed(2)}, y: ${terrainPisckInfo.pickedPoint.z.toFixed(2)}, height: ${terrainPisckInfo.pickedPoint.y.toFixed(2)}`;
          }
        }
      }
      );
    } else {
      if (this.mouseObservable) {
        this.renderService.getScene().onPointerObservable.remove(this.mouseObservable);
        this.mouseObservable = null;
      }
      this.cursorPosition = undefined;
    }
  }


}
