import {Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class MainCockpitService {
  showLoginDialog = false;
  showUserDialog = false;
  showRegisterDialog = false;
  showRegisteredDialog = false;
}
