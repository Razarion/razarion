import {Injectable} from '@angular/core';
import {GwtAngularFacade, MarkerConfig, QuestConfig} from '../../gwtangular/GwtAngularFacade';
import {BabylonRenderServiceAccessImpl} from '../renderer/babylon-render-service-access-impl.service';
import {ItemCockpitComponent} from '../cockpit/item/item-cockpit.component';
import {GwtAngularService} from '../../gwtangular/GwtAngularService';
import {SelectionService} from '../selection.service';
import {UiSettingsService} from '../ui-settings.service';
import {TipStallTrackerService} from './tip-stall-tracker.service';
import {FirstInteractionTrackerService} from '../tracking/first-interaction-tracker.service';
import {ActionService} from '../action.service';
import {TipGuide} from './guide/tip-guide';

/**
 * The quest tip: shown for a quest that has a tip config, taken down when the quest ends or tips are
 * switched off. What it shows is decided by the guide (docs/architecture/quest-tip-redesign.md).
 */
@Injectable({
  providedIn: 'root'
})
export class TipService {
  private itemCockpit: ItemCockpitComponent | null = null;
  private readonly guide: TipGuide;

  private readonly outOfViewMarkerConfig: MarkerConfig = {
    radius: 10,
    nodesMaterialId: null,
    placeNodesMaterialId: null,
    outOfViewNodesMaterialId: 12, // TODO: Load from config
    outOfViewSize: 1,
    outOfViewDistanceFromCamera: 3
  };

  constructor(
    renderService: BabylonRenderServiceAccessImpl,
    private readonly gwtAngularService: GwtAngularService,
    selectionService: SelectionService,
    private readonly uiSettingsService: UiSettingsService,
    tipStallTrackerService: TipStallTrackerService,
    firstInteractionTracker: FirstInteractionTrackerService,
    actionService: ActionService
  ) {
    this.guide = new TipGuide({
      renderService,
      selectionService,
      actionService,
      facade: () => this.gwtAngularFacade,
      itemCockpit: () => this.itemCockpit,
      stallTracker: tipStallTrackerService,
      firstInteractionTracker,
      outOfViewMarkerConfig: this.outOfViewMarkerConfig
    });
    // Turning tips off (e.g. for clean director footage) clears any active tip.
    this.uiSettingsService.tipsVisible$.subscribe(visible => {
      if (!visible) {
        this.deactivate();
      }
    });
  }

  get gwtAngularFacade(): GwtAngularFacade {
    return this.gwtAngularService.gwtAngularFacade;
  }

  public activate(questConfig: QuestConfig): void {
    this.deactivate();
    if (!this.uiSettingsService.tipsVisible) {
      return; // tips disabled (e.g. director/filming mode)
    }
    this.guide.activate(questConfig);
  }

  public deactivate(): void {
    this.guide.deactivate();
  }

  public setItemCockpit(itemCockpit: ItemCockpitComponent | null) {
    this.itemCockpit = itemCockpit;
  }
}
