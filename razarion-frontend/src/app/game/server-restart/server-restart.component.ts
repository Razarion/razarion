import {Component, NgZone, OnDestroy} from '@angular/core';
import {ButtonModule} from 'primeng/button';
import {ServerRestartPresenter} from '../../gwtangular/GwtAngularFacade';
import {CockpitDisplayService} from '../cockpit/cockpit-display.service';
import {UserService} from '../../auth/user.service';

/**
 * Shows what the old GWT ServerRestartDialog did, but driven by the automated deployment instead
 * of a hand-typed admin message:
 * <ul>
 *   <li>while the countdown runs: a non-blocking banner — the game keeps going</li>
 *   <li>once the connection drops: a blocking overlay that polls the server and reloads by itself</li>
 * </ul>
 * Unregistered players are told they lose their base, because the server deletes unregistered
 * users on startup (UserService.cleanupUnregisteredUsersStartup).
 */
@Component({
  selector: 'server-restart',
  imports: [ButtonModule],
  templateUrl: './server-restart.component.html',
  styleUrl: './server-restart.component.scss'
})
export class ServerRestartComponent implements ServerRestartPresenter, OnDestroy {
  private static readonly HEALTH_URL = '/actuator/health/readiness';
  private static readonly HEALTH_POLL_MS = 5000;

  /** Seconds until the restart, or undefined while no restart is announced. */
  secondsLeft?: number;
  /** True once the connection is gone — blocks the screen. */
  serverDown = false;
  /** True if the outage was announced, so we can promise the game continues. */
  serverRestarting = false;

  private countdownHandle?: any;
  private healthPollHandle?: any;

  constructor(private zone: NgZone,
              private cockpitDisplayService: CockpitDisplayService,
              private userService: UserService) {
  }

  onServerRestartAnnounced(seconds: number): void {
    this.zone.run(() => {
      this.serverRestarting = true;
      this.secondsLeft = Math.max(0, Math.floor(seconds));
      this.startCountdown();
    });
  }

  onServerRestartCancelled(): void {
    this.zone.run(() => {
      this.serverRestarting = false;
      this.secondsLeft = undefined;
      this.stopCountdown();
    });
  }

  onServerUnavailable(serverRestarting: boolean): void {
    this.zone.run(() => {
      this.stopCountdown();
      this.secondsLeft = undefined;
      this.serverRestarting = serverRestarting;
      if (this.serverDown) {
        return;
      }
      this.serverDown = true;
      this.startHealthPolling();
    });
  }

  ngOnDestroy(): void {
    this.stopCountdown();
    this.stopHealthPolling();
  }

  /** Whether the base-loss warning and the register button apply to this player. */
  get losesBase(): boolean {
    return this.userService.showRegisterButton();
  }

  get countdownText(): string {
    const total = this.secondsLeft ?? 0;
    const minutes = Math.floor(total / 60);
    const seconds = total % 60;
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  }

  onRegisterClicked(): void {
    this.cockpitDisplayService.showRegisterDialog = true;
  }

  private startCountdown(): void {
    this.stopCountdown();
    this.countdownHandle = setInterval(() => {
      this.zone.run(() => {
        if (this.secondsLeft === undefined || this.secondsLeft <= 0) {
          // The countdown expired. Keep the banner at 0:00 — the server may take a few more
          // seconds, and if the deployment was aborted nothing bad happens either.
          this.stopCountdown();
          return;
        }
        this.secondsLeft--;
      });
    }, 1000);
  }

  private stopCountdown(): void {
    if (this.countdownHandle !== undefined) {
      clearInterval(this.countdownHandle);
      this.countdownHandle = undefined;
    }
  }

  /**
   * Reload as soon as the new server instance is ready. A reload rather than a reconnect: the
   * server restarted from a backup, so the client has to boot cold against the fresh state.
   */
  private startHealthPolling(): void {
    this.stopHealthPolling();
    this.healthPollHandle = setInterval(() => {
      fetch(ServerRestartComponent.HEALTH_URL, {cache: 'no-store'})
        .then(response => {
          if (response.ok) {
            this.stopHealthPolling();
            window.location.reload();
          }
        })
        .catch(() => {
          // Server still down — keep polling.
        });
    }, ServerRestartComponent.HEALTH_POLL_MS);
  }

  private stopHealthPolling(): void {
    if (this.healthPollHandle !== undefined) {
      clearInterval(this.healthPollHandle);
      this.healthPollHandle = undefined;
    }
  }
}
