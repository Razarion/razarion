import {TipTaskContainer} from './tip-task.container';
import {SelectTipTask} from './tiptask/select-tip-task';
import {SelectGroupTipTask} from './tiptask/select-group-tip-task';
import {TipService} from './tip.service';
import {QuestConfig, Tip} from '../../gwtangular/GwtAngularFacade';
import {StartBuildPlacerTipTask} from './tiptask/start-build-placer-tip-task';
import {SendBuildCommandTipTask} from './tiptask/send-build-command-tip-task';
import {IdleItemTipTask} from './tiptask/idle-item-tip-task';
import {GwtHelper} from '../../gwtangular/GwtHelper';
import {SendFabricateCommandTipTask} from './tiptask/send-fabricate-command-tip-task';
import {SendHarvestCommandTipTask} from './tiptask/send-harvest-command-tip-task';
import {SendAttackCommandTipTask} from './tiptask/send-attack-command-tip-task';

export class TipTaskFactory {

  static create(questConfig: QuestConfig, tipService: TipService): TipTaskContainer | null {
    let tip = TipTaskFactory.mapStringToTip(questConfig.getTipConfig()!.getTipString());
    switch (tip) {
      case Tip.BUILD:
        return TipTaskFactory.createBuilt(questConfig, tipService);
      case Tip.FABRICATE:
        return TipTaskFactory.createFabricate(questConfig, tipService);
      case Tip.HARVEST:
        return TipTaskFactory.createHarvest(questConfig, tipService);
      case Tip.ATTACK:
        return TipTaskFactory.createAttack(questConfig, tipService);
      default:
        return null;
    }
  }

  private static createBuilt(questConfig: QuestConfig, tipService: TipService): TipTaskContainer {
    let tipConfig = questConfig.getTipConfig()!;
    let comparisonConfig = questConfig.getConditionConfig()?.getComparisonConfig();
    let toBeBuiltItemTypeId = GwtHelper.gwtIssueNumber(comparisonConfig?.toTypeCountAngular()[0][0])!;
    let placeConfig = comparisonConfig?.getPlaceConfig() ?? null;
    let tipTaskContainer = new TipTaskContainer(tipService.renderService);
    tipTaskContainer.add(new SelectTipTask(tipConfig, tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.add(new StartBuildPlacerTipTask(toBeBuiltItemTypeId, tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.add(new SendBuildCommandTipTask(toBeBuiltItemTypeId, placeConfig, tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.addFallback(new IdleItemTipTask(tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.addFallback(new SelectTipTask(tipConfig, tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.addFallback(new StartBuildPlacerTipTask(toBeBuiltItemTypeId, tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.addFallback(new SendBuildCommandTipTask(toBeBuiltItemTypeId, placeConfig, tipService, tipTaskContainer.tipTaskContext));
    return tipTaskContainer;
  }

  private static createFabricate(questConfig: QuestConfig, tipService: TipService): TipTaskContainer {
    let tipConfig = questConfig.getTipConfig()!;
    let toBeBuiltItemTypeId = GwtHelper.gwtIssueNumber(questConfig.getConditionConfig()?.getComparisonConfig().toTypeCountAngular()[0][0])!;
    let tipTaskContainer = new TipTaskContainer(tipService.renderService);
    tipTaskContainer.add(new SelectTipTask(tipConfig, tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.add(new SendFabricateCommandTipTask(toBeBuiltItemTypeId, tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.addFallback(new IdleItemTipTask(tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.addFallback(new SelectTipTask(tipConfig, tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.addFallback(new SendFabricateCommandTipTask(toBeBuiltItemTypeId, tipService, tipTaskContainer.tipTaskContext));
    return tipTaskContainer;
  }

  private static createHarvest(questConfig: QuestConfig, tipService: TipService): TipTaskContainer {
    let tipConfig = questConfig.getTipConfig()!;
    let tipTaskContainer = new TipTaskContainer(tipService.renderService);

    tipTaskContainer.add(new SelectTipTask(tipConfig, tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.add(new SendHarvestCommandTipTask(tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.addFallback(new IdleItemTipTask(tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.addFallback(new SelectTipTask(tipConfig, tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.addFallback(new SendHarvestCommandTipTask(tipService, tipTaskContainer.tipTaskContext));
    return tipTaskContainer;
  }

  private static createAttack(questConfig: QuestConfig, tipService: TipService): TipTaskContainer {
    let tipConfig = questConfig.getTipConfig()!;
    let typeCount = questConfig.getConditionConfig()?.getComparisonConfig().toTypeCountAngular();
    let enemyItemTypeId: number | null = null;
    if (typeCount && typeCount.length > 0) {
      enemyItemTypeId = GwtHelper.gwtIssueNumber(typeCount[0][0]);
    }
    let tipTaskContainer = new TipTaskContainer(tipService.renderService);

    /*
     * The group is asked for only when the quest names what to destroy.
     *
     * A quest that says "destroy anything" is satisfied by the nearest enemy, and on planet 117
     * that is an undefended extractor 33 units from the player's base - one unit is plenty. A quest
     * that names a target has to be carried to wherever that target stands: the refinery of 379 is
     * 111 units away behind teslas that out-range a viper.
     *
     * This is not a rule derived from the two quests it happens to separate. It was added after
     * asking every attack quest for a group cost quest 365 - the first fight in the game, at level
     * 2, "destroy anything" - twenty points of its pass rate in a day: 83% over the six days before,
     * 64% and 62% on the two days after, with one player in five stalling on a group tip that had
     * not existed the day before. Asking somebody to gather an army before their first shot at an
     * unarmed extractor is a hurdle where the game meant to have none.
     */
    tipTaskContainer.add(new SelectTipTask(tipConfig, tipService, tipTaskContainer.tipTaskContext));
    if (enemyItemTypeId !== null) {
      tipTaskContainer.add(new SelectGroupTipTask(tipConfig, tipService, tipTaskContainer.tipTaskContext));
    }
    tipTaskContainer.add(new SendAttackCommandTipTask(enemyItemTypeId, tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.addFallback(new IdleItemTipTask(tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.addFallback(new SelectTipTask(tipConfig, tipService, tipTaskContainer.tipTaskContext));
    if (enemyItemTypeId !== null) {
      tipTaskContainer.addFallback(new SelectGroupTipTask(tipConfig, tipService, tipTaskContainer.tipTaskContext));
    }
    tipTaskContainer.addFallback(new SendAttackCommandTipTask(enemyItemTypeId, tipService, tipTaskContainer.tipTaskContext));
    return tipTaskContainer;
  }

  private static mapStringToTip(tipString: string): Tip {
    if (Object.values(Tip).includes(tipString as Tip)) {
      return tipString as Tip;
    }
    throw new Error(`Unknown tipString ${tipString}`);
  }

}
