import {Component, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {PageRequest, StartupTaskJson, StartupTerminatedJson, TrackerControllerImplClient} from '../../generated/razarion-share';
import {TypescriptGenerator} from '../typescript-generator';
import {DataViewModule} from 'primeng/dataview';
import {CommonModule} from '@angular/common';
import {DatePickerModule} from 'primeng/datepicker';
import {FormsModule} from '@angular/forms';
import {Select} from 'primeng/select';
import {ButtonModule} from 'primeng/button';
import {SelectItem} from 'primeng/api';
import {TabsModule} from 'primeng/tabs';
import {createStatistics, ProgressStatistic} from './progress-statistic';
import {TableModule} from 'primeng/table';
import {TrackingContainerAnalyzer} from './tracking-container-analyzer';
import {UserMgmtComponent} from '../../editor/user-mgmt/user-mgmt.component';
import {StartupTrackingComponent} from '../startup-tracking/startup-tracking.component';

@Component({
  selector: 'tracking-container',
  imports: [DataViewModule,
    CommonModule,
    DatePickerModule,
    FormsModule,
    Select,
    ButtonModule,
    TabsModule,
    TableModule,
    UserMgmtComponent,
    StartupTrackingComponent],
  templateUrl: './tracking-container.component.html',
  styleUrl: './tracking-container.component.scss'
})
export class TrackingContainerComponent implements OnInit {
  toDate = new Date();
  fromDate = new Date(this.toDate.getTime() - 24 * 60 * 60 * 1000);
  public static readonly FILTER_ALL = "ALL";
  public static readonly FILTER_GAME = "GAME";
  sortOptions!: SelectItem[];
  sortDesc = true;
  filterOptions = [{name: "All", value: TrackingContainerComponent.FILTER_ALL},
    {name: "Game", value: TrackingContainerComponent.FILTER_GAME}];
  filter = TrackingContainerComponent.FILTER_ALL;
  homePageRequests: PageRequest[] = [];
  progressStatistics: ProgressStatistic[] = [];
  startupTerminatedJsons: StartupTerminatedJson[] = [];
  startupTaskJsons: StartupTaskJson[] = [];
  private trackerControllerImplClient!: TrackerControllerImplClient;
  private trackingContainerAnalyzer = new TrackingContainerAnalyzer();

  constructor(httpClient: HttpClient) {
    this.trackerControllerImplClient = new TrackerControllerImplClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
  }

  ngOnInit(): void {
    this.sortOptions = [
      {label: 'Desc', value: true},
      {label: 'Asc', value: false},
    ];

    this.load();
  }

  load() {
    try {
      this.trackerControllerImplClient.loadTrackingContainer({
        fromDate: this.fromDate,
        toDate: this.toDate
      }).then(trackingContainer => {
        this.trackingContainerAnalyzer.setTrackingContainer(trackingContainer);
        this.progressStatistics.length = 0;
        this.progressStatistics.push(...createStatistics(this.trackingContainerAnalyzer));
        this.startupTerminatedJsons = trackingContainer.startupTerminatedJson || [];
        this.startupTaskJsons = trackingContainer.startupTaskJsons || [];
        this.onFilterChanged();
      })
    } catch (e) {
      console.log(e);
    }
  }

  load24h() {
    this.loadTime(24 * 60 * 60 * 1000)
  }

  load48h() {
    this.loadTime(48 * 60 * 60 * 1000)
  }

  load5d() {
    this.loadTime(5 * 24 * 60 * 60 * 1000)
  }

  load7d() {
    this.loadTime(7 * 24 * 60 * 60 * 1000)
  }

  load2w() {
    this.loadTime(14 * 24 * 60 * 60 * 1000)
  }

  load1m() {
    this.loadTime(30 * 24 * 60 * 60 * 1000)
  }

  private loadTime(millis: number) {
    this.toDate = new Date();
    this.fromDate = new Date(this.toDate.getTime() - millis);
    this.load();
  }

  gamePageRequest(homePageRequest: PageRequest): PageRequest[] {
    return this.trackingContainerAnalyzer.getGames4Home(homePageRequest);
  }

  onSortChange() {
    this.homePageRequests = [...this.homePageRequests].sort((a, b) => {
      const timeA = new Date(a.serverTime).getTime();
      const timeB = new Date(b.serverTime).getTime();

      return this.sortDesc ? timeB - timeA : timeA - timeB;
    });
  }

  onFilterChanged() {
    switch (this.filter) {
      case TrackingContainerComponent.FILTER_ALL:
        this.homePageRequests = this.trackingContainerAnalyzer.getDistinctHomePageRequests();
        break;
      case TrackingContainerComponent.FILTER_GAME:
        this.homePageRequests = this.trackingContainerAnalyzer.getGamePageRequests();
        break;
    }
    this.onSortChange();
  }
}
