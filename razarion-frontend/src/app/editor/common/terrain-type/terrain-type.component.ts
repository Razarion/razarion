import { Component, EventEmitter, Input, Output } from '@angular/core';
import { TerrainType } from 'src/app/generated/razarion-share';

@Component({
    selector: 'terrain-type',
    templateUrl: './terrain-type.component.html',
    standalone: false
})
export class TerrainTypeComponent {
  @Input("terrainType")
  terrainType: TerrainType | null = null;
  @Output()
  terrainTypeChange = new EventEmitter<TerrainType>();
  terrainTypes = [
    { label: 'Blocked', value: TerrainType.BLOCKED },
    { label: 'Land', value: TerrainType.LAND },
    { label: 'Water', value: TerrainType.WATER },
    { label: 'Land coast', value: TerrainType.LAND_COAST },
    { label: 'Water coast', value: TerrainType.WATER_COAST }
  ];

  onChange(terrainType: TerrainType) {
    this.terrainTypeChange.emit(terrainType);
  }
}
