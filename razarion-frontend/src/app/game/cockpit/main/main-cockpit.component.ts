﻿import {Component, NgZone} from '@angular/core';
import {MainCockpit, RadarState, Rectangle} from "../../../gwtangular/GwtAngularFacade";
import {GameComponent} from '../../game.component';
import {Nullable, Observer, PointerEventTypes, PointerInfo} from '@babylonjs/core';
import {BabylonRenderServiceAccessImpl} from '../../renderer/babylon-render-service-access-impl.service';
import {Router} from '@angular/router';
import {Button} from 'primeng/button';
import {RadarComponent} from './radar/radar.component';
import {RadarNoPowerComponent} from './radar/radar-no-power.component';
import {Checkbox} from 'primeng/checkbox';
import {NgClass, NgIf} from '@angular/common';
import {Badge} from 'primeng/badge';
import {CockpitDisplayService} from '../cockpit-display.service';
import {InputText} from 'primeng/inputtext';
import {InputGroupAddonModule} from 'primeng/inputgroupaddon';
import {InputGroupModule} from 'primeng/inputgroup';
import {TooltipModule} from 'primeng/tooltip';
import {FormsModule} from '@angular/forms';
import {Dialog} from 'primeng/dialog';
import {LoginComponent} from '../../../auth/login/login.component';
import {AuthService} from '../../../auth/auth.service';
import {UserComponent} from '../../../auth/user/user.component';


@Component({
  selector: 'main-cockpit',
  templateUrl: 'main-cockpit.component.html',
  imports: [
    Button,
    RadarComponent,
    RadarNoPowerComponent,
    Checkbox,
    NgClass,
    Badge,
    NgIf,
    InputText,
    InputGroupAddonModule,
    InputGroupModule,
    TooltipModule,
    FormsModule,
    Dialog,
    LoginComponent,
    UserComponent
  ],
  styleUrls: ['main-cockpit.component.scss']
})
export class MainCockpitComponent implements MainCockpit {
  editorDialog: boolean = false;
  showCursorPosition: boolean = false;
  cursorPosition?: string;
  private mouseObservable: Nullable<Observer<PointerInfo>> = null;
  levelNumber!: number;
  resources: number = 0;
  displayHouseSpace = "";
  displayEnergyString = "";
  displayXp2LevelUp = "";
  radarState!: RadarState;
  WORKING = RadarState.WORKING;
  NO_POWER = RadarState.NO_POWER;
  blinkUnlockEnabled = false;
  showLoginDialog = false;
  showUserDialog = false;

  constructor(private zone: NgZone,
              private cockpitDisplayService: CockpitDisplayService,
              private gameComponent: GameComponent,
              private renderService: BabylonRenderServiceAccessImpl,
              private authService: AuthService) {
  }

  show(): void {
    this.zone.run(() => {
      this.cockpitDisplayService.showMainCockpit = true;
    });
  }

  hide(): void {
    this.zone.run(() => {
      this.cockpitDisplayService.showMainCockpit = false;
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
      this.displayEnergyString = `${consuming} / ${generating}`;
    });
  }

  displayItemCount(itemCount: number, usedHouseSpace: number, houseSpace: number): void {
    this.zone.run(() => {
      this.displayHouseSpace = `${usedHouseSpace} / ${houseSpace}`;
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
      this.displayXp2LevelUp = `${xp} / ${xp2LevelUp}`;
    });
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

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  isAdmin(): boolean {
    return this.authService.isAdmin();
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
