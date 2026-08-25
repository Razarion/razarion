import {Component, NgZone} from '@angular/core';
import {MainCockpit, RadarState} from "../../../gwtangular/GwtAngularFacade";
import {GameComponent} from '../../game.component';
import {Nullable, Observer, PointerEventTypes, PointerInfo} from '@babylonjs/core';
import {BabylonRenderServiceAccessImpl} from '../../renderer/babylon-render-service-access-impl.service';
import {RadarComponent} from './radar/radar.component';
import {RadarNoPowerComponent} from './radar/radar-no-power.component';
import { CommonModule, NgClass } from '@angular/common';
import {TooltipModule} from 'primeng/tooltip';
import {Dialog} from 'primeng/dialog';
import {LoginComponent} from '../../../auth/login/login.component';
import {UserService} from '../../../auth/user.service';
import {UserComponent} from '../../../auth/user/user.component';
import {RegisterComponent} from '../../../auth/register/register.component';
import {CockpitDisplayService} from '../cockpit-display.service';
import {SetNameComponent} from '../../../auth/set-name/set-name.component';
import {SettingsComponent} from '../settings/settings.component';
import {TechTreeComponent} from '../techtree/tech-tree.component';
import {GwtAngularService} from '../../../gwtangular/GwtAngularService';
import {CompactLayoutService} from '../compact-layout.service';


@Component({
  selector: 'main-cockpit',
  templateUrl: 'main-cockpit.component.html',
  imports: [
    RadarComponent,
    RadarNoPowerComponent,
    NgClass,
    CommonModule,
    TooltipModule,
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
  /**
   * The same three readouts as numbers.
   * <p>
   * The strings above are what the phone menu shows and what the desktop showed until the cockpit
   * grew bars: "how close am I to the next level" and "how much power have I got left" are
   * questions a fraction answers slowly and a filled rail answers at a glance. Kept beside the
   * strings rather than replacing them - the compact menu still reads the strings, and the top bar
   * prints them next to its bars.
   */
  xp = 0;
  xp2LevelUp = 0;
  energyConsuming = 0;
  energyGenerating = 0;
  radarState!: RadarState;
  WORKING = RadarState.WORKING;
  NO_POWER = RadarState.NO_POWER;
  blinkUnlockEnabled = false;
  /** Cells in the power meter. An array because the template iterates it; the value is the index. */
  readonly powerSegments = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11];

  constructor(public mainCockpitService: CockpitDisplayService,
              private zone: NgZone,
              private cockpitDisplayService: CockpitDisplayService,
              private gameComponent: GameComponent,
              private renderService: BabylonRenderServiceAccessImpl,
              private gwtAngularService: GwtAngularService,
              public compactLayout: CompactLayoutService,
              public userService: UserService) {
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
      this.energyConsuming = consuming;
      this.energyGenerating = generating;
      this.displayEnergyString = `${consuming} / ${generating}`;
    });
  }

  /**
   * Consumption has passed generation: the base is over its power budget. The one energy state
   * worth a colour, so the meter can say it without the player working the fraction out.
   */
  get powerOverloaded(): boolean {
    return this.energyConsuming > this.energyGenerating;
  }

  /**
   * How many cells of the power meter are lit - consumption as a share of generation, rounded up
   * so that drawing anything at all lights the first cell. Nothing generated yet but something
   * drawing is a full red row rather than an empty one: it is the worst case, not the emptiest.
   */
  get powerFilled(): number {
    const total = this.powerSegments.length;
    if (this.energyGenerating <= 0) {
      return this.energyConsuming > 0 ? total : 0;
    }
    return Math.min(total, Math.ceil(this.energyConsuming / this.energyGenerating * total));
  }

  /** Progress towards the next level, as a percentage for the rail under the top bar. */
  get xpPercent(): number {
    if (this.xp2LevelUp <= 0) {
      return 0;
    }
    return Math.max(0, Math.min(100, this.xp / this.xp2LevelUp * 100));
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
      this.xp = xp;
      this.xp2LevelUp = xp2LevelUp;
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

  /** The phone menu has a row instead of a checkbox, so the row itself does the flipping. */
  onToggleCursorPosition(): void {
    this.showCursorPosition = !this.showCursorPosition;
    this.onShowCursorPosition();
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
