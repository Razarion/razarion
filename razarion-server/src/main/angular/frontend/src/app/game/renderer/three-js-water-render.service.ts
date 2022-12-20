import {Injectable} from "@angular/core";
import {ShallowWaterConfig, TerrainWaterTile, WaterConfig} from "../../gwtangular/GwtAngularFacade";
import {ThreeJsTerrainTileImpl} from "./three-js-terrain-tile.impl";
import {SignalGenerator} from "../signal-generator";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {AssetContainer, Vector3} from "@babylonjs/core";

@Injectable()
export class ThreeJsWaterRenderService {
  private materials: {
    material: any,
    waterConfig: WaterConfig,
    shallowWaterConfig: ShallowWaterConfig | null
  }[] = [];

  constructor(private gwtAngularService: GwtAngularService) {
  }

  private static getWaterAnimation(millis: number, durationSeconds: number): number {
    return SignalGenerator.sawtooth(millis, durationSeconds * 1000.0, 0);
  }

  private static setupWaterGeometry(positions: Float32Array) {
    let geometry: any = {};
    geometry.setAttribute('position', positions);
    geometry.setAttribute('normal', ThreeJsTerrainTileImpl.fillVec3(new Vector3(0, 0, 1), positions.length));
    geometry.setAttribute('uv', ThreeJsTerrainTileImpl.uvFromPosition(positions));
    return geometry;
  }


  public setup(terrainWaterTiles: TerrainWaterTile[], container: AssetContainer): void {
    if (!terrainWaterTiles) {
      return;
    }
    terrainWaterTiles.forEach(terrainWaterTile => {
    });
  }

  public update() {
    this.materials.forEach(value => {
      let uniforms = value.material.uniforms;
      uniforms.uDistortionAnimation.value = ThreeJsWaterRenderService.getWaterAnimation(Date.now(), value.waterConfig.getDistortionAnimationSeconds());
      if (uniforms.uShallowAnimation && value.shallowWaterConfig) {
        uniforms.uShallowAnimation.value = ThreeJsWaterRenderService.getWaterAnimation(Date.now(), value.shallowWaterConfig.getDurationSeconds());
      }
    })
  }

}
