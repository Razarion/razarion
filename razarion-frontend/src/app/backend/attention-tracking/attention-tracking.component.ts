import {Component, Input, OnChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TableModule} from 'primeng/table';
import {AttentionCohort, AttentionReport} from '../tracking-container/attention-analyzer';
import {ProgressStatistic} from '../tracking-container/progress-statistic';

/**
 * Shows whether the players were watching. Two questions the startup view cannot answer: what a
 * player who left mid load actually did, and whether a startup that succeeded had anybody in
 * front of it.
 */
@Component({
  selector: 'attention-tracking',
  imports: [CommonModule, TableModule],
  templateUrl: './attention-tracking.component.html',
  styles: [`
    .attention-note {
      color: var(--p-text-muted-color, #898781);
    }
  `]
})
export class AttentionTrackingComponent implements OnChanges {
  @Input() attentionReport?: AttentionReport;

  abandonedRows: ProgressStatistic[] = [];
  timingRows: ProgressStatistic[] = [];
  cohorts: AttentionCohort[] = [];

  ngOnChanges(): void {
    const attentionReport = this.attentionReport;
    if (!attentionReport) {
      this.abandonedRows = [];
      this.timingRows = [];
      this.cohorts = [];
      return;
    }

    const abandoned = attentionReport.abandoned;
    const abandonedTotal = abandoned.tabbedAway + abandoned.leftPage + abandoned.unknown;
    this.abandonedRows = [
      new ProgressStatistic('Tabbed away', abandoned.tabbedAway, abandonedTotal),
      new ProgressStatistic('Left the page', abandoned.leftPage, abandonedTotal),
      new ProgressStatistic('Unknown (reconstructed)', abandoned.unknown, abandonedTotal)
    ];

    // Every slice is measured against the backgrounded startups, not against each other: the
    // buckets are one distribution, and chaining them would read as a funnel that does not exist.
    const timing = attentionReport.timing;
    this.timingRows = timing.buckets.map(bucket =>
      new ProgressStatistic(bucket.label, bucket.count, timing.backgrounded));

    this.cohorts = attentionReport.cohorts;
  }

  /** Share of a cohort that got that far. Percent of nobody is not zero percent, it is nothing. */
  percent(count: number, of: number): string {
    return of > 0 ? Math.round(count / of * 100) + '%' : '-';
  }
}
