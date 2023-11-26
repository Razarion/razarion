import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { SLOPE_EDITOR_PATH } from "../../../common";
import { SlopeConfig, SlopeShape } from "../../../generated/razarion-share";
import 'chartjs-plugin-dragdata'
import 'chartjs-plugin-zoom';
import zoomPlugin from 'chartjs-plugin-zoom';
import { Chart } from 'chart.js';
import { UIChart } from "primeng/chart/chart";
import { EditorService } from "../../editor-service";
import { Vector2 } from "@babylonjs/core";
import { CrudContainerChild } from '../crud-container/crud-container.component';

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
  length?: number;

  constructor(private editorService: EditorService) {
    Chart.register(zoomPlugin);
  }

  init(slopeConfig: SlopeConfig) {
    this.slopeConfig = slopeConfig;
    if (this.slopeConfig.waterConfigId) {
      this.editorService.readWater(this.slopeConfig.waterConfigId).then(waterConfig => this.waterLevel = waterConfig.waterLevel)
    }
  }

  exportConfig(): SlopeConfig {
    let slopeShapes: SlopeShape[] = [];
    if (this.data.datasets[0].data) {
      this.data.datasets[0].data.forEach((value: any) => slopeShapes.push({
        position: { x: value.x, y: value.y },
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
      });
    }
    this.calculateLength(shapes);

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
      aspectRatio: 1,
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
          max: 20,
          grid: {
            color: '#333333'
          }

        },
        y: {
          beginAtZero: true,
          max: 20,
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
              this.calculateLength(this.uiChart!.chart.data.datasets[0].data);
              return false;
            }
            return true;
          },
          onDragEnd: (event: any, datasetIndex: any, index: any, value: any) => {
            this.calculateLength(this.uiChart!.chart.data.datasets[0].data);
          },

        },
        zoom: {
          zoom: {
            wheel: {
              enabled: true,
            },
            pinch: {
              enabled: true
            },
            mode: 'xy',
          }
        }
      }
    }

    this.plugins = [
      {
        id: 'draw-water-delimiter-line',
        beforeDraw: (chart: any, args: any, options: any) => {
          const { ctx } = chart;
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
      if (chart.data.datasets[0].data.length === 0) {
        chart.data.datasets[0].data.push({ x: 0, y: 0 });
      } else {
        chart.data.datasets[0].data.push({ x: value.x, y: value.y });
      }

      chart.update();
      this.calculateLength(chart.data.datasets[0].data);
    });
  }

  public updateChart() {
    this.uiChart!.chart.update();
  }

  restartPlanetWarm() {
    this.editorService.executeServerCommand(EditorService.RESTART_PLANET_WARM);
  }

  restartPlanetCold() {
    this.editorService.executeServerCommand(EditorService.RESTART_PLANET_COLD);
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

  private calculateLength(data: any[]) {
    if (data && data.length > 1) {
      this.length = 0;
      for (let i = 0; i < data.length - 1; i++) {
        this.length += new Vector2(data[i].x, data[i].y)
          .subtract(new Vector2(data[i + 1].x, data[i + 1].y))
          .length();
      }
    } else {
      this.length = undefined;
    }
  }
}
