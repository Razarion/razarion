import {Component, Input} from '@angular/core';
import {PerfmonEnum, PerfmonStatistic} from "../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'perfmon',
  template: '<p-chart type="bar" [data]="data" [options]="options"></p-chart>'
})
export class PerfmonComponent {
  data: any = [];
  options: any = [];
  @Input("perfmon-enum")
  perfmonEnum!: PerfmonEnum;
  @Input("stat1")
  stat1!: string;
  @Input("stat1-color")
  stat1Color!: string;
  @Input("stat1-max")
  stat1Max!: number;


  constructor() {
    this.options = {
      animation: {
        duration: 0
      },
      scales: {
        yAxes: [{
          ticks: {
            max: this.stat1Max,
            min: 0,
            maxTicksLimit: 6,
          },
          gridLines: {
            color: '#4d4d4e'
          }
        }]
      }
    };
  }

  display(perfmonStatistic: PerfmonStatistic[]) {
    let data: any = [];
    let labels: any = [];
    perfmonStatistic.forEach(perfmonStatistic => {
      if (this.perfmonEnum === ((<any>PerfmonEnum)[perfmonStatistic.getPerfmonEnumString()])) {
        perfmonStatistic.getPerfmonStatisticEntriesArray().forEach(perfmonStatisticEntry => {
          data.push(perfmonStatisticEntry.getFrequency())
          labels.push(new Date(perfmonStatisticEntry.getDateAsLong()).toLocaleTimeString())
        })
      }
    });

    if (data.length > 0) {
      this.data = {
        labels: labels,
        datasets: [
          {
            label: this.stat1,
            data: data,
            backgroundColor: this.stat1Color,
          }
        ]
      };
    }
  }
}
