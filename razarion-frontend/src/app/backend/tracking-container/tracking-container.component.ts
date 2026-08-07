import {Component, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {
  DailyProgress,
  StartupTaskJson,
  StartupTerminatedJson,
  TrackerControllerImplClient,
  TrackingPlatform
} from '../../generated/razarion-share';
import {TypescriptGenerator} from '../typescript-generator';
import {CommonModule} from '@angular/common';
import {DatePickerModule} from 'primeng/datepicker';
import {FormsModule} from '@angular/forms';
import {Select} from 'primeng/select';
import {ButtonModule} from 'primeng/button';
import {TabsModule} from 'primeng/tabs';
import {createStatistics, ProgressStatistic} from './progress-statistic';
import {TableModule} from 'primeng/table';
import {ChartModule} from 'primeng/chart';
import {ClickIdField, TrackingContainerAnalyzer} from './tracking-container-analyzer';
import {UserMgmtComponent} from '../../editor/user-mgmt/user-mgmt.component';
import {OpenConnectionsComponent} from '../../editor/open-connections/open-connections.component';
import {PlayerSessionsComponent} from '../player-sessions/player-sessions.component';
import {StartupTrackingComponent} from '../startup-tracking/startup-tracking.component';
import {AttentionTrackingComponent} from '../attention-tracking/attention-tracking.component';
import {AttentionAnalyzer, AttentionReport} from './attention-analyzer';

@Component({
  selector: 'tracking-container',
  imports: [
    CommonModule,
    DatePickerModule,
    FormsModule,
    Select,
    ButtonModule,
    TabsModule,
    TableModule,
    ChartModule,
    UserMgmtComponent,
    OpenConnectionsComponent,
    PlayerSessionsComponent,
    StartupTrackingComponent,
    AttentionTrackingComponent],
  templateUrl: './tracking-container.component.html',
  styleUrl: './tracking-container.component.scss'
})
export class TrackingContainerComponent implements OnInit {
  toDate = new Date();
  fromDate = new Date(this.toDate.getTime() - 24 * 60 * 60 * 1000);
  platformOptions = [{name: "Reddit", value: 'rdtCid' as ClickIdField},
    {name: "X", value: 'twclid' as ClickIdField}];
  platform: ClickIdField = 'rdtCid';
  progressStatistics: ProgressStatistic[] = [];
  /** Per-day funnel, newest first. Fixed 10-day window, independent of the range picker. */
  dailyProgresses: DailyProgress[] = [];
  /**
   * Level columns of the daily table; must match DAILY_PROGRESS_MIN/MAX_LEVEL on the server.
   * Starts at 2: level 1 comes free with the first base and emits no LEVEL_UP, so "Initial Base
   * created" already is the level 1 number.
   */
  readonly dailyLevels = [2, 3, 4, 5];
  dailyChartData: any;
  chartOptions: any;
  /**
   * Entry stages: green, yellow, red. Steps picked so the three clear the gates on the dark
   * surface all-pairs - in a grouped cluster every bar sits next to every other, so the
   * adjacent-pair allowance does not apply. Red against green stays in the 6-8 CVD band
   * (deutan/protan cannot separate red from green, whatever the steps), which is why identity
   * never rests on hue alone here: the legend, the fixed bar order inside a day and the table
   * below all carry it too.
   * A stage always keeps its color - never reassign by rank or by how many series are visible.
   */
  private static readonly ENTRY_COLORS = [
    '#008300', // green  - Home
    // Blue, and inserted here rather than appended, because the stage belongs between Home and
    // Game and every stage keeps the color it already had. Blue is the one hue that stays clear
    // of the red-green axis this palette already spends, so it separates from all three under
    // colour vision deficiency (validated light and dark).
    '#1f6fd0', // blue   - Play clicked
    '#c98500', // yellow - Game
    '#e34948'  // red    - Initial Base created
  ];
  /**
   * Levels are an ordered tier, so they share one hue and differ by step instead of competing
   * as five more colors: light means early, dark means deep. Validated monotone with visible
   * step gaps and a dark end that still clears the surface.
   */
  private static readonly LEVEL_COLORS = [
    '#cde2fb',
    '#86b6ef',
    '#3987e5',
    '#184f95'
  ];
  startupTerminatedJsons: StartupTerminatedJson[] = [];
  startupTaskJsons: StartupTaskJson[] = [];
  attentionReport?: AttentionReport;
  private trackerControllerImplClient!: TrackerControllerImplClient;
  private trackingContainerAnalyzer = new TrackingContainerAnalyzer();
  private attentionAnalyzer = new AttentionAnalyzer();

  constructor(httpClient: HttpClient) {
    this.trackerControllerImplClient = new TrackerControllerImplClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
  }

  ngOnInit(): void {
    this.load();
    this.loadDailyProgress();
  }

  loadDailyProgress() {
    const platform = this.platform === 'rdtCid' ? TrackingPlatform.REDDIT : TrackingPlatform.X;
    this.trackerControllerImplClient.loadDailyProgress({platform})
      .then(dailyProgresses => {
        this.dailyProgresses = dailyProgresses;
        this.updateDailyChart();
      })
      .catch(e => console.log(e));
  }

  /**
   * One group of bars per day, days left to right, counts upwards. Every funnel stage sits in
   * the same group so a day can be read as a whole and compared against its neighbours.
   * <p>
   * Home runs in the hundreds while the deep levels are a handful, so the level bars are short
   * on the shared axis. Clicking a legend entry hides that stage and rescales the axis, which
   * is how you zoom into the tail; the table below carries the exact numbers regardless.
   */
  private updateDailyChart() {
    // Server sends newest first; time reads left to right.
    const days = [...this.dailyProgresses].reverse();
    const documentStyle = getComputedStyle(document.documentElement);
    const muted = documentStyle.getPropertyValue('--p-text-muted-color')?.trim() || '#898781';

    const entrySeries = [
      {label: 'Home', values: days.map(day => day.home)},
      {label: 'Play clicked', values: days.map(day => day.playClicked)},
      {label: 'Game', values: days.map(day => day.game)},
      {label: 'Initial Base created', values: days.map(day => day.initialBaseCreated)}
    ];
    this.dailyChartData = {
      labels: days.map(day => day.day),
      datasets: [
        ...entrySeries.map((entry, index) => this.dataset(
          entry.label, entry.values, TrackingContainerComponent.ENTRY_COLORS[index])),
        ...this.dailyLevels.map((level, index) => this.dataset(
          `Level ${level}`,
          days.map(day => this.levelUp(day, level)),
          TrackingContainerComponent.LEVEL_COLORS[index]))
      ]
    };

    this.chartOptions = {
      // Height follows the width instead of the container: p-chart gives the canvas no resolved
      // height, so maintainAspectRatio:false lets it grow without bound over the table below.
      aspectRatio: 6,
      plugins: {
        legend: {
          position: 'top',
          labels: {color: muted, usePointStyle: true, pointStyle: 'rect', boxWidth: 10}
        }
      },
      scales: {
        x: {
          ticks: {color: muted},
          grid: {display: false}
        },
        y: {
          beginAtZero: true,
          ticks: {color: muted, precision: 0},
          // Recessive hairline grid, one shade off the surface.
          grid: {color: '#2c2c2a'},
          border: {color: '#383835'}
        }
      }
    };
  }

  private dataset(label: string, values: number[], color: string) {
    return {
      label,
      data: values,
      backgroundColor: color,
      // These three belong on the dataset, not on the x scale - BarController reads them from
      // its own defaults, so a scale-level copy is silently ignored.
      // Keeps a day's bars together as one group with a clear gap to the next day...
      categoryPercentage: 0.5,
      // ...and a thin surface gap between the bars inside that group, instead of borders.
      barPercentage: 0.9,
      // Thin marks. Without a cap the bars balloon on a wide screen.
      maxBarThickness: 9,
      // Rounded top ends only; the bar stays anchored to the baseline.
      borderRadius: 2,
      borderSkipped: 'bottom'
    };
  }

  /** Level count of a day, 0 when nobody reached that level. */
  levelUp(dailyProgress: DailyProgress, level: number): number {
    return dailyProgress.levelUps?.[level] ?? 0;
  }

  /**
   * Refresh: the same span of time, ending now.
   * <p>
   * Not load(). The range ends when the page was opened, so asking for it again can only return
   * what is already on screen - the refresh looked broken because it was, and reopening the page
   * was the only way to see anything that had happened since. Moving the end also hands the tabs
   * that fetch their own data a new date reference, which is what makes them reload.
   * <p>
   * A range picked by hand is slid forward too. Re-running one of those is what the date pickers
   * are for: they reload on every change, including setting the same value again.
   */
  refresh() {
    this.loadTime(this.toDate.getTime() - this.fromDate.getTime());
    // Aggregated server side over its own window, so it does not come with the range above and was
    // left standing by every refresh - only opening the page reloaded it.
    this.loadDailyProgress();
  }

  load() {
    try {
      this.trackerControllerImplClient.loadTrackingContainer({
        fromDate: this.fromDate,
        toDate: this.toDate
      }).then(trackingContainer => {
        this.trackingContainerAnalyzer.setTrackingContainer(trackingContainer);
        this.trackingContainerAnalyzer.setClickIdField(this.platform);
        this.progressStatistics.length = 0;
        this.progressStatistics.push(...createStatistics(this.trackingContainerAnalyzer));
        this.startupTerminatedJsons = trackingContainer.startupTerminatedJson || [];
        this.startupTaskJsons = trackingContainer.startupTaskJsons || [];
        this.attentionAnalyzer.setTrackingContainer(trackingContainer);
        // A fresh object every load: the attention view is driven by the input reference changing.
        this.attentionReport = this.attentionAnalyzer.createReport();
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

  onPlatformChange() {
    // Recompute the whole funnel for the selected platform; the loaded data already carries both click ids.
    this.trackingContainerAnalyzer.setClickIdField(this.platform);
    this.progressStatistics.length = 0;
    this.progressStatistics.push(...createStatistics(this.trackingContainerAnalyzer));
    // The daily funnel is aggregated server side, so it needs its own reload for the new platform.
    this.loadDailyProgress();
  }
}
