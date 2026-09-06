import {AfterViewInit, Component, NgZone} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Card} from 'primeng/card';
import {PrimeTemplate} from 'primeng/api';
import {ProgressBarModule} from 'primeng/progressbar';
import {ScreenCover} from '../../gwtangular/GwtAngularFacade';
import {BabylonModelService} from '../renderer/babylon-model.service';
import {announceReadyIfHidden} from './tab-ready-notice';
import {isAnimatedSplash, removeSplash, reportBootProgress} from '../boot-splash';

@Component({
  selector: 'screen-cover',
  imports: [CommonModule, Card, PrimeTemplate, ProgressBarModule],
  templateUrl: './screen-cover.component.html',
  styleUrl: './screen-cover.component.scss'
})
export class ScreenCoverComponent implements ScreenCover, AfterViewInit {
  fadeOutCover: boolean = false;
  removeCover: boolean = false;
  /**
   * Null until the engine reports its first task, which is what the bar being indeterminate
   * means. It used to start at the literal 3, so a client whose WebAssembly module never ran
   * showed a bar frozen at 3% for as long as the player was willing to look at it - the state
   * an X post reported as "Loading stops at 3%". That number was never progress: nothing but
   * AngularStartupListener.onNextTask ever moved it, and that runs inside the engine.
   */
  loadingProgress: number | null = null;

  constructor(private zone: NgZone, public babylonModelService: BabylonModelService) {
  }

  /**
   * Takes over from the boot splash in index.html, which covered the stretch before Angular
   * existed - but only where that splash is the plain one.
   * <p>
   * Where the page is drawing the animated build-up it keeps it, and this cover waits behind it
   * until the terrain is on screen. Handing over here instead would take the picture away at the
   * moment the application appears, which is four seconds before there is anything to look at.
   */
  ngAfterViewInit(): void {
    requestAnimationFrame(() => {
      if (!isAnimatedSplash()) {
        removeSplash();
        return;
      }
      /*
       * The outer bound. removeLoadingCover arms a shorter one, but only if the engine gets far
       * enough to call it - and a loading screen that never leaves is the one way this change
       * could be worse than what it replaced.
       */
      setTimeout(() => removeSplash(), 45000);
    });
  }

  removeLoadingCover(): void {
    // The engine is up. If nobody is looking, say so in the tab strip.
    announceReadyIfHidden();
    /*
     * From here the splash is waiting for the terrain. If that never arrives - a tile that fails
     * to build, a material that never parses - the player would be left on a loading screen for
     * good, which is worse than the abrupt handover this replaced. Twelve seconds is far past
     * the 1.5 s a tile normally needs after the first tick.
     */
    if (isAnimatedSplash()) {
      setTimeout(() => removeSplash(), 12000);
    }
    this.zone.run(() => {
      this.fadeOutCover = true;
      setTimeout(() => {
        // Some very strange babylon behavior, _projectionMatrix is zero matrix
        this.removeCover = true;
      }, 300);
    });
  }

  onStartupProgress(percent: number): void {
    // The page cannot see any of this: it can weigh the JavaScript arriving and nothing after.
    // Handed on so the build-up keeps moving instead of standing finished for four seconds.
    reportBootProgress(percent / 100);
    this.zone.run(() => {
      this.loadingProgress = Math.floor(percent);
    });
  }

  hideStoryCover(): void {
    throw new Error("Not Implemented hideStoryCover()");
  }

  showStoryCover(html: string): void {
    throw new Error("Not Implemented showStoryCover()");
  }

  protected readonly Math = Math;
}
