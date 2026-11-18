import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {firstValueFrom} from 'rxjs';
import {MessageService} from 'primeng/api';
import {CockpitDisplayService} from '../cockpit/cockpit-display.service';
import {UserService} from '../../auth/user.service';

export type ProgressLinkResult = 'SENT' | 'INVALID_EMAIL' | 'TOO_MANY' | 'REGISTERED' | 'FAILED';

/** The level a player has after leaving the noob island (quest 392): the first one worth keeping. */
export const PROGRESS_OFFER_LEVEL = 9;
const OFFER_DELAY_MILLIS = 45000;
const OFFERED_STORAGE_KEY = 'raz.progressSaveOffered';

/**
 * "Save your progress" for players without an account: an address, a link by mail, no password. The
 * server keeps only a fingerprint of the address - see ProgressSaveService on the server. Opening the
 * link lands on /game?progress=saved|welcome|expired, which {@link #announceLinkOutcome} turns into a
 * toast.
 */
@Injectable({
  providedIn: 'root'
})
export class ProgressSaveService {
  saved = false;
  /** Whether "Continue with Facebook" is configured on the server. */
  facebook = false;
  private offerTimer: ReturnType<typeof setTimeout> | null = null;
  private stateLoaded = false;

  constructor(private httpClient: HttpClient,
              private cockpitDisplayService: CockpitDisplayService,
              private userService: UserService,
              private messageService: MessageService) {
  }

  /** Nothing to offer to a password account, it is safe already. */
  get applies(): boolean {
    return !this.userService.isLoggedIn();
  }

  async loadState(): Promise<void> {
    if (!this.applies) {
      return;
    }
    try {
      const state = await firstValueFrom(this.httpClient.get<{ saved: boolean, facebook?: boolean }>('/rest/progress/state'));
      this.saved = !!state?.saved;
      this.facebook = !!state?.facebook;
      this.stateLoaded = true;
    } catch (e) {
      console.warn('progress state', e);
    }
  }

  async requestLink(email: string): Promise<ProgressLinkResult> {
    try {
      const answer = await firstValueFrom(this.httpClient.post<{ result: ProgressLinkResult }>('/rest/progress/request-link', {email}));
      return answer?.result ?? 'FAILED';
    } catch (e) {
      console.warn('progress link', e);
      return 'FAILED';
    }
  }

  /** Off to Facebook and back - the game page is left for that moment, the progress is on the server. */
  continueWithFacebook(): void {
    window.location.href = '/rest/progress/facebook/start';
  }

  open(where: string): void {
    this.cockpitDisplayService.showSaveProgressDialog = true;
    this.httpClient.post('/rest/progress/offered', null, {params: {where}}).subscribe({error: () => undefined});
  }

  /**
   * Once per browser, and only once there is something to lose: a while after the level that comes
   * with leaving the island, so it does not land on top of the level-up splash and the island sale.
   */
  onLevel(levelNumber: number): void {
    if (levelNumber < PROGRESS_OFFER_LEVEL || this.offerTimer || !this.applies || this.wasOffered()) {
      return;
    }
    this.offerTimer = setTimeout(() => {
      if (this.stateLoaded && !this.saved && !this.wasOffered() && !this.cockpitDisplayService.showSaveProgressDialog) {
        this.markOffered();
        this.open('LEVEL_' + levelNumber);
      }
    }, OFFER_DELAY_MILLIS);
  }

  /** Reads ?progress= left by the mail link, says how it went, and takes it off the address bar. */
  announceLinkOutcome(): void {
    let outcome: string | null = null;
    try {
      const url = new URL(window.location.href);
      outcome = url.searchParams.get('progress');
      if (!outcome) {
        return;
      }
      url.searchParams.delete('progress');
      window.history.replaceState(window.history.state, '', url.pathname + url.search + url.hash);
    } catch (e) {
      return;
    }
    if (outcome === 'saved') {
      this.saved = true;
      this.markOffered();
      this.messageService.add({severity: 'success', summary: 'Progress saved', detail: 'Continue on any device: use the same email or Facebook login under "Save progress" there.', life: 10000});
    } else if (outcome === 'welcome') {
      this.saved = true;
      this.markOffered();
      this.messageService.add({severity: 'success', summary: 'Welcome back', detail: 'You continue your saved game.', life: 8000});
    } else if (outcome === 'cancelled') {
      this.messageService.add({severity: 'info', summary: 'Not saved', detail: 'Facebook login was cancelled. Your game goes on as before.', life: 8000});
    } else if (outcome === 'failed') {
      this.messageService.add({severity: 'warn', summary: 'Not saved', detail: 'Facebook login did not work. Please try again, or use your email.', life: 10000});
    } else if (outcome === 'expired') {
      this.messageService.add({severity: 'warn', summary: 'Link expired', detail: 'That link was used already or is too old. Ask for a new one under "Save progress".', life: 10000});
    }
  }

  private wasOffered(): boolean {
    try {
      return localStorage.getItem(OFFERED_STORAGE_KEY) === '1';
    } catch (e) {
      return false;
    }
  }

  private markOffered(): void {
    try {
      localStorage.setItem(OFFERED_STORAGE_KEY, '1');
    } catch (e) {
      // Private mode or blocked storage: the offer may come once more, nothing worse.
    }
  }
}
