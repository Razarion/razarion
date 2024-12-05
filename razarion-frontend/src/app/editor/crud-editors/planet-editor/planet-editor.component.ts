import { Component, OnInit } from '@angular/core';
import { CrudContainerChild } from '../crud-container/crud-container.component';
import { PlanetConfig, PlanetEditorControllerClient } from 'src/app/generated/razarion-share';

@Component({
  selector: 'app-planet-editor',
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


}
