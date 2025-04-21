import {Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class CockpitDisplayService {
  showMainCockpit = false;
  showItemCockpit = false;
  showQuestCockpit = false;
}
