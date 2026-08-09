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
import {GwtAngularService} from '../../../gwtangular/GwtAngularService';


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
  /**
   * Why the Razarion counter is worth looking at right now. EMPTY and FULL are the two states in
   * which the game stops responding to the player without saying so: broke, and the factories
   * quietly stop building; at the cap, and the harvesters bring in nothing. Both look the same
   * from the outside - the number does not move - which is how a player ends up staring at an
   * idle base wondering what is broken.
   */
  razarionState: 'OK' | 'EMPTY' | 'FULL' = 'OK';
  /**
   * Null until the first balance arrives, which suppresses the splash for that first update. A
   * player who joins already broke has not just run out of anything - the red field states it
   * without a splash claiming an event that did not happen. It also keeps the cockpit quiet
   * during startup, where the balance is 0 until a base exists.
   */
  private lastRazarionState: 'OK' | 'EMPTY' | 'FULL' | null = null;
  /**
   * The cap, read once from the planet config. 0 means unlimited, so FULL can never trigger.
   * Null until the config is available, which keeps the very first update from claiming FULL.
   */
  private maxRazarion: number | null = null;
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
              private gwtAngularService: GwtAngularService,
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
      this.readMaxRazarion();
      this.razarionState = this.toRazarionState(resources);
      // Only the edge is announced, and never the first one. The state itself is on screen for as
      // long as it lasts, in the colour of the field; a splash on every update would be one per
      // tick.
      const firstUpdate = this.lastRazarionState === null;
      if (this.razarionState !== this.lastRazarionState) {
        this.lastRazarionState = this.razarionState;
        if (firstUpdate) {
          // Nothing to announce yet - this is the state the player arrived in, not a change.
        } else if (this.razarionState === 'EMPTY') {
          this.gameComponent.modelDialogPresenter.post("Out of Razarion",
            ["Harvest more before building"]);
        } else if (this.razarionState === 'FULL') {
          this.gameComponent.modelDialogPresenter.post("Razarion full",
            ["Harvesting earns nothing until you spend"]);
        }
      }
    });
  }

  razarionTooltip(): string {
    switch (this.razarionState) {
      case 'EMPTY':
        return 'Out of Razarion - factories and builders have stopped. Send a harvester to a Razarion field.';
      case 'FULL':
        return 'Razarion at the maximum - harvesting brings in nothing until you spend some.';
      default:
        return 'Razarion';
    }
  }

  private toRazarionState(resources: number): 'OK' | 'EMPTY' | 'FULL' {
    if (resources <= 0) {
      return 'EMPTY';
    }
    if (this.maxRazarion === null) {
      // Cap not known yet - saying FULL here would be a guess, and the wrong one at game start.
      return 'OK';
    }
    // 0 means unlimited, as PlayerBase.addResource reads it.
    if (this.maxRazarion > 0 && resources >= this.maxRazarion) {
      return 'FULL';
    }
    return 'OK';
  }

  /**
   * The cap lives in the planet config, which only exists once the engine has started - the
   * cockpit is built before that is true, so this is read on first use instead of in the
   * constructor. Failing to read it leaves maxRazarion null, and a null cap simply means no FULL
   * warning: the safe way to be wrong here.
   */
  private readMaxRazarion(): void {
    if (this.maxRazarion !== null) {
      return;
    }
    try {
      this.maxRazarion = this.gwtAngularService.gwtAngularFacade
        .gameUiControl.getPlanetConfig().getMaxRazarion();
    } catch (e) {
      // Engine not up yet. Tried again on the next resource update.
    }
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
