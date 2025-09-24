import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {UiConfigCollection, UiConfigCollectionControllerClient} from '../generated/razarion-share';
import {TypescriptGenerator} from '../backend/typescript-generator';
import {UserService} from '../auth/user.service';

@Injectable({
  providedIn: 'root'
})
export class UiConfigCollectionService {
  private uiConfigCollection: UiConfigCollection | null = null;
  private pendingResolves: ((UiConfigCollection: UiConfigCollection) => void)[] = [];

  constructor(httpClient: HttpClient, private userService: UserService) {
    const client = new UiConfigCollectionControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
    client.getUiConfigCollection()
      .then(uiConfigCollection => {
        this.userService.registerState = uiConfigCollection.registerState;
        this.userService.name = uiConfigCollection.name;
        this.uiConfigCollection = uiConfigCollection;
        this.pendingResolves.forEach((resolve: (UiConfigCollection: UiConfigCollection) => void) => resolve(uiConfigCollection));
        this.pendingResolves = [];
      })
      .catch(error => console.error(`Can not load UiConfigCollection ${error}`));
  }

  getUiConfigCollection(): Promise<UiConfigCollection> {
    return new Promise((resolve) => {
      if (this.uiConfigCollection) {
        resolve(this.uiConfigCollection);
      } else {
        this.pendingResolves.push(resolve);
      }
    });
  }

  getSelectionItemMaterialId(): number | null {
    return this.uiConfigCollection!.selectionItemMaterialId;
  }
}
