import { Component, EventEmitter, Input, Output } from '@angular/core';
import { TerrainType } from 'src/app/generated/razarion-share';
import {DropdownModule} from 'primeng/dropdown';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'terrain-type',
  imports: [
    DropdownModule,
    FormsModule
  ],
  templateUrl: './terrain-type.component.html'
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
