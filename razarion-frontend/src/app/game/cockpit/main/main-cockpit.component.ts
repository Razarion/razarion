import {Component, NgZone} from '@angular/core';
import {MainCockpit, RadarState} from "../../../gwtangular/GwtAngularFacade";
import {GameComponent} from '../../game.component';
import {Nullable, Observer, PointerEventTypes, PointerInfo} from '@babylonjs/core';
import {BabylonRenderServiceAccessImpl} from '../../renderer/babylon-render-service-access-impl.service';
import {Button} from 'primeng/button';
import {RadarComponent} from './radar/radar.component';
import {RadarNoPowerComponent} from './radar/radar-no-power.component';
import {Checkbox} from 'primeng/checkbox';
import { CommonModule, NgClass } from '@angular/common';
import {Badge} from 'primeng/badge';
import {InputText} from 'primeng/inputtext';
import {InputGroupAddonModule} from 'primeng/inputgroupaddon';
import {InputGroupModule} from 'primeng/inputgroup';
import {TooltipModule} from 'primeng/tooltip';
import {FormsModule} from '@angular/forms';
import {Dialog} from 'primeng/dialog';
import {LoginComponent} from '../../../auth/login/login.component';
import {UserService} from '../../../auth/user.service';
import {UserComponent} from '../../../auth/user/user.component';
import {RegisterComponent} from '../../../auth/register/register.component';
import {CockpitDisplayService} from '../cockpit-display.service';
import {SetNameComponent} from '../../../auth/set-name/set-name.component';
import {SelectionShortcutCategory, SelectionShortcutsService} from '../../selection-shortcuts.service';
import {SettingsComponent} from '../settings/settings.component';
import {TechTreeComponent} from '../techtree/tech-tree.component';


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
    CommonModule,
    InputText,
    InputGroupAddonModule,
    InputGroupModule,
    TooltipModule,
    FormsModule,
    Dialog,
    LoginComponent,
    UserComponent,
    RegisterComponent,
    SetNameComponent,
    SettingsComponent,
    TechTreeComponent
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
  // Order matters: the grid fills row-major into 3 columns, so
  //   row 1: builder | factory | building(other)
  //   row 2: harvester | attack | deselect
  // The deselect button is rendered separately as the 6th cell after this list.
  shortcutCategories: { id: SelectionShortcutCategory, icon: string, tooltip: string }[] = [
    {id: 'builder', icon: 'pi pi-wrench', tooltip: 'Next builder'},
    {id: 'factory', icon: 'pi pi-cog', tooltip: 'Next factory'},
    {id: 'other', icon: 'pi pi-building', tooltip: 'Next building / other'},
    {id: 'harvester', icon: 'pi pi-truck', tooltip: 'Next harvester'},
    {id: 'attack', icon: 'pi pi-bolt', tooltip: 'Next attack unit'},
  ];

  constructor(public mainCockpitService: CockpitDisplayService,
              private zone: NgZone,
              private cockpitDisplayService: CockpitDisplayService,
              private gameComponent: GameComponent,
              private renderService: BabylonRenderServiceAccessImpl,
              public userService: UserService,
              public selectionShortcuts: SelectionShortcutsService) {
  }

  shortcutBadge(category: SelectionShortcutCategory): string {
    return this.selectionShortcuts.getCount(category).toString();
  }

  show(): void {
    this.zone.run(() => {
      this.cockpitDisplayService.showMainCockpit = true;
      this.cockpitDisplayService.showChatCockpit = true;
    });
  }

  hide(): void {
    this.zone.run(() => {
      this.cockpitDisplayService.showMainCockpit = false;
      this.cockpitDisplayService.showChatCockpit = false;
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
      this.cockpitDisplayService.currentLevelNumber = levelNumber;
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

  onInfo(): void {
    this.cockpitDisplayService.openInfoDialog('info');
  }

  blinkAvailableUnlock(show: boolean): void {
    this.zone.run(() => {
      this.blinkUnlockEnabled = show;
    });
  }

  isLoggedIn(): boolean {
    return this.userService.isLoggedIn();
  }

  isAdmin(): boolean {
    return this.userService.isAdmin();
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
