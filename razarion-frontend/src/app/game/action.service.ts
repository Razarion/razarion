import { Injectable } from '@angular/core';
import { BabylonItem, ItemType, Diplomacy, GameCommandService, Vertex } from '../gwtangular/GwtAngularFacade';
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
  private moveAckTimeout: ReturnType<typeof setTimeout> | null = null;
  /**
   * Only one move command is in flight at a time, so a click storm does not queue up an A* search
   * per click. The gate opens again when the game engine acknowledges. If that acknowledgement
   * never arrives the gate used to stay shut for the rest of the session: every later click was
   * silently parked in queuedMoveCommand and nothing ever moved again. This is how long we wait
   * before assuming the acknowledgement is lost and opening the gate ourselves.
   */
  private static readonly MOVE_ACK_TIMEOUT_MS = 2000;

  constructor(private gwtAngularService: GwtAngularService,
              private babylonAudioService: BabylonAudioService,
              private tsSelectionService: TsSelectionService) {
    // Listen for TS selection changes to update cursors
    this.tsSelectionService.addSelectionListener(() => this.onSelectionChanged());
  }

  setRendererService(rendererService: BabylonRenderServiceAccessImpl): void {
    this.rendererService = rendererService;
    window.addEventListener('keydown', (event: KeyboardEvent) => {
      if (event.key === 'Escape' && !this.rendererService?.baseItemPlacerActive) {
        this.tsSelectionService.clearSelection();
      }
    });
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
      this.handleFriendItemClicked(id, babylonItem, itemType.getName());
    } else if (diplomacy === Diplomacy.ENEMY) {
      this.handleEnemyItemClicked(id, itemType.getId(), babylonItem, itemType.getName());
    } else if (diplomacy === Diplomacy.RESOURCE) {
      this.handleResourceItemClicked(id, itemType.getId(), babylonItem, itemType.getName());
    } else if (diplomacy === Diplomacy.BOX) {
      this.handleBoxItemClicked(id, itemType.getId(), babylonItem, itemType.getName());
    }
  }

  onTerrainClicked(xTerrainPosition: number, yTerrainPosition: number) {
    if (this.rendererService?.baseItemPlacerActive) {
      return;
    }
    if (!this.tsSelectionService.hasOwnSelection()) {
      return;
    }
    const movableIds = this.tsSelectionService.getMovableIds();
    if (movableIds.length === 0) {
      return;
    }

    this.babylonAudioService.speakCommand('Moving out');
    this.ensureMoveAckCallback();
    if (this.hasPendingMoveCommand) {
      this.queuedMoveCommand = { movableIds, x: xTerrainPosition, y: yTerrainPosition };
    } else {
      this.sendMoveCommand(movableIds, xTerrainPosition, yTerrainPosition);
    }
  }

  private sendMoveCommand(movableIds: number[], x: number, y: number): void {
    this.gameCommandService.moveCmd(movableIds, x, y);
    this.hasPendingMoveCommand = true;
    if (this.moveAckTimeout) {
      clearTimeout(this.moveAckTimeout);
    }
    this.moveAckTimeout = setTimeout(() => {
      this.moveAckTimeout = null;
      // Worth a line: the engine acknowledges even when the command failed, so reaching this means
      // the command or its acknowledgement was lost on the way - a defect, not a slow tick.
      console.warn('Move command was not acknowledged within '
        + ActionService.MOVE_ACK_TIMEOUT_MS + ' ms - releasing the gate so the next click works');
      this.hasPendingMoveCommand = false;
      const queued = this.queuedMoveCommand;
      this.queuedMoveCommand = null;
      if (queued) {
        this.sendMoveCommand(queued.movableIds, queued.x, queued.y);
      }
    }, ActionService.MOVE_ACK_TIMEOUT_MS);
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

  /**
   * Select everything inside the on-screen marquee. Bounds are in canvas pixel coordinates (the same
   * space as the green selection overlay). Each candidate's ground position is projected to screen
   * and tested against the pixel rectangle — this matches what the user drew exactly. (The previous
   * world-space box test, built from only the two drag corners, drifted under the tilted perspective
   * camera and dropped units near the view edges.)
   */
  selectScreenRectangle(xStart: number, yStart: number, xEnd: number, yEnd: number): void {
    if (!this.rendererService) return;
    const rendererService = this.rendererService;

    const inRect = (item: { getPosition(): Vertex | null }): boolean => {
      const pos = item.getPosition();
      if (!pos) return false;
      const screen = rendererService.projectGroundPositionToScreen(pos.getX(), pos.getY(), pos.getZ());
      if (!screen) return false;
      return screen.x >= xStart && screen.x <= xEnd && screen.y >= yStart && screen.y <= yEnd;
    };

    // Find own base items in the rectangle
    const ownItems = this.rendererService.getBabylonBaseItemsByDiplomacy(Diplomacy.OWN)
      .filter(inRect);

    if (ownItems.length > 0) {
      this.tsSelectionService.selectOwnItems(ownItems);
      return;
    }

    // No own items — check resources
    const resources = this.rendererService.getBabylonResourceItemImpls()
      .filter(inRect);

    if (resources.length > 0) {
      this.tsSelectionService.selectOther(resources[0].getId(), Diplomacy.RESOURCE, resources[0].itemType.getId(), undefined, resources[0], resources[0].itemType.getName());
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
      
          this.babylonAudioService.speakCommand('Loading up');
          this.gameCommandService.loadContainerCmd(containableIds, id);
          return;
        }
      }

      // Not fully built? → finalize build with selected builders
      if (item.getBuildup() < 1.0) {
        const builderIds = this.tsSelectionService.getBuilderIds(baseItemType.getId());
        if (builderIds.length > 0) {
      
          this.babylonAudioService.speakCommand('Completing construction');
          this.gameCommandService.finalizeBuildCmd(builderIds, id);
          return;
        }
      }
    }

    // Default: select the clicked item
    this.tsSelectionService.selectOwnItems([item]);
  }

  private handleFriendItemClicked(id: number, babylonItem: BabylonItem, itemTypeName: string): void {
    const item = this.rendererService?.getBabylonBaseItemById(id);
    if (item) {
      this.tsSelectionService.selectOther(id, Diplomacy.FRIEND, item.getBaseItemType().getId(), item.getBaseId(), babylonItem, itemTypeName);
    }
  }

  private handleEnemyItemClicked(id: number, itemTypeId: number, babylonItem: BabylonItem, itemTypeName: string): void {
    const item = this.rendererService?.getBabylonBaseItemById(id);
    if (!item) return;

    if (this.tsSelectionService.hasOwnSelection()) {
      const attackerIds = this.tsSelectionService.getAttackerIds(item.getBaseItemType().getId());
      if (attackerIds.length > 0) {
        this.babylonAudioService.speakCommand('Engaging target');
        this.gameCommandService.attackCmd(attackerIds, id);
        return;
      }
    }
    this.tsSelectionService.selectOther(id, Diplomacy.ENEMY, itemTypeId, item.getBaseId(), babylonItem, itemTypeName);
  }

  private handleResourceItemClicked(id: number, itemTypeId: number, babylonItem: BabylonItem, itemTypeName: string): void {
    if (this.tsSelectionService.hasOwnSelection()) {
      const harvesterIds = this.tsSelectionService.getHarvesterIds();
      if (harvesterIds.length > 0) {
        this.babylonAudioService.speakCommand('Harvesting');
        this.gameCommandService.harvestCmd(harvesterIds, id);
        return;
      }
    }
    this.tsSelectionService.selectOther(id, Diplomacy.RESOURCE, itemTypeId, undefined, babylonItem, itemTypeName);
  }

  private handleBoxItemClicked(id: number, itemTypeId: number, babylonItem: BabylonItem, itemTypeName: string): void {
    if (this.tsSelectionService.hasOwnSelection()) {
      const movableIds = this.tsSelectionService.getMovableIds();
      if (movableIds.length > 0) {
        this.babylonAudioService.speakCommand('Picking up');
        this.gameCommandService.pickBoxCmd(movableIds, id);
        return;
      }
    }
    this.tsSelectionService.selectOther(id, Diplomacy.BOX, itemTypeId, undefined, babylonItem, itemTypeName);
  }

  private onMoveCommandAck(): void {
    if (this.moveAckTimeout) {
      clearTimeout(this.moveAckTimeout);
      this.moveAckTimeout = null;
    }
    if (!this.hasPendingMoveCommand) {
      return;
    }
    const queued = this.queuedMoveCommand;
    this.queuedMoveCommand = null;
    if (queued != null) {
      this.sendMoveCommand(queued.movableIds, queued.x, queued.y);
    } else {
      this.hasPendingMoveCommand = false;
    }
  }
}
