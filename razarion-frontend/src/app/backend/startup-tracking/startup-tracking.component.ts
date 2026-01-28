import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {ButtonModule} from 'primeng/button';
import {CommonModule} from '@angular/common';
import {DialogModule} from 'primeng/dialog';
import {TableModule} from 'primeng/table';
import {ChartModule} from 'primeng/chart';
import {StartupTaskJson, StartupTerminatedJson} from '../../generated/razarion-share';

@Component({
  selector: 'startup-tracking',
  imports: [
    ButtonModule,
    TableModule,
    ChartModule,
    DialogModule,
    CommonModule
  ],
  templateUrl: './startup-tracking.component.html'
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
      this.updateChart();
    }
  }

  private updateChart() {
    let chartData: number[] = [];
    let labels: string[] = [];
    this.startupTerminatedJsons.forEach(startupTerminatedJson => {
      chartData.push(startupTerminatedJson.totalTime);
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
