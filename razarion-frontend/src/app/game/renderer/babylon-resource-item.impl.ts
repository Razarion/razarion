import {BabylonItemImpl} from "./babylon-item.impl";
import {BabylonResourceItem, Diplomacy, ResourceItemType, Vertex} from "../../gwtangular/GwtAngularFacade";
import {BabylonRenderServiceAccessImpl} from "./babylon-render-service-access-impl.service";
import {BabylonModelService} from "./babylon-model.service";

export class BabylonResourceItemImpl extends BabylonItemImpl implements BabylonResourceItem {
  constructor(id: number, private resourceItemType: ResourceItemType, rendererService: BabylonRenderServiceAccessImpl, babylonModelService: BabylonModelService) {
    super(id, resourceItemType, Diplomacy.RESOURCE, rendererService, babylonModelService, rendererService.resourceItemContainer);
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

      select(active: boolean): void {
      }

      setAngle(angle: number): void {
      }

      setPosition(position: Vertex): void {
      }

      updateAngle(): void {
      }

      updatePosition(): void {
      }

    };
  }
}
