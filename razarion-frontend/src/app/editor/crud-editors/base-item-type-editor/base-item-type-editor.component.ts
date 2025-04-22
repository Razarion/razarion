import {Component} from '@angular/core';
import {
  BaseItemType,
  BaseItemTypeEditorControllerClient,
  BuilderType,
  ConsumerType,
  FactoryType,
  GeneratorType,
  HarvesterType,
  HouseType,
  ItemContainerType,
  SpecialType,
  WeaponType
} from 'src/app/generated/razarion-share';
import {CrudContainerChild} from '../crud-container/crud-container.component';
import {InputNumber} from 'primeng/inputnumber';
import {AngleComponent} from '../../common/angle/angle.component';
import {Divider} from 'primeng/divider';
import {FormsModule} from '@angular/forms';
import {Panel} from 'primeng/panel';
import {BaseItemTypesComponent} from '../../common/base-item-types/base-item-types.component';
import {ParticleSystemComponent} from '../../common/particle-system/particle-system.component';
import {VelocityComponent} from '../../common/velocity/velocity.component';
import {Checkbox} from 'primeng/checkbox';
import {TerrainTypeComponent} from '../../common/terrain-type/terrain-type.component';
import {Model3dComponent} from '../../common/model3d/model3d.component';
import {ImageItemComponent} from '../../common/image-item/image-item.component';
import {AccelerationComponent} from '../../common/acceleration/acceleration.component';
import {ScrollPanel} from 'primeng/scrollpanel';
import {NgIf} from '@angular/common';

@Component({
  selector: 'base-item-type-editor',
  imports: [
    InputNumber,
    AngleComponent,
    Divider,
    FormsModule,
    Panel,
    BaseItemTypesComponent,
    ParticleSystemComponent,
    VelocityComponent,
    Checkbox,
    TerrainTypeComponent,
    Model3dComponent,
    ImageItemComponent,
    AccelerationComponent,
    ScrollPanel,
    NgIf
  ],
  templateUrl: './base-item-type-editor.component.html'
})
export class BaseItemTypeEditorComponent implements CrudContainerChild<BaseItemType> {
  static editorControllerClient = BaseItemTypeEditorControllerClient;
  baseItemType!: BaseItemType;

  init(baseItemType: BaseItemType): void {
    this.baseItemType = baseItemType;
  }

  exportConfig(): BaseItemType {
    return this.baseItemType!;
  }

  getId(): number {
    return this.baseItemType!.id;
  }

  onBuilderTypeChange(avtive: boolean) {
    if (avtive) {
      this.baseItemType.builderType = new class implements BuilderType {
        range = 1;
        progress = 1;
        ableToBuildIds = [];
        particleSystemConfigId = null;
      };
    } else {
      this.baseItemType.builderType = null;
    }
  }

  onFactoryTypeChange(avtive: boolean) {
    if (avtive) {
      this.baseItemType.factoryType = new class implements FactoryType {
        progress = 1;
        ableToBuildIds = [];
      };
    } else {
      this.baseItemType.factoryType = null;
    }
  }

  onHarvesterTypeChange(avtive: boolean) {
    if (avtive) {
      this.baseItemType.harvesterType = new class implements HarvesterType {
        range = 1;
        progress = 1;
        particleSystemConfigId = null;
      };
    } else {
      this.baseItemType.harvesterType = null;
    }
  }

  onWeaponTypeChange(avtive: boolean) {
    if (avtive) {
      this.baseItemType.weaponType = new class implements WeaponType {
        range = 1;
        damage = 1;
        detonationRadius = 1;
        reloadTime = 1;
        disallowedItemTypes = [];
        projectileSpeed = 1;
        muzzleFlashParticleSystemConfigId = null;
        trailParticleSystemConfigId = null;
        turretType = <any>null;
        muzzleFlashAudioItemConfigId = <any>null;
      };
    } else {
      this.baseItemType.weaponType = null;
    }
  }

  onGeneratorTypeChange(avtive: boolean) {
    if (avtive) {
      this.baseItemType.generatorType = new class implements GeneratorType {
        wattage = 1;
      };
    } else {
      this.baseItemType.generatorType = null;
    }
  }

  onConsumerTypeChange(avtive: boolean) {
    if (avtive) {
      this.baseItemType.consumerType = new class implements ConsumerType {
        wattage = 1;
      };
    } else {
      this.baseItemType.consumerType = null;
    }
  }

  onItemContainerTypeChange(avtive: boolean) {
    if (avtive) {
      this.baseItemType.itemContainerType = new class implements ItemContainerType {
        ableToContain = [];
        maxCount = 1;
        range = 1;
      };
    } else {
      this.baseItemType.itemContainerType = null;
    }
  }

  onHouseTypeChange(avtive: boolean) {
    if (avtive) {
      this.baseItemType.houseType = new class implements HouseType {
        space = 1;
      };
    } else {
      this.baseItemType.houseType = null;
    }
  }

  onSpecialTypeChange(avtive: boolean) {
    if (avtive) {
      this.baseItemType.specialType = new class implements SpecialType {
        miniTerrain = false;
      };
    } else {
      this.baseItemType.specialType = null;
    }
  }
}
