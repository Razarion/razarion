import {Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class CockpitDisplayService {
  showMainCockpit = false;
  showItemCockpit = false;
  showQuestCockpit = false;
  showChatCockpit = false;

  showLoginDialog = false;
  showUserDialog = false;
  showRegisterDialog = false;
  showRegisteredDialog = false;
  showSetUserNameDialog = false;
  showQuestDialog = false;
}
