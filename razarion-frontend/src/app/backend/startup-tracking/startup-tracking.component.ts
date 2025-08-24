import {Component} from '@angular/core';
import {ButtonModule} from 'primeng/button';
import {CommonModule} from '@angular/common';
import {DialogModule} from 'primeng/dialog';
import {TableModule} from 'primeng/table';
import {ChartModule} from 'primeng/chart';
import {StartupTaskJson, StartupTerminatedJson, TrackerControllerImplClient} from '../../generated/razarion-share';
import {HttpClient} from '@angular/common/http';
import {TypescriptGenerator} from '../typescript-generator';

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
export class StartupTrackingComponent {
  startupTerminatedJsons: StartupTerminatedJson[] = [];
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
  private trackerControllerImplClient!: TrackerControllerImplClient;
  detailStartupTaskJsons: StartupTaskJson[] = [];

  constructor(httpClient: HttpClient) {
    try {
      this.trackerControllerImplClient = new TrackerControllerImplClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
      this.trackerControllerImplClient.loadStartupTerminatedJson().then(startupTerminatedJsons => {
        this.startupTerminatedJsons = startupTerminatedJsons;
        let chartData: number[] = [];
        let labels: string[] = [];
        startupTerminatedJsons.forEach(startupTerminatedJson => {
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

      });
    } catch (e) {
      console.log(e);
    }
  }

  openDetailDialog(startupTerminatedJson: StartupTerminatedJson) {
    this.trackerControllerImplClient.loadStartupTaskJson(startupTerminatedJson.gameSessionUuid).then(startupTaskJsons => {
      this.detailStartupTaskJsons = startupTaskJsons;
    });
    this.detailDialogVisible = true;
  }


}
