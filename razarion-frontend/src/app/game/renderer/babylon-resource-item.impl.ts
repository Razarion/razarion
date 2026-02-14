import {BabylonItemImpl} from "./babylon-item.impl";
import {
  BabylonResourceItem,
  Diplomacy,
  MarkerConfig,
  ResourceItemType,
  Vertex
} from "../../gwtangular/GwtAngularFacade";
import {BabylonRenderServiceAccessImpl} from "./babylon-render-service-access-impl.service";
import {BabylonModelService} from "./babylon-model.service";
import {ActionService} from "../action.service";
import {UiConfigCollectionService} from "../ui-config-collection.service";
import {SelectionService as TsSelectionService} from "../selection.service";

export class BabylonResourceItemImpl extends BabylonItemImpl implements BabylonResourceItem {
  constructor(id: number,
              resourceItemType: ResourceItemType,
              rendererService: BabylonRenderServiceAccessImpl,
              actionService: ActionService,
              tsSelectionService: TsSelectionService,
              babylonModelService: BabylonModelService,
              uiConfigCollectionService: UiConfigCollectionService,
              disposeCallback: (() => void) | null) {
    super(id,
      resourceItemType,
      Diplomacy.RESOURCE,
      rendererService,
      babylonModelService,
      uiConfigCollectionService,
      actionService,
      tsSelectionService,
      rendererService.resourceItemContainer,
      disposeCallback);
    this.updateItemCursor();
  }

  public static createDummy(id: number): BabylonResourceItem {
    return new class implements BabylonResourceItem {
      dispose(): void {
      }

      getAngle(): number {
        return 0;
      }

      getId(): number {
        return id;
      }

      getPosition(): Vertex | null {
        return null;
      }

      hover(active: boolean): void {
      }

      mark(markerConfig: MarkerConfig | null): void {
      }

      select(active: boolean): void {
      }

      setAngle(angle: number): void {
      }

      setPosition(position: Vertex): void {
      }

      isEnemy(): boolean {
        return false;
      }
    };
  }
}
