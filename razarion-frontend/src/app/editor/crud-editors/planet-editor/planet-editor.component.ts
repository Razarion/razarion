import {Component} from '@angular/core';
import {CrudContainerChild} from '../crud-container/crud-container.component';
import {PlanetConfig, PlanetEditorControllerClient} from 'src/app/generated/razarion-share';
import {GroundComponent} from '../../common/ground/ground.component';
import {BaseItemTypeComponent} from '../../common/base-item-type/base-item-type.component';
import {InputNumber} from 'primeng/inputnumber';
import {BaseItemTypeCountComponent} from '../../common/base-item-type-count/base-item-type-count.component';
import {DecimalPositionComponent} from '../../common/decimal-position/decimal-position.component';
import {FormsModule} from '@angular/forms';
import {ButtonModule} from 'primeng/button';
import {getMiniMapPlanetUrl} from '../../../common';

@Component({
  selector: 'app-planet-editor',
  imports: [
    GroundComponent,
    BaseItemTypeComponent,
    InputNumber,
    BaseItemTypeCountComponent,
    DecimalPositionComponent,
    FormsModule,
    ButtonModule
  ],
  templateUrl: './planet-editor.component.html'
})
export class PlanetEditorComponent implements CrudContainerChild<PlanetConfig> {
  static editorControllerClient = PlanetEditorControllerClient;
  planetConfig!: PlanetConfig;

  init(planetConfig: PlanetConfig): void {
    this.planetConfig = planetConfig;
  }

  exportConfig(): PlanetConfig {
    return this.planetConfig!;
  }

  getId(): number {
    throw this.planetConfig!.id;
  }

  showMinimap() {
    window.open(getMiniMapPlanetUrl(this.planetConfig.id), '_blank');
  }
}
