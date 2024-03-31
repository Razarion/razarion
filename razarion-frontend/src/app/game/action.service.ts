import { Injectable } from '@angular/core';
import { ItemType, Diplomacy, BaseItemType } from '../gwtangular/GwtAngularFacade';
import { GwtAngularService } from '../gwtangular/GwtAngularService';
import { GwtInstance } from '../gwtangular/GwtInstance';


export class SelectionInfo {
  hasOwnSelection: boolean = false;
  hasOwnMovable: boolean = false;
  hasAttackers: boolean = false;
  canAttack: boolean = false;
  hasHarvesters: boolean = false;
}

@Injectable({
  providedIn: 'root'
})
export class ActionService {
  private readonly cursorTypeHandlers: ((selectionInfo: SelectionInfo) => void)[] = [];

  constructor(private gwtAngularService: GwtAngularService) {
  }

  onItemClicked(itemType: ItemType, id: number, diplomacy: Diplomacy) {
    let targetItemType: BaseItemType | undefined = undefined;

    if (diplomacy === Diplomacy.OWN) {
      this.gwtAngularService.gwtAngularFacade.inputService.ownItemClicked(id, <BaseItemType>itemType);
    } else if (diplomacy === Diplomacy.FRIEND) {
      this.gwtAngularService.gwtAngularFacade.inputService.friendItemClicked(id);
    } else if (diplomacy === Diplomacy.ENEMY) {
      this.gwtAngularService.gwtAngularFacade.inputService.enemyItemClicked(id);
      targetItemType = <BaseItemType>itemType;
    } else if (diplomacy === Diplomacy.RESOURCE) {
      this.gwtAngularService.gwtAngularFacade.inputService.resourceItemClicked(id);
    } else if (diplomacy === Diplomacy.BOX) {
      this.gwtAngularService.gwtAngularFacade.inputService.boxItemClicked(id);
    }

    let selectionInfo = this.setupSelectionInfo(targetItemType);
    this.cursorTypeHandlers.forEach(cursorTypeHandler => cursorTypeHandler(selectionInfo));
  }

  onTerrainClicked(xTerrainPosition: number, yTerrainPosition: number) {
    this.gwtAngularService.gwtAngularFacade.inputService.terrainClicked(GwtInstance.newDecimalPosition(xTerrainPosition, yTerrainPosition));
  }

  addCursoHandler(cursorTypeHandler: (selectionInfo: SelectionInfo) => void) {
    this.cursorTypeHandlers.push(cursorTypeHandler);
  }

  removeCursoHandler(cursorTypeHandler: (selectionInfo: SelectionInfo) => void) {
    this.cursorTypeHandlers.splice(this.cursorTypeHandlers.indexOf(cursorTypeHandler), 1);
  }

  private setupSelectionInfo(target?: BaseItemType): SelectionInfo {
    let selectionInfo = new SelectionInfo();
    selectionInfo.hasOwnSelection = this.gwtAngularService.gwtAngularFacade.selectionHandler.hasOwnSelection();
    if (selectionInfo.hasOwnSelection) {
      selectionInfo.hasOwnMovable = this.gwtAngularService.gwtAngularFacade.selectionHandler.hasOwnMovable();
    }
    if (target) {
      selectionInfo.hasAttackers = this.gwtAngularService.gwtAngularFacade.selectionHandler.hasAttackers();
      selectionInfo.canAttack = this.gwtAngularService.gwtAngularFacade.selectionHandler.canAttack(target.getId());
    }
    selectionInfo.hasHarvesters = this.gwtAngularService.gwtAngularFacade.selectionHandler.hasHarvesters();
    return selectionInfo;
  }
}
