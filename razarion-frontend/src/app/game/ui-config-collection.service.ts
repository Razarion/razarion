import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {UiConfigCollection, UiConfigCollectionControllerClient} from '../generated/razarion-share';
import {TypescriptGenerator} from '../backend/typescript-generator';

@Injectable({
  providedIn: 'root'
})
export class UiConfigCollectionService {
  private uiConfigCollection: UiConfigCollection | null = null;
  private pendingResolves: ((UiConfigCollection: UiConfigCollection) => void)[] = [];

  constructor(httpClient: HttpClient) {
    const client = new UiConfigCollectionControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
    client.getUiConfigCollection()
      .then(uiConfigCollection => {
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

  getProgressBarNodeMaterialId(): number | null {
    return this.uiConfigCollection!.progressBarNodeMaterialId;
  }

  getHealthBarNodeMaterialId(): number | null {
    return this.uiConfigCollection!.healthBarNodeMaterialId;
  }
}
