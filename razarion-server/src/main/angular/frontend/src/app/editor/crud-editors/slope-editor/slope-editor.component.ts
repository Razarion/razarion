import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {SLOPE_EDITOR_PATH} from "../../../common";
import {SlopeConfig, SlopeShape} from "../../../generated/razarion-share";
import 'chartjs-plugin-dragdata'
import {UIChart} from "primeng/chart/chart";
import {EditorService} from "../../editor-service";
import {CrudContainerChild} from "../crud-container/crud-container.component";

@Component({
  selector: 'slope-editor',
  templateUrl: './slope-editor.component.html'
})
export class SlopeEditorComponent implements CrudContainerChild<SlopeConfig>, OnInit, AfterViewInit {
  public static readonly editorUrl = SLOPE_EDITOR_PATH;
  slopeConfig!: SlopeConfig;
  data: any;
  options: any;
  plugins!: any[];
  @ViewChild("chart")
  uiChart?: UIChart;
  insertMode: boolean = false;
  deleteMode: boolean = false;
  private waterLevel?: number;

  constructor(private editorService: EditorService) {
  }

  init(slopeConfig: SlopeConfig) {
    this.slopeConfig = slopeConfig;
    if (this.slopeConfig.waterConfigId) {
      this.editorService.readWater(this.slopeConfig.waterConfigId).then(waterConfig => this.waterLevel = waterConfig.waterLevel)
    }
  }

  exportConfig(): SlopeConfig {
    let slopeShapes: SlopeShape[] = [];
    if(this.data.datasets[0].data) {
      this.data.datasets[0].data.forEach((value: any) => slopeShapes.push({
        position: {x: value.x, y: value.y},
        slopeFactor: 0
      }));
    }
    this.slopeConfig.slopeShapes = slopeShapes;
    return this.slopeConfig;
  }

  getId(): number {
    return this.slopeConfig.id;
  }

  ngOnInit() {
    let shapes: any = [];
    if (this.slopeConfig.slopeShapes) {
      this.slopeConfig.slopeShapes.forEach(slopeShape => {
        shapes.push(
          slopeShape.position ?
            {
              "x": slopeShape.position.x,
              "y": slopeShape.position.y
            } : {
              "x": 0,
              "y": 0
            });
      })
    }
    this.data = {
      datasets: [
        {
          label: "Ground",
          data: shapes,
          backgroundColor: "rgba(255, 99, 132, 1)",
          borderColor: "rgba(255, 99, 132, 1)",
          borderWidth: 2.5,
          fill: false,
          pointRadius: 2,
          pointHitRadius: 25,
          showLine: true
        }
      ]
    };

    this.options = {
      layout: {
        padding: {
          left: 20,
          right: 20,
          top: 20,
          bottom: 10
        }
      },
      scales: {
        x: {
          beginAtZero: true,
          grid: {
            color: '#333333'
          }

        },
        y: {
          beginAtZero: true,
          grid: {
            color: '#333333'
          }

        }
      },
      plugins: {
        legend: {
          display: false
        },
        dragData: {
          round: 2,
          showTooltip: true,
          // IMPORTANT - you also need to specify dragX
          dragX: true,
          onDragStart: (event: any, datasetIndex: any, index: any) => {
            if (index === 0) {
              return false;
            }
            if (this.deleteMode) {
              this.uiChart!.chart.data.datasets[datasetIndex].data.splice(index, 1);
              this.uiChart!.chart.update();
              return false;
            }
            return true;
          },

        }
      }
    }

    this.plugins = [
      {
        id: 'draw-water-delimiter-line',
        beforeDraw: (chart: any, args: any, options: any) => {
          const {ctx} = chart;
          ctx.save();

          if (this.slopeConfig.outerLineGameEngine || this.slopeConfig.outerLineGameEngine === 0) {
            ctx.fillStyle = "#003300";
            ctx.fillRect(0, 0, this.calculateToPixel(this.slopeConfig.outerLineGameEngine, 0).x, chart.height);
          }

          if (this.slopeConfig.innerLineGameEngine || this.slopeConfig.innerLineGameEngine === 0) {
            ctx.fillStyle = "#003300";
            ctx.fillRect(this.calculateToPixel(this.slopeConfig.innerLineGameEngine, 0).x, 0, chart.width, chart.height);
          }


          if (this.waterLevel || this.waterLevel === 0) {
            ctx.globalAlpha = 0.2;
            ctx.fillStyle = "#0000FF";
            ctx.fillRect(0, this.calculateToPixel(0, this.waterLevel).y, chart.width, chart.height);
          }

          ctx.restore();
        },
      }];

  }

  ngAfterViewInit() {
    let chart = this.uiChart!.chart;
    this.uiChart!.getCanvas().addEventListener('click', (event: any) => {
      if (!this.insertMode || this.deleteMode) {
        return;
      }

      let value = this.calculateFromPixel(event);
      chart.data.labels.push('');
      chart.data.datasets[0].data.push({x: value.x, y: value.y});

      chart.update();
    });
  }

  public updateChart() {
    this.uiChart!.chart.update();
  }

  private calculateFromPixel(event: any): { x: number, y: number } {
    return {
      x: this.uiChart!.chart.scales.x.getValueForPixel(event.offsetX),
      y: this.uiChart!.chart.scales.y.getValueForPixel(event.offsetY)
    }
  }

  private calculateToPixel(x: number, y: number): { x: number, y: number } {
    return {
      x: this.uiChart!.chart.scales.x.getPixelForValue(x),
      y: this.uiChart!.chart.scales.y.getPixelForValue(y)
    }
  }
}
