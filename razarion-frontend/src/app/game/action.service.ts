import { Injectable } from '@angular/core';
import { ItemType, Diplomacy, BaseItemType, ActionServiceListener } from '../gwtangular/GwtAngularFacade';
import { GwtAngularService } from '../gwtangular/GwtAngularService';
import { GwtInstance } from '../gwtangular/GwtInstance';
import { BabylonAudioService } from './renderer/babylon-audio.service';


export class SelectionInfo {
  hasOwnSelection: boolean = false;
  hasOwnMovable: boolean = false;
  hasAttackers: boolean = false;
  hasHarvesters: boolean = false;
}

@Injectable({
  providedIn: 'root'
})
export class ActionService implements ActionServiceListener {
  private readonly cursorTypeHandlers: ((selectionInfo: SelectionInfo) => void)[] = [];

  constructor(private gwtAngularService: GwtAngularService,
              private babylonAudioService: BabylonAudioService) {
  }

  onSelectionChanged(): void {
    let selectionInfo = this.setupSelectionInfo();
    this.cursorTypeHandlers.forEach(cursorTypeHandler => cursorTypeHandler(selectionInfo));
  }

  onItemClicked(itemType: ItemType, id: number, diplomacy: Diplomacy) {
    const selectionService = this.gwtAngularService.gwtAngularFacade.selectionService;

    if (diplomacy === Diplomacy.OWN) {
      if (selectionService.hasOwnSelection()) {
        this.babylonAudioService.playCommandSentAudio();
      }
      this.gwtAngularService.gwtAngularFacade.inputService.ownItemClicked(id);
    } else if (diplomacy === Diplomacy.FRIEND) {
      this.gwtAngularService.gwtAngularFacade.inputService.friendItemClicked(id);
    } else if (diplomacy === Diplomacy.ENEMY) {
      if (selectionService.hasAttackers()) {
        this.babylonAudioService.playCommandSentAudio();
      }
      this.gwtAngularService.gwtAngularFacade.inputService.enemyItemClicked(id);
    } else if (diplomacy === Diplomacy.RESOURCE) {
      if (selectionService.hasHarvesters()) {
        this.babylonAudioService.playCommandSentAudio();
      }
      this.gwtAngularService.gwtAngularFacade.inputService.resourceItemClicked(id);
    } else if (diplomacy === Diplomacy.BOX) {
      if (selectionService.hasOwnMovable()) {
        this.babylonAudioService.playCommandSentAudio();
      }
      this.gwtAngularService.gwtAngularFacade.inputService.boxItemClicked(id);
    }

  }

  onTerrainClicked(xTerrainPosition: number, yTerrainPosition: number) {
    if (this.gwtAngularService.gwtAngularFacade.selectionService.hasOwnMovable()) {
      this.babylonAudioService.playCommandSentAudio();
    }
    this.gwtAngularService.gwtAngularFacade.inputService.terrainClicked(GwtInstance.newDecimalPosition(xTerrainPosition, yTerrainPosition));
  }

  addCursorHandler(cursorTypeHandler: (selectionInfo: SelectionInfo) => void) {
    this.cursorTypeHandlers.push(cursorTypeHandler);
  }

  removeCursorHandler(cursorTypeHandler: (selectionInfo: SelectionInfo) => void) {
    this.cursorTypeHandlers.splice(this.cursorTypeHandlers.indexOf(cursorTypeHandler), 1);
  }

  setupSelectionInfo(): SelectionInfo {
    let selectionInfo = new SelectionInfo();
    selectionInfo.hasOwnSelection = this.gwtAngularService.gwtAngularFacade.selectionService.hasOwnSelection();
    if (selectionInfo.hasOwnSelection) {
      selectionInfo.hasOwnMovable = this.gwtAngularService.gwtAngularFacade.selectionService.hasOwnMovable();
    }
    selectionInfo.hasAttackers = this.gwtAngularService.gwtAngularFacade.selectionService.hasAttackers();
    selectionInfo.hasHarvesters = this.gwtAngularService.gwtAngularFacade.selectionService.hasHarvesters();
    return selectionInfo;
  }
}
