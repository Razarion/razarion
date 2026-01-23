import {Injectable} from '@angular/core';
import {InfoDialogMode} from '../info-dialog/info-dialog.component';

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

  showInfoDialog = false;
  infoDialogMode: InfoDialogMode = 'info';

  openInfoDialog(mode: InfoDialogMode = 'info'): void {
    this.infoDialogMode = mode;
    this.showInfoDialog = true;
  }
}
