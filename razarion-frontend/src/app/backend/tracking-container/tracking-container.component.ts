import {Component, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {PageRequest, TrackerControllerImplClient} from '../../generated/razarion-share';
import {TypescriptGenerator} from '../typescript-generator';
import {DataViewModule} from 'primeng/dataview';
import {CommonModule} from '@angular/common';
import {DatePickerModule} from 'primeng/datepicker';
import {FormsModule} from '@angular/forms';
import {SelectModule} from 'primeng/select';
import {ButtonModule} from 'primeng/button';
import {SelectItem} from 'primeng/api';
import {TabsModule} from 'primeng/tabs';
import {createStatistics, ProgressStatistic} from './progress-statistic';
import {TableModule} from 'primeng/table';
import {TrackingContainerAnalyzer} from './tracking-container-analyzer';

@Component({
  selector: 'tracking-container',
  imports: [DataViewModule,
    CommonModule,
    DatePickerModule,
    FormsModule,
    SelectModule,
    ButtonModule,
    TabsModule,
    TableModule],
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
  progressStatistics: ProgressStatistic[] = []
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
        this.onFilterChanged();
      })
    } catch (e) {
      console.log(e);
    }
  }

  gamePageRequest(homePageRequest: PageRequest): PageRequest[] {
    return this.trackingContainerAnalyzer.getGames4Home(homePageRequest);
  }

  onSortChange() {
    if (!this.homePageRequests || this.homePageRequests.length === 0) {
      return;
    }

    this.homePageRequests.sort((a: PageRequest, b: PageRequest) => {
      const timeA = new Date(a.serverTime).getTime();
      const timeB = new Date(b.serverTime).getTime();

      return this.sortDesc
        ? timeB - timeA
        : timeA - timeB;
    });
  }

  onFilterChanged() {
    this.homePageRequests.length = 0;
    switch (this.filter) {
      case TrackingContainerComponent.FILTER_ALL:
        this.homePageRequests.push(...this.trackingContainerAnalyzer.getHomePageRequests());
        break;
      case TrackingContainerComponent.FILTER_GAME:
        this.homePageRequests.push(...this.trackingContainerAnalyzer.getGamePageRequests());
        break;
    }
    this.onSortChange();
  }
}
