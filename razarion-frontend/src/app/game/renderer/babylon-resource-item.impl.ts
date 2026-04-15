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
import {BabylonResourceSparkle} from "./babylon-resource-sparkle";
import {BabylonResourceDecal} from "./babylon-resource-decal";

export class BabylonResourceItemImpl extends BabylonItemImpl implements BabylonResourceItem {
  private sparkle: BabylonResourceSparkle | null = null;
  private decal: BabylonResourceDecal | null = null;

  constructor(id: number,
              resourceItemType: ResourceItemType,
              rendererService: BabylonRenderServiceAccessImpl,
              actionService: ActionService,
              tsSelectionService: TsSelectionService,
              babylonModelService: BabylonModelService,
              uiConfigCollectionService: UiConfigCollectionService,
              disposeCallback: ((permanent: boolean) => void) | null) {
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
    this.sparkle = new BabylonResourceSparkle(rendererService.getScene(), resourceItemType.getRadius());
    this.decal = new BabylonResourceDecal(rendererService.getScene(), resourceItemType.getRadius(), rendererService);
  }

  override setPosition(position: Vertex): void {
    super.setPosition(position);
    if (position) {
      if (this.sparkle) {
        this.sparkle.emitter.x = position.getX();
        this.sparkle.emitter.y = position.getZ();
        this.sparkle.emitter.z = position.getY();
      }
      if (this.decal) {
        this.decal.updatePosition(position.getX(), position.getZ(), position.getY());
      }
    }
  }

  override dispose(): void {
    this.sparkle?.dispose();
    this.sparkle = null;
    this.decal?.dispose();
    this.decal = null;
    super.dispose();
  }

  override removeFromView(): void {
    this.sparkle?.dispose();
    this.sparkle = null;
    this.decal?.dispose();
    this.decal = null;
    super.removeFromView();
  }

  public static createDummy(id: number): BabylonResourceItem {
    return new class implements BabylonResourceItem {
      dispose(): void {
      }

      removeFromView(): void {
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
