import {Component, inject} from '@angular/core';
import {RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {ThumbnailStorageService} from '../tasks/thumbnail-storage.service';

const LAST_EMAIL_KEY = 'razarion.studio.lastLoginEmail';

/**
 * Studio shell. Owns nothing 3D — no canvas, no game bootstrap. Just:
 *   - Login state (admin JWT lives in localStorage["app.token"])
 *   - Header with title + auth status
 *   - <router-outlet /> for the active task
 *
 * The Babylon engine is created on-demand by the positioner modal inside
 * the thumbnails task; while no modal is open, no renderer exists.
 */
@Component({
  selector: 'studio-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, FormsModule],
  template: `
    <header>
      <h1>Razarion Studio</h1>
      <nav class="tabs">
        <a routerLink="/thumbnails" routerLinkActive="active">Thumbnails</a>
        <a routerLink="/scenes" routerLinkActive="active">Scenes</a>
      </nav>
      <div class="auth">
        @if (storage.needsLogin()) {
          <form (submit)="submitLogin($event)" class="login-form">
            <input type="email" name="email" placeholder="email" autocomplete="username"
                   [(ngModel)]="loginEmail" [disabled]="busy" required>
            <input type="password" name="password" placeholder="password" autocomplete="current-password"
                   [(ngModel)]="loginPassword" [disabled]="busy" required>
            <button type="submit" [disabled]="busy">{{ busy ? '…' : 'Login' }}</button>
          </form>
        } @else {
          <span class="logged-in">{{ storage.loggedInAs() }}</span>
          <button class="ghost" (click)="logout()">Logout</button>
        }
        @if (storage.lastError(); as err) {
          <span class="error" title="Storage error">{{ err }}</span>
        }
      </div>
    </header>
    <main>
      <router-outlet />
    </main>
  `,
  styles: [`
    :host {
      display: flex;
      flex-direction: column;
      width: 100%;
      height: 100%;
      background: #15171c;
      color: #ddd;
    }
    header {
      flex-shrink: 0;
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 10px 18px;
      background: #1a1d22;
      border-bottom: 1px solid #2a2e35;
    }
    h1 {
      margin: 0;
      font-size: 14px;
      letter-spacing: 0.08em;
      text-transform: uppercase;
      color: #aaa;
    }
    .tabs {
      display: flex;
      gap: 2px;
      margin-left: 8px;
    }
    .tabs a {
      padding: 6px 14px;
      font-size: 12px;
      color: #aaa;
      text-decoration: none;
      border-radius: 4px;
      border: 1px solid transparent;
    }
    .tabs a:hover { background: rgba(74, 158, 255, 0.10); color: #ddd; }
    .tabs a.active {
      background: rgba(74, 158, 255, 0.22);
      color: #fff;
      border-color: rgba(74, 158, 255, 0.5);
    }
    .auth {
      margin-left: auto;
      display: flex;
      align-items: center;
      gap: 10px;
    }
    .auth .logged-in {
      font-size: 12px;
      color: #b8e9c4;
    }
    .auth .error {
      font-size: 11px;
      color: #ff8a8a;
      max-width: 320px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .login-form {
      display: flex;
      gap: 6px;
      align-items: center;
    }
    .login-form input {
      padding: 4px 8px;
      font-size: 11px;
      background: rgba(0, 0, 0, 0.3);
      border: 1px solid rgba(255, 255, 255, 0.18);
      border-radius: 3px;
      color: #eee;
      width: 160px;
      font-family: inherit;
    }
    .login-form input:focus {
      outline: none;
      border-color: #4a9eff;
    }
    button {
      padding: 5px 12px;
      border-radius: 4px;
      border: 1px solid #4a9eff;
      background: rgba(74, 158, 255, 0.18);
      color: #eee;
      cursor: pointer;
      font-size: 11px;
    }
    button:hover:not(:disabled) { background: rgba(74, 158, 255, 0.32); }
    button:disabled { opacity: 0.5; cursor: default; }
    button.ghost {
      background: transparent;
      border-color: rgba(255, 255, 255, 0.2);
      color: #aaa;
    }
    main {
      flex: 1;
      position: relative;
      overflow: hidden;
    }
  `]
})
export class ShellComponent {
  protected readonly storage = inject(ThumbnailStorageService);
  loginEmail = localStorage.getItem(LAST_EMAIL_KEY) ?? '';
  loginPassword = '';
  busy = false;

  async submitLogin(event: Event): Promise<void> {
    event.preventDefault();
    if (!this.loginEmail || !this.loginPassword) return;
    this.busy = true;
    try {
      await this.storage.login(this.loginEmail, this.loginPassword);
      localStorage.setItem(LAST_EMAIL_KEY, this.loginEmail);
      this.loginPassword = '';
    } catch (e) {
      console.warn('[Studio] login failed', e);
    } finally {
      this.busy = false;
    }
  }

  logout(): void {
    this.storage.logout();
    this.loginPassword = '';
  }
}
