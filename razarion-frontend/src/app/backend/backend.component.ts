import { Component, ViewChild } from '@angular/core';
import { BackendControllerClient, StartupTerminatedJson, HttpClient as HttpClientAdapter, RestResponse, StartupTaskJson } from '../generated/razarion-share';
import { HttpClient } from '@angular/common/http';
import { OverlayPanel } from 'primeng/overlaypanel';
import { TypescriptGenerator } from './typescript-generator';

@Component({
    selector: 'app-backend',
    templateUrl: './backend.component.html',
    styleUrls: ['./backend.component.scss']
})
export class BackendComponent {
  startupTerminatedJsons: StartupTerminatedJson[] = [];
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
  @ViewChild('detailOverlayPanel')
  detailOverlayPanel!: OverlayPanel;
  private backendControllerClient!: BackendControllerClient;
  detailStartupTaskJsons: StartupTaskJson[] = [];

  constructor(httpClient: HttpClient) {
    try {
      this.backendControllerClient = new BackendControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
      this.backendControllerClient.loadStartupTerminatedJson().then(startupTerminatedJsons => {
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

  showDetailOverlayPanel(event: Event, startupTerminatedJson: StartupTerminatedJson) {
    this.backendControllerClient.loadStartupTaskJson(startupTerminatedJson.gameSessionUuid).then(startupTaskJsons => {
      this.detailStartupTaskJsons = startupTaskJsons;
      this.detailOverlayPanel.toggle(event);
    });
  }

}
