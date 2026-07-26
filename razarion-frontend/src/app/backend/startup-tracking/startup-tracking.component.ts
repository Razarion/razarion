import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {ButtonModule} from 'primeng/button';
import {CommonModule} from '@angular/common';
import {DialogModule} from 'primeng/dialog';
import {TableModule} from 'primeng/table';
import {ChartModule} from 'primeng/chart';
import {Select} from 'primeng/select';
import {FormsModule} from '@angular/forms';
import {StartupTaskJson, StartupTerminatedJson} from '../../generated/razarion-share';

/** What happened to a startup. Drives the filter, the label and the colour of a row. */
export enum StartupOutcome {
  SUCCESSFUL = 'Successful',
  FAILED = 'Failed',
  ABORTED = 'Aborted'
}

@Component({
  selector: 'startup-tracking',
  imports: [
    ButtonModule,
    TableModule,
    ChartModule,
    DialogModule,
    CommonModule,
    Select,
    FormsModule
  ],
  templateUrl: './startup-tracking.component.html',
  styles: [`
    .startup-failed {
      color: var(--p-red-400, #e34948);
    }
  `]
})
export class StartupTrackingComponent implements OnChanges {
  @Input() startupTerminatedJsons: StartupTerminatedJson[] = [];
  @Input() startupTaskJsons: StartupTaskJson[] = [];

  detailDialogVisible = false;
  data: any;
  options = {
    scales: {
      y: {
        beginAtZero: true
      }
    }
  };
  readonly StartupOutcome = StartupOutcome;
  static readonly FILTER_ALL = 'All';
  readonly filterOptions = [
    {name: StartupTrackingComponent.FILTER_ALL, value: StartupTrackingComponent.FILTER_ALL},
    {name: 'Successful', value: StartupOutcome.SUCCESSFUL},
    {name: 'Failed', value: StartupOutcome.FAILED},
    {name: 'Aborted', value: StartupOutcome.ABORTED}
  ];
  filter: string = StartupTrackingComponent.FILTER_ALL;
  filtered: StartupTerminatedJson[] = [];
  private readonly DATE_FORMAT_OPTION: Intl.DateTimeFormatOptions = {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  };
  detailStartupTaskJsons: StartupTaskJson[] = [];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['startupTerminatedJsons']) {
      this.onFilterChanged();
      this.updateChart();
    }
  }

  /**
   * An aborted startup never reported an end, so it has no duration - it is a row in the table
   * but not a point on the curve.
   */
  outcome(startupTerminatedJson: StartupTerminatedJson): StartupOutcome {
    if (startupTerminatedJson.aborted) {
      return StartupOutcome.ABORTED;
    }
    return startupTerminatedJson.successful ? StartupOutcome.SUCCESSFUL : StartupOutcome.FAILED;
  }

  onFilterChanged() {
    // A copy: the table sorts its value array in place, the chart must keep the server's order.
    this.filtered = this.filter === StartupTrackingComponent.FILTER_ALL
      ? [...this.startupTerminatedJsons]
      : this.startupTerminatedJsons.filter(startupTerminatedJson => this.outcome(startupTerminatedJson) === this.filter);
  }

  count(outcome: StartupOutcome): number {
    return this.startupTerminatedJsons.filter(startupTerminatedJson => this.outcome(startupTerminatedJson) === outcome).length;
  }

  private updateChart() {
    let chartData: number[] = [];
    let labels: string[] = [];
    this.startupTerminatedJsons.forEach(startupTerminatedJson => {
      // Aborted startups carry no total time; plotting them as zero would fake a fast startup.
      // The generated type says number, but the server sends null for those.
      const totalTime = startupTerminatedJson.totalTime as number | null;
      if (totalTime == null) {
        return;
      }
      chartData.push(totalTime);
      labels.push(new Date(startupTerminatedJson.serverTime).toLocaleString('de-DE', this.DATE_FORMAT_OPTION));
    });
    this.data = {
      labels: labels,
      datasets: [{
        label: 'Startup duration',
        data: chartData,
        backgroundColor: [
          'rgba(255, 99, 132, 0.2)',
        ],
        borderColor: [
          'rgb(255, 99, 132)',
        ],
        borderWidth: 1
      }]
    };
  }

  openDetailDialog(startupTerminatedJson: StartupTerminatedJson) {
    this.detailStartupTaskJsons = this.startupTaskJsons.filter(
      task => task.gameSessionUuid === startupTerminatedJson.gameSessionUuid
    );
    this.detailDialogVisible = true;
  }
}
