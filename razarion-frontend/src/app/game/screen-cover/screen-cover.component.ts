import {Component, NgZone} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Card} from 'primeng/card';
import {PrimeTemplate} from 'primeng/api';
import {ProgressBarModule} from 'primeng/progressbar';
import {ScreenCover} from '../../gwtangular/GwtAngularFacade';

@Component({
  selector: 'screen-cover',
  imports: [CommonModule, Card, PrimeTemplate, ProgressBarModule],
  templateUrl: './screen-cover.component.html',
  styleUrl: './screen-cover.component.scss'
})
export class ScreenCoverComponent implements ScreenCover {
  fadeOutCover: boolean = false;
  removeCover: boolean = false;
  loadingProgress = 0;

  constructor(private zone: NgZone) {
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
      this.loadingProgress = percent;
    });
  }

  hideStoryCover(): void {
    throw new Error("Not Implemented hideStoryCover()");
  }

  showStoryCover(html: string): void {
    throw new Error("Not Implemented showStoryCover()");
  }
}
