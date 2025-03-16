import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Card} from 'primeng/card';
import {PrimeTemplate} from 'primeng/api';
import {ProgressBarModule} from 'primeng/progressbar';

@Component({
  selector: 'loading',
  imports: [CommonModule, Card, PrimeTemplate, ProgressBarModule],
  templateUrl: './loading.component.html',
  styleUrl: './loading.component.scss'
})
export class LoadingComponent {
  fadeOutCover: boolean = false;
  removeCover: boolean = false;

}
