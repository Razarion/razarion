import {TipTaskContainer} from './tip-task.container';
import {SelectTipTask} from './tiptask/select-tip-task';
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
    let toBeBuiltItemTypeId = GwtHelper.gwtIssueNumber(questConfig.getConditionConfig()?.getComparisonConfig().toTypeCountAngular()[0][0])!;
    let tipTaskContainer = new TipTaskContainer();
    tipTaskContainer.add(new SelectTipTask(tipConfig, tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.add(new StartBuildPlacerTipTask(toBeBuiltItemTypeId, tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.add(new SendBuildCommandTipTask(toBeBuiltItemTypeId, tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.addFallback(new IdleItemTipTask(tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.addFallback(new SelectTipTask(tipConfig, tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.addFallback(new StartBuildPlacerTipTask(toBeBuiltItemTypeId, tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.addFallback(new SendBuildCommandTipTask(toBeBuiltItemTypeId, tipService, tipTaskContainer.tipTaskContext));
    return tipTaskContainer;
  }

  private static createFabricate(questConfig: QuestConfig, tipService: TipService): TipTaskContainer {
    let tipConfig = questConfig.getTipConfig()!;
    let toBeBuiltItemTypeId = GwtHelper.gwtIssueNumber(questConfig.getConditionConfig()?.getComparisonConfig().toTypeCountAngular()[0][0])!;
    let tipTaskContainer = new TipTaskContainer();
    tipTaskContainer.add(new SelectTipTask(tipConfig, tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.add(new SendFabricateCommandTipTask(toBeBuiltItemTypeId, tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.addFallback(new IdleItemTipTask(tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.addFallback(new SelectTipTask(tipConfig, tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.addFallback(new SendFabricateCommandTipTask(toBeBuiltItemTypeId, tipService, tipTaskContainer.tipTaskContext));
    return tipTaskContainer;
  }

  private static createHarvest(questConfig: QuestConfig, tipService: TipService): TipTaskContainer {
    let tipConfig = questConfig.getTipConfig()!;
    let tipTaskContainer = new TipTaskContainer();

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
    let tipTaskContainer = new TipTaskContainer();

    tipTaskContainer.add(new SelectTipTask(tipConfig, tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.add(new SendAttackCommandTipTask(enemyItemTypeId, tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.addFallback(new IdleItemTipTask(tipService, tipTaskContainer.tipTaskContext));
    tipTaskContainer.addFallback(new SelectTipTask(tipConfig, tipService, tipTaskContainer.tipTaskContext));
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
