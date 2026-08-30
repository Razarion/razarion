import {Component, Input, OnChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TableModule} from 'primeng/table';
import {
  DeviceReport,
  FirstInteractionReport,
  InteractionFunnel
} from '../tracking-container/first-interaction-analyzer';

/** One step of the funnel, already measured against the step it is a share of. */
interface FunnelStep {
  name: string;
  count: number;
  /** What this is a share of, named so the column can say it rather than leaving it to be guessed. */
  ofName: string;
  ofCount: number;
}

/**
 * Whether the players found the controls, per device.
 *
 * Reads the same loaded range as the rest of the backend view. Every share is against the sessions
 * that got a running game, so a control nobody used shows as a low percentage rather than as a
 * missing row - absence is the whole signal here.
 */
@Component({
  selector: 'first-interaction',
  imports: [CommonModule, TableModule],
  templateUrl: './first-interaction.component.html',
  styles: [`
    .interaction-note {
      color: var(--p-text-muted-color, #898781);
    }
  `]
})
export class FirstInteractionComponent implements OnChanges {
  @Input() firstInteractionReport?: FirstInteractionReport;

  devices: DeviceReport[] = [];
  /** Funnel steps per device, keyed by device so the template can pair them with the row above. */
  funnelSteps = new Map<string, FunnelStep[]>();

  ngOnChanges(): void {
    this.devices = this.firstInteractionReport?.devices ?? [];
    this.funnelSteps = new Map(this.devices.map(device =>
      [device.device, FirstInteractionComponent.steps(device.funnel)]));
  }

  /**
   * Each step against the one before it. Chained rather than all against the start, because the
   * question this view was built for is which step loses the players, and a share of the start
   * cannot separate "few got here" from "few got past here". The analyzer nests the steps, so
   * every row is a share of a population it belongs to and none of them can exceed 100%.
   */
  private static steps(funnel: InteractionFunnel): FunnelStep[] {
    return [
      {name: 'Game running', count: funnel.started, ofName: 'sessions', ofCount: funnel.started},
      // Two steps where there used to be one. Reaching for the game and getting something out of
      // it are different events, and the gap between them is the only place a control that does
      // not answer can show up at all.
      {name: 'Touched the field', count: funnel.touched, ofName: 'game running', ofCount: funnel.started},
      {name: 'Got a reaction', count: funnel.interacted, ofName: 'touched the field', ofCount: funnel.touched},
      {name: 'Selected something', count: funnel.selected, ofName: 'got a reaction', ofCount: funnel.interacted},
      {name: 'Gave an order', count: funnel.commanded, ofName: 'selected something', ofCount: funnel.selected},
      {name: 'Passed a quest', count: funnel.questPassed, ofName: 'gave an order', ofCount: funnel.commanded}
    ];
  }

  steps(device: DeviceReport): FunnelStep[] {
    return this.funnelSteps.get(device.device) ?? [];
  }

  /** Percent of nobody is not zero percent, it is nothing. */
  percent(count: number, of: number): string {
    return of > 0 ? Math.round(count / of * 100) + '%' : '-';
  }

  seconds(medianSeconds: number | null): string {
    return medianSeconds == null ? '-' : Math.round(medianSeconds) + ' s';
  }
}
