import { Injectable } from '@angular/core';
import { BabylonItem, ItemType, Diplomacy, GameCommandService } from '../gwtangular/GwtAngularFacade';
import { GwtAngularService } from '../gwtangular/GwtAngularService';
import { BabylonAudioService } from './renderer/babylon-audio.service';
import { SelectionService as TsSelectionService } from './selection.service';
import { BabylonRenderServiceAccessImpl } from './renderer/babylon-render-service-access-impl.service';


export class SelectionInfo {
  hasOwnSelection: boolean = false;
  hasOwnMovable: boolean = false;
  hasAttackers: boolean = false;
  hasHarvesters: boolean = false;
}

@Injectable({
  providedIn: 'root'
})
export class ActionService {
  private readonly cursorTypeHandlers: ((selectionInfo: SelectionInfo) => void)[] = [];
  private rendererService: BabylonRenderServiceAccessImpl | null = null;
  private hasPendingMoveCommand = false;
  private queuedMoveCommand: { movableIds: number[], x: number, y: number } | null = null;
  private moveAckCallbackRegistered = false;

  constructor(private gwtAngularService: GwtAngularService,
              private babylonAudioService: BabylonAudioService,
              private tsSelectionService: TsSelectionService) {
    // Listen for TS selection changes to update cursors
    this.tsSelectionService.addSelectionListener(() => this.onSelectionChanged());
  }

  setRendererService(rendererService: BabylonRenderServiceAccessImpl): void {
    this.rendererService = rendererService;
  }

  private get gameCommandService(): GameCommandService {
    return this.gwtAngularService.gwtAngularFacade.gameCommandService;
  }

  private ensureMoveAckCallback(): void {
    if (!this.moveAckCallbackRegistered) {
      this.gameCommandService.setMoveCommandAckCallback(() => this.onMoveCommandAck());
      this.moveAckCallbackRegistered = true;
    }
  }

  onSelectionChanged(): void {
    const selectionInfo = this.setupSelectionInfo();
    this.cursorTypeHandlers.forEach(cursorTypeHandler => cursorTypeHandler(selectionInfo));
  }

  onItemClicked(itemType: ItemType, id: number, diplomacy: Diplomacy, babylonItem: BabylonItem) {
    if (diplomacy === Diplomacy.OWN) {
      this.handleOwnItemClicked(id);
    } else if (diplomacy === Diplomacy.FRIEND) {
      this.handleFriendItemClicked(id, babylonItem);
    } else if (diplomacy === Diplomacy.ENEMY) {
      this.handleEnemyItemClicked(id, itemType.getId(), babylonItem);
    } else if (diplomacy === Diplomacy.RESOURCE) {
      this.handleResourceItemClicked(id, itemType.getId(), babylonItem);
    } else if (diplomacy === Diplomacy.BOX) {
      this.handleBoxItemClicked(id, itemType.getId(), babylonItem);
    }
  }

  onTerrainClicked(xTerrainPosition: number, yTerrainPosition: number) {
    if (!this.tsSelectionService.hasOwnSelection()) {
      return;
    }
    const movableIds = this.tsSelectionService.getMovableIds();
    if (movableIds.length === 0) {
      return;
    }
    this.babylonAudioService.playCommandSentAudio();
    this.ensureMoveAckCallback();
    if (this.hasPendingMoveCommand) {
      this.queuedMoveCommand = { movableIds, x: xTerrainPosition, y: yTerrainPosition };
    } else {
      this.gameCommandService.moveCmd(movableIds, xTerrainPosition, yTerrainPosition);
    }
    this.hasPendingMoveCommand = true;
  }

  addCursorHandler(cursorTypeHandler: (selectionInfo: SelectionInfo) => void) {
    this.cursorTypeHandlers.push(cursorTypeHandler);
  }

  removeCursorHandler(cursorTypeHandler: (selectionInfo: SelectionInfo) => void) {
    this.cursorTypeHandlers.splice(this.cursorTypeHandlers.indexOf(cursorTypeHandler), 1);
  }

  setupSelectionInfo(): SelectionInfo {
    const selectionInfo = new SelectionInfo();
    selectionInfo.hasOwnSelection = this.tsSelectionService.hasOwnSelection();
    if (selectionInfo.hasOwnSelection) {
      selectionInfo.hasOwnMovable = this.tsSelectionService.hasMovables();
      selectionInfo.hasAttackers = this.tsSelectionService.hasAttackers();
      selectionInfo.hasHarvesters = this.tsSelectionService.hasHarvesters();
    }
    return selectionInfo;
  }

  selectRectangle(xStart: number, yStart: number, width: number, height: number): void {
    if (!this.rendererService) return;

    const xEnd = xStart + width;
    const yEnd = yStart + height;

    // Find own base items in rectangle
    // Rectangle coords are in Babylon space (X/Z = ground plane)
    // Item positions are Razarion Vertex (X/Y = ground plane, Z = height)
    const ownItems = this.rendererService.getBabylonBaseItemsByDiplomacy(Diplomacy.OWN)
      .filter(item => {
        const pos = item.getPosition();
        if (!pos) return false;
        const x = pos.getX();
        const y = pos.getY();
        return x >= xStart && x <= xEnd && y >= yStart && y <= yEnd;
      });

    if (ownItems.length > 0) {
      this.tsSelectionService.selectOwnItems(ownItems);
      return;
    }

    // No own items — check resources
    const resources = this.rendererService.getBabylonResourceItemImpls()
      .filter(item => {
        const pos = item.getPosition();
        if (!pos) return false;
        const x = pos.getX();
        const y = pos.getY();
        return x >= xStart && x <= xEnd && y >= yStart && y <= yEnd;
      });

    if (resources.length > 0) {
      this.tsSelectionService.selectOther(resources[0].getId(), Diplomacy.RESOURCE, resources[0].itemType.getId(), undefined, resources[0]);
      return;
    }

    this.tsSelectionService.clearSelection();
  }

  // --- Private click handlers ---

  private handleOwnItemClicked(id: number): void {
    const item = this.rendererService?.getBabylonBaseItemById(id);
    if (!item) return;

    if (this.tsSelectionService.hasOwnSelection()) {
      const baseItemType = item.getBaseItemType();

      // Fully built container? → load selected items into it
      if (item.getBuildup() >= 1.0 && baseItemType.getItemContainerType() != null) {
        const containableIds = this.tsSelectionService.getContainableIds(baseItemType);
        if (containableIds.length > 0) {
          this.babylonAudioService.playCommandSentAudio();
          this.gameCommandService.loadContainerCmd(containableIds, id);
          return;
        }
      }

      // Not fully built? → finalize build with selected builders
      if (item.getBuildup() < 1.0) {
        const builderIds = this.tsSelectionService.getBuilderIds(baseItemType.getId());
        if (builderIds.length > 0) {
          this.babylonAudioService.playCommandSentAudio();
          this.gameCommandService.finalizeBuildCmd(builderIds, id);
          return;
        }
      }
    }

    // Default: select the clicked item
    this.tsSelectionService.selectOwnItems([item]);
  }

  private handleFriendItemClicked(id: number, babylonItem: BabylonItem): void {
    const item = this.rendererService?.getBabylonBaseItemById(id);
    if (item) {
      this.tsSelectionService.selectOther(id, Diplomacy.FRIEND, item.getBaseItemType().getId(), item.getBaseId(), babylonItem);
    }
  }

  private handleEnemyItemClicked(id: number, itemTypeId: number, babylonItem: BabylonItem): void {
    const item = this.rendererService?.getBabylonBaseItemById(id);
    if (!item) return;

    if (this.tsSelectionService.hasOwnSelection()) {
      const attackerIds = this.tsSelectionService.getAttackerIds(item.getBaseItemType().getId());
      if (attackerIds.length > 0) {
        this.babylonAudioService.playCommandSentAudio();
        this.gameCommandService.attackCmd(attackerIds, id);
        return;
      }
    }
    this.tsSelectionService.selectOther(id, Diplomacy.ENEMY, itemTypeId, item.getBaseId(), babylonItem);
  }

  private handleResourceItemClicked(id: number, itemTypeId: number, babylonItem: BabylonItem): void {
    if (this.tsSelectionService.hasOwnSelection()) {
      const harvesterIds = this.tsSelectionService.getHarvesterIds();
      if (harvesterIds.length > 0) {
        this.babylonAudioService.playCommandSentAudio();
        this.gameCommandService.harvestCmd(harvesterIds, id);
        return;
      }
    }
    this.tsSelectionService.selectOther(id, Diplomacy.RESOURCE, itemTypeId, undefined, babylonItem);
  }

  private handleBoxItemClicked(id: number, itemTypeId: number, babylonItem: BabylonItem): void {
    if (this.tsSelectionService.hasOwnSelection()) {
      const movableIds = this.tsSelectionService.getMovableIds();
      if (movableIds.length > 0) {
        this.babylonAudioService.playCommandSentAudio();
        this.gameCommandService.pickBoxCmd(movableIds, id);
        return;
      }
    }
    this.tsSelectionService.selectOther(id, Diplomacy.BOX, itemTypeId, undefined, babylonItem);
  }

  private onMoveCommandAck(): void {
    if (!this.hasPendingMoveCommand) {
      return;
    }
    if (this.queuedMoveCommand != null) {
      this.gameCommandService.moveCmd(
        this.queuedMoveCommand.movableIds,
        this.queuedMoveCommand.x,
        this.queuedMoveCommand.y
      );
    } else {
      this.hasPendingMoveCommand = false;
    }
    this.queuedMoveCommand = null;
  }
}
