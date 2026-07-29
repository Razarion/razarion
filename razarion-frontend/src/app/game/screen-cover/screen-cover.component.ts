import {AfterViewInit, Component, NgZone} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Card} from 'primeng/card';
import {PrimeTemplate} from 'primeng/api';
import {ProgressBarModule} from 'primeng/progressbar';
import {ScreenCover} from '../../gwtangular/GwtAngularFacade';
import {BabylonModelService} from '../renderer/babylon-model.service';

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
   * existed. Dropped after a frame so the two are never both gone at once.
   */
  ngAfterViewInit(): void {
    requestAnimationFrame(() => {
      document.getElementById('raz-boot')?.remove();
    });
  }

  removeLoadingCover(): void {
    this.zone.run(() => {
      this.fadeOutCover = true;
      setTimeout(() => {
        // Some very strange babylon behavior, _projectionMatrix is zero matrix
        this.removeCover = true;
      }, 300);
    });
  }

  onStartupProgress(percent: number): void {
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
