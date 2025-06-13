import {TipTaskContainer} from './tip-task.container';
import {SelectTipTask} from './tiptask/select-tip-task';
import {TipService} from './tip.service';
import {QuestConfig, Tip} from '../../gwtangular/GwtAngularFacade';
import {StartBuildPlacerTipTask} from './tiptask/start-build-placer-tip-task';
import {SendBuildCommandTipTask} from './tiptask/send-build-command-tip-task';
import {IdleItemTipTask} from './tiptask/idle-item-tip-task';
import {GwtHelper} from '../../gwtangular/GwtHelper';

export class TipTaskFactory {

  static create(questConfig: QuestConfig, tipService: TipService): TipTaskContainer | null {
    switch (questConfig.getTipConfig()!.getTip()) {
      case Tip.BUILD:
        return TipTaskFactory.createBuiltFactory(questConfig, tipService);
      default:
        return null;
    }
  }

  private static createBuiltFactory(questConfig: QuestConfig, tipService: TipService): TipTaskContainer {
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

}
