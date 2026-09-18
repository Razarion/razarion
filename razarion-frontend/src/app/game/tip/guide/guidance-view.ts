import {MarkerConfig, PlaceConfig} from '../../../gwtangular/GwtAngularFacade';
import {BabylonRenderServiceAccessImpl} from '../../renderer/babylon-render-service-access-impl.service';
import {ItemCockpitComponent} from '../../cockpit/item/item-cockpit.component';
import {GwtInstance} from '../../../gwtangular/GwtInstance';
import {Guidance, PROMPT} from './tip-decision';

/** Label and container widths of the prompt, per text - the longer ones do not fit the default. */
const PROMPT_WIDTHS: Record<string, [string, string]> = {
  [PROMPT.HARVEST]: ['150px', '200px'],
  [PROMPT.CONTINUE_BUILDING]: ['200px', '250px']
};

/**
 * Puts one Guidance on the screen and takes the previous one down.
 *
 * The only part of the tips that touches the renderer, and it reads nothing back from it except
 * "which instance has this id right now": an item that left the view and came back is a new
 * instance, and it gets the prompt again on the next evaluation because the prompt is looked up by
 * id every time (W2).
 */
export class GuidanceView {
  private current: Guidance = {kind: 'quiet'};

  constructor(private readonly renderService: BabylonRenderServiceAccessImpl,
              private readonly itemCockpit: () => ItemCockpitComponent | null,
              private readonly outOfViewMarkerConfig: MarkerConfig,
              private readonly placeMarkerConfig: () => MarkerConfig) {
  }

  show(guidance: Guidance, placeConfig: PlaceConfig | null): void {
    const previous = this.current;
    this.current = guidance;
    if (previous.kind !== guidance.kind || !samePrompt(previous, guidance)) {
      this.takeDown(previous, guidance);
    }
    switch (guidance.kind) {
      case 'prompt': {
        const item = this.findItem(guidance.itemId, guidance.resource);
        if (item && !item.isSelectPromptVisible()) {
          const [labelWidth, containerWidth] = PROMPT_WIDTHS[guidance.text] ?? ['150px', '200px'];
          item.showSelectPromptVisualization(guidance.text, labelWidth, containerWidth);
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
        this.renderService.showPlaceMarker(placeConfig, this.placeMarkerConfig());
        break;
      case 'group':
        this.renderService.touchSelectionMode.setAsked(true);
        if (guidance.arrow) {
          this.showArrow(guidance.arrow.x, guidance.arrow.y);
        } else {
          this.renderService.showOutOfViewMarker(null, 0);
        }
        break;
      case 'quiet':
        break;
    }
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
      this.findItem(previous.itemId, previous.resource)?.hideSelectPromptVisualization();
    }
    const arrowNext = next.kind === 'arrow' || (next.kind === 'group' && next.arrow !== null);
    if (!arrowNext) {
      this.renderService.showOutOfViewMarker(null, 0);
    }
    if (previous.kind === 'button' && next.kind !== 'button') {
      this.itemCockpit()?.showBuildupTip(null);
    }
    if (previous.kind === 'placeMarker') {
      this.renderService.showPlaceMarker(null, null);
    }
    if (previous.kind === 'group' && next.kind !== 'group') {
      this.renderService.touchSelectionMode.setAsked(false);
    }
  }

  private showArrow(x: number, y: number): void {
    const viewField = this.renderService.getCurrentViewField();
    if (!viewField) {
      return;
    }
    this.renderService.showOutOfViewMarker(this.outOfViewMarkerConfig,
      viewField.getAngleTo(GwtInstance.newDecimalPosition(x, y)));
  }

  private findItem(id: number, resource: boolean): {
    isSelectPromptVisible(): boolean,
    showSelectPromptVisualization(text: string, labelWidth: string, containerWidth: string): void,
    hideSelectPromptVisualization(): void
  } | null {
    if (resource) {
      return this.renderService.getBabylonResourceItemImpls().find(item => item.getId() === id) ?? null;
    }
    return this.renderService.getBabylonBaseItemById(id);
  }
}

function samePrompt(one: Guidance, other: Guidance): boolean {
  if (one.kind !== 'prompt' || other.kind !== 'prompt') {
    return true;
  }
  return one.itemId === other.itemId && one.text === other.text && one.resource === other.resource;
}
