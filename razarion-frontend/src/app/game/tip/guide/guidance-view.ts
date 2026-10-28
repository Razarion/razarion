import {MarkerConfig, PlaceConfig} from '../../../gwtangular/GwtAngularFacade';
import {BabylonRenderServiceAccessImpl} from '../../renderer/babylon-render-service-access-impl.service';
import {ItemCockpitComponent} from '../../cockpit/item/item-cockpit.component';
import {GwtInstance} from '../../../gwtangular/GwtInstance';
import {Guidance} from './tip-decision';
import {regionInReach} from './reach-area';
import {ETA_WINDOW_MILLIS, progressText, ProgressSample, remainingSeconds} from './progress-eta';
import {UnlockTip} from './unlock-tip';

/**
 * Puts one Guidance on the screen and takes the previous one down.
 *
 * The only part of the tips that touches the renderer, and it reads nothing back from it except
 * "which instance has this id right now": an item that left the view and came back is a new
 * instance, and it gets the prompt again on the next evaluation because the prompt is looked up by
 * id every time (W2).
 */
export class GuidanceView {
  private static readonly PROGRESS_SAMPLE_MILLIS = 250;
  private current: Guidance = {kind: 'quiet'};
  /** The wait being shown - see showProgress. Its samples belong to one phase of one item. */
  private progressKey: string | null = null;
  private progressSamples: ProgressSample[] = [];
  /** Where the camera follows to, updated on every evaluation; the renderer eases toward it. */
  private followTarget: { x: number, y: number } | null = null;
  private stopFollow: (() => void) | null = null;
  /** The player moved the camera during this build: the camera is theirs until it is over. */
  private followDeclined = false;
  /** The clock the time left is measured with; the test bed runs on a clock of its own. */
  private now: () => number = () => Date.now();
  private readonly unlockTip = new UnlockTip();

  constructor(private readonly renderService: BabylonRenderServiceAccessImpl,
              private readonly itemCockpit: () => ItemCockpitComponent | null,
              private readonly outOfViewMarkerConfig: MarkerConfig,
              private readonly placeMarkerConfig: () => MarkerConfig) {
  }

  show(guidance: Guidance, placeConfig: PlaceConfig | null): void {
    const previous = this.current;
    this.current = guidance;
    if (previous.kind !== guidance.kind || !samePrompt(previous, guidance) || !sameReach(previous, guidance)) {
      this.takeDown(previous, guidance);
    }
    switch (guidance.kind) {
      case 'prompt': {
        const item = this.findItem(guidance.itemId, guidance.resource, !!guidance.box);
        // Misplaced: the prompt is up but built for the other side of the item, which the player
        // scrolled it to - see isSelectPromptMisplaced. Rebuilding is what turns the label round.
        if (item && (!item.isSelectPromptVisible() || item.isSelectPromptMisplaced?.())) {
          item.showSelectPromptVisualization(guidance.text);
        }
        break;
      }
      case 'arrow':
        this.showArrow(guidance.x, guidance.y);
        break;
      case 'button':
        // Re-anchored on every evaluation: a selection event rebuilds the cockpit and the button.
        this.itemCockpit()?.showBuildupTip(guidance.itemTypeId);
        break;
      case 'placeMarker':
        this.renderService.showPlaceMarker(guidance.reach ? regionInReach(placeConfig, guidance.reach) : placeConfig,
          this.placeMarkerConfig());
        break;
      case 'unload':
        // Re-anchored on every evaluation, like the build button.
        this.itemCockpit()?.showUnloadTip(true);
        break;
      case 'sell':
        this.itemCockpit()?.showSellTip(true);
        break;
      case 'unlock':
        // Re-anchored on every evaluation: the phone menu and the unlock dialog come and go.
        this.unlockTip.show();
        break;
      case 'group':
        this.renderService.touchSelectionMode.setAsked(true);
        if (guidance.arrow) {
          this.showArrow(guidance.arrow.x, guidance.arrow.y);
        } else {
          this.renderService.showOutOfViewMarker(null, 0);
        }
        break;
      case 'progress':
        this.showProgress(guidance);
        break;
      case 'quiet':
        break;
    }
  }

  /**
   * The builder on its way, then the site going up, with the time it still takes - and the camera
   * along with it. The wait after placing a building was a blank half minute at the coast in quest
   * 386, and 10 of 16 who failed the quest after it left during it (2026-10-09).
   */
  private showProgress(guidance: Extract<Guidance, { kind: 'progress' }>): void {
    const key = guidance.phase + ':' + guidance.itemId;
    if (key !== this.progressKey) {
      this.progressKey = key;
      this.progressSamples = [];
    }
    const value = guidance.phase === 'driving' ? guidance.distance : guidance.buildup;
    if (value !== null) {
      const now = this.now();
      const last = this.progressSamples[this.progressSamples.length - 1];
      // The guide evaluates on every camera move, and the camera moves every frame while it
      // follows: without a minimum spacing the window filled up within a fraction of a second.
      if (!last || now - last.t >= GuidanceView.PROGRESS_SAMPLE_MILLIS) {
        this.progressSamples.push({t: now, value});
        this.progressSamples = this.progressSamples.filter(sample => now - sample.t <= ETA_WINDOW_MILLIS);
      }
    }
    const seconds = value !== null ? remainingSeconds(this.progressSamples, guidance.phase === 'driving' ? 0 : 1) : null;
    this.renderService.showBuildProgress(guidance.itemId, progressText(guidance.phase, seconds, guidance.buildup));
    this.followTarget = {x: guidance.x, y: guidance.y};
    if (!this.followDeclined && !this.stopFollow) {
      this.stopFollow = this.renderService.followViewFieldCenter(() => this.followTarget, () => {
        this.followDeclined = true;
        this.stopFollow = null;
      });
    }
  }

  setClock(now: () => number): void {
    this.now = now;
  }

  /** Recomputes the arrow's angle for the new view; everything else does not depend on it. */
  onViewFieldChanged(): void {
    const guidance = this.current;
    if (guidance.kind === 'arrow') {
      this.showArrow(guidance.x, guidance.y);
    } else if (guidance.kind === 'group' && guidance.arrow) {
      this.showArrow(guidance.arrow.x, guidance.arrow.y);
    }
  }

  clear(): void {
    this.takeDown(this.current, {kind: 'quiet'});
    this.current = {kind: 'quiet'};
  }

  private takeDown(previous: Guidance, next: Guidance): void {
    if (previous.kind === 'prompt') {
      this.findItem(previous.itemId, previous.resource, !!previous.box)?.hideSelectPromptVisualization();
    }
    const arrowNext = next.kind === 'arrow' || (next.kind === 'group' && next.arrow !== null);
    if (!arrowNext) {
      this.renderService.showOutOfViewMarker(null, 0);
    }
    if (previous.kind === 'button' && next.kind !== 'button') {
      this.itemCockpit()?.showBuildupTip(null);
    }
    if (previous.kind === 'unload' && next.kind !== 'unload') {
      this.itemCockpit()?.showUnloadTip(false);
    }
    if (previous.kind === 'sell' && next.kind !== 'sell') {
      this.itemCockpit()?.showSellTip(false);
    }
    if (previous.kind === 'unlock' && next.kind !== 'unlock') {
      this.unlockTip.hide();
    }
    if (previous.kind === 'placeMarker') {
      this.renderService.showPlaceMarker(null, null);
    }
    if (previous.kind === 'group' && next.kind !== 'group') {
      this.renderService.touchSelectionMode.setAsked(false);
    }
    if (previous.kind === 'progress' && next.kind !== 'progress') {
      this.renderService.showBuildProgress(null, '');
      this.stopFollow?.();
      this.stopFollow = null;
      this.followTarget = null;
      this.followDeclined = false;
      this.progressKey = null;
      this.progressSamples = [];
    }
  }

  private showArrow(x: number, y: number): void {
    const viewField = this.renderService.getCurrentViewField();
    if (!viewField) {
      return;
    }
    this.renderService.showOutOfViewMarker(this.outOfViewMarkerConfig,
      viewField.getAngleTo(GwtInstance.newDecimalPosition(x, y)), {x, y});
  }

  private findItem(id: number, resource: boolean, box: boolean): {
    isSelectPromptVisible(): boolean,
    /** Optional: the resource items do not carry a side. */
    isSelectPromptMisplaced?(): boolean,
    showSelectPromptVisualization(text: string): void,
    hideSelectPromptVisualization(): void
  } | null {
    if (resource) {
      return this.renderService.getBabylonResourceItemImpls().find(item => item.getId() === id) ?? null;
    }
    if (box) {
      return this.renderService.getBabylonBoxItemImpls().find(item => item.getId() === id) ?? null;
    }
    return this.renderService.getBabylonBaseItemById(id);
  }
}

function samePrompt(one: Guidance, other: Guidance): boolean {
  if (one.kind !== 'prompt' || other.kind !== 'prompt') {
    return true;
  }
  return one.itemId === other.itemId && one.text === other.text && one.resource === other.resource && !!one.box === !!other.box;
}

/** A marker cut to a ship's reach is rebuilt when the ship is another one or has moved. */
function sameReach(one: Guidance, other: Guidance): boolean {
  if (one.kind !== 'placeMarker' || other.kind !== 'placeMarker') {
    return true;
  }
  const a = one.reach;
  const b = other.reach;
  if (!a || !b) {
    return a === b;
  }
  return Math.abs(a.x - b.x) < 0.5 && Math.abs(a.y - b.y) < 0.5 && a.radius === b.radius;
}
