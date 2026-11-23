import {Component} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {Button} from 'primeng/button';
import {ProgressLinkResult, ProgressSaveService} from './progress-save.service';
import {CockpitDisplayService} from '../cockpit/cockpit-display.service';

/**
 * The "Save progress" dialog: one address field for both saving here and continuing a saved game on
 * this device - the server decides which from the address, the player does not have to.
 */
@Component({
  selector: 'save-progress',
  imports: [FormsModule, Button],
  templateUrl: './save-progress.component.html',
  styles: [':host ::ng-deep .facebook-button { background: #1877f2; border-color: #1877f2; color: #fff; }']
})
export class SaveProgressComponent {
  email = '';
  sending = false;
  sentTo: string | null = null;
  error: string | null = null;

  constructor(public progressSaveService: ProgressSaveService,
              private cockpitDisplayService: CockpitDisplayService) {
  }

  async send(): Promise<void> {
    const email = this.email.trim();
    if (!email || this.sending) {
      return;
    }
    this.sending = true;
    this.error = null;
    const result: ProgressLinkResult = await this.progressSaveService.requestLink(email);
    this.sending = false;
    switch (result) {
      case 'SENT':
        this.sentTo = email;
        break;
      case 'INVALID_EMAIL':
        this.error = 'That does not look like an email address.';
        break;
      case 'TOO_MANY':
        this.error = 'A link is on its way already. Please check your inbox, or try again in a few minutes.';
        break;
      case 'REGISTERED':
        this.error = 'You are signed in with a password, your game is saved already.';
        break;
      case 'NOT_IN_GAME':
        // The connection to the game dropped; a reload brings it back.
        this.error = 'The connection to the game was lost. Please reload the page and try again.';
        break;
      case 'BUSY':
        this.error = 'Too many links are being sent right now. Please try again in an hour.';
        break;
      default:
        this.error = 'Sending the link did not work. Please try again later.';
    }
  }

  passwordLogin(): void {
    this.cockpitDisplayService.showSaveProgressDialog = false;
    this.cockpitDisplayService.showLoginDialog = true;
  }
}
