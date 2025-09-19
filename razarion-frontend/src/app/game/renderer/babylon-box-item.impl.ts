import {
  BabylonBoxItem,
  BoxItemType,
  Diplomacy,
  MarkerConfig,
  SelectionService,
  Vertex
} from "src/app/gwtangular/GwtAngularFacade";
import {BabylonItemImpl} from "./babylon-item.impl";
import {BabylonRenderServiceAccessImpl} from "./babylon-render-service-access-impl.service";
import {BabylonModelService} from "./babylon-model.service";
import {ActionService} from "../action.service";
import {UiConfigCollectionService} from "../ui-config-collection.service";

export class BabylonBoxItemImpl extends BabylonItemImpl implements BabylonBoxItem {
  constructor(id: number,
              private boxItemType: BoxItemType,
              rendererService: BabylonRenderServiceAccessImpl,
              actionService: ActionService,
              selectionService: SelectionService,
              babylonModelService: BabylonModelService,
              uiConfigCollectionService: UiConfigCollectionService,
              disposeCallback: (() => void) | null) {
    super(id,
      boxItemType,
      Diplomacy.BOX,
      rendererService,
      babylonModelService,
      uiConfigCollectionService,
      actionService,
      selectionService,
      rendererService.boxItemContainer,
      disposeCallback);
  }

  static createDummy(id: number): BabylonBoxItem {
    return new class implements BabylonBoxItem {
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
