import {BrowserModule} from '@angular/platform-browser';
import {ErrorHandler, NgModule} from '@angular/core';
import {AppComponent} from './app.component';
import {FrontendService} from "./service/frontend.service";
import {provideHttpClient, withInterceptorsFromDi} from "@angular/common/http";
import {HomeComponent} from "./home/home.component";
import {GameComponent} from "./game/game.component";
import {RegisterComponent} from "./register/register.component";
import {GlobalErrorHandler} from "./global.error.fandler";
import {NoCookies} from "./nocookies/nocookies.component";
import {EmailVerification} from "./emailverification/email-verification.component";
import {FormsModule} from "@angular/forms";
import {ResetPasswordComponent} from "./resetpassword/reset-password.component";
import {LogoutComponent} from "./logout/logout.component";
import {ChangePasswordComponent} from "./resetpassword/change-password.component";
import {AppRoutingModule} from "./app-routing.module";
import {TreeModule} from "primeng/tree";
import {TreeTableModule} from "primeng/treetable";
import {CardModule} from 'primeng/card';
import {PanelModule} from 'primeng/panel';
import {ButtonModule} from "primeng/button";
import {GwtAngularService} from "./gwtangular/GwtAngularService";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {SidebarModule} from 'primeng/sidebar';
import {DialogModule} from 'primeng/dialog';
import {EditorPanelComponent} from './editor/editor-panel/editor-panel.component';
import {DropdownModule} from 'primeng/dropdown';
import {MenubarModule} from 'primeng/menubar';
import {MessageService} from 'primeng/api';
import {ToastModule} from 'primeng/toast';
import {InputTextModule} from "primeng/inputtext";
import {EditorDialogComponent} from "./editor/editor-dialog/editor-dialog.component";
import {EditorService} from "./editor/editor-service";
import {InputNumberModule} from 'primeng/inputnumber';
import {InputSwitchModule} from 'primeng/inputswitch';
import {CarouselModule} from 'primeng/carousel';
import {ItemCockpitComponent} from "./game/cockpit/item/item-cockpit.component";
import {MainCockpitComponent} from "./game/cockpit/main/main-cockpit.component";
import {ChartModule} from 'primeng/chart';
import {RadioButtonModule} from 'primeng/radiobutton';
import {CrashPanelComponent} from "./editor/crash-panel/crash-panel.component";
import {TableModule} from 'primeng/table';
import {CommonModule, DatePipe, DecimalPipe} from "@angular/common";
import {ServerPanelComponent} from "./editor/server-panel/server-panel.component";
import {BackupRestoreComponent} from "./editor/backup-restore/backup-restore.component";
import {TabViewModule} from 'primeng/tabview';
import {CheckboxModule} from 'primeng/checkbox';
import {SliderModule} from 'primeng/slider';
import {DataViewModule} from 'primeng/dataview';
import {FormatFileSizePipe} from "./common/pipes/format-file-size-pipe";
import {ImageEditorComponent} from './editor/image-editor/image-editor.component';
import {ImageGalleryItemComponent, ImageTypePipe} from "./editor/image-editor/image-gallery-item.component";
import {FileUploadModule} from 'primeng/fileupload';
import {RadToDegreePipe} from "./common/pipes/rad-to-degree-pipe";
import {BabylonRenderServiceAccessImpl} from './game/renderer/babylon-render-service-access-impl.service';
import {GameMockService} from './game/renderer/game-mock.service';
import {BabylonModelService} from './game/renderer/babylon-model.service';
import {OverlayPanelModule} from "primeng/overlaypanel";
import {ListboxModule} from "primeng/listbox";
import {ToolbarModule} from "primeng/toolbar";
import {SplitterModule} from "primeng/splitter";
import {ThreeJsWaterRenderService} from "./game/renderer/three-js-water-render.service";
import {InputTextareaModule} from "primeng/inputtextarea";
import {AngleVector3EditorComponent} from "./editor/terrain-editor/angle-vector3-editor.component";
import {Vector3EditorComponent} from "./editor/terrain-editor/vector3-editor.component";
import {TerrainObjectPositionComponent} from "./editor/terrain-editor/terrain-object-position.component";
import {ChipsModule} from "primeng/chips";
import {DividerModule} from "primeng/divider";
import {SelectButtonModule} from "primeng/selectbutton";
import {ObjectTerrainEditorComponent} from "./editor/terrain-editor/object-terrain-editor.component";
import {QuestCockpitComponent} from "./game/cockpit/quest/quest-cockpit.component";
import {ServerQuestEditorComponent} from "./editor/server-quest-editor/server-quest-editor.component";
import {AccordionModule} from "primeng/accordion";
import {BaseItemTypeCountComponent} from './editor/common/base-item-type-count/base-item-type-count.component';
import {BaseItemTypeComponent} from './editor/common/base-item-type/base-item-type.component';
import {PlaceConfigComponent} from './editor/common/place-config/place-config.component';
import {ToggleButtonModule} from "primeng/togglebutton";
import {ServerBotEditorComponent} from './editor/server-bot-editor/server-bot-editor.component';
import {LevelComponent} from "./editor/common/level/level.component";
import {ServerStartRegionComponent} from './editor/server-start-region/server-start-region.component';
import {DecimalPositionComponent} from './editor/common/decimal-position/decimal-position.component';
import {ServerResourceRegionComponent} from "./editor/server-resource-region/server-resource-region.component";
import {ResourceItemTypeComponent} from './editor/common/resource-item-type/resource-item-type.component';
import {GroundComponent} from './editor/common/ground/ground.component';
import {WaterComponent} from './editor/common/water/water.component';
import {ProgressSpinnerModule} from "primeng/progressspinner";
import {MenuModule} from 'primeng/menu';
import {
  TerrainObjectGeneratorComponent
} from './editor/terrain-editor/terrain-object-generator/terrain-object-generator.component';
import {BackendComponent} from './backend/backend.component';
import {LevelEditorComponent} from './editor/crud-editors/level-editor/level-editor.component';
import {BaseItemTypeEditorComponent} from './editor/crud-editors/base-item-type-editor/base-item-type-editor.component';
import {AbstractCrudContainerComponent} from './editor/crud-editors/crud-container/crud-container.component';
import {ManuallyCrudContainerComponent} from './editor/crud-editors/crud-container/manually-crud-container.component';
import {GeneratedCrudContainerComponent} from './editor/crud-editors/crud-container/generated-crud-container.component';
import {TerrainTypeComponent} from './editor/common/terrain-type/terrain-type.component';
import {VelocityComponent} from './editor/common/velocity/velocity.component';
import {AccelerationComponent} from './editor/common/acceleration/acceleration.component';
import {AngleComponent} from './editor/common/angle/angle.component';
import {BaseItemTypesComponent} from './editor/common/base-item-types/base-item-types.component';
import {ParticleSystemComponent} from './editor/common/particle-system/particle-system.component';
import {ImageItemComponent} from './editor/common/image-item/image-item.component';
import {ImageGalleryComponent} from './editor/common/image-item/image-gallery.component';
import {KnobModule} from 'primeng/knob';
import {BadgeModule} from 'primeng/badge';
import {PlanetEditorComponent} from './editor/crud-editors/planet-editor/planet-editor.component';
import {RadarComponent} from './game/cockpit/main/radar/radar.component';
import {RadarNoPowerComponent} from './game/cockpit/main/radar/radar-no-power.component';
import {BaseMgmtComponent} from './editor/base-mgmt/base-mgmt.component';
import {BoxItemTypeEditorComponent} from './editor/crud-editors/box-item-type-editor/box-item-type-editor.component';
import {
  InventoryItemEditorComponent
} from './editor/crud-editors/inventory-item-editor/inventory-item-editor.component';
import {BoxItemTypeComponent} from './editor/common/box-item-type/box-item-type.component';
import {InventoryItemComponent} from './editor/common/inventory-item/inventory-item.component';
import {PercentInputComponent} from './editor/common/percent-input/percent-input.component';
import {BoxRegionComponent} from './editor/box-region/box-region.component';
import {InventoryComponent} from './game/inventory/inventory.component';
import {QuestDialogComponent} from './game/cockpit/quest/quest-dialog/quest-dialog.component';
import {UnlockComponent} from './game/unlock/unlock.component';
import {UserMgmtComponent} from './editor/user-mgmt/user-mgmt.component';
import {ActionService} from './game/action.service';
import {TerrainEditorComponent} from "./editor/terrain-editor/terrain-editor.component";
import {ShapeTerrainEditorComponent} from "./editor/terrain-editor/shape-terrain-editor.component";
import {FixHeightBrushComponent} from './editor/terrain-editor/brushes/fix-height-brush.component';
import {FlattenBrushComponent} from "./editor/terrain-editor/brushes/flattem-brush.component";
import {
  ParticleSystemEditorComponent
} from './editor/crud-editors/particle-system-editor/particle-system-editor.component';
import {VertexEditorComponent} from './editor/common/vertex-editor/vertex-editor.component';
import {
  BabylonMaterialEditorComponent
} from './editor/crud-editors/babylon-material-editor/babylon-material-editor.component';
import {BabylonMaterialComponent} from './editor/common/babylon-material/babylon-material.component';
import {ScrollPanelModule} from "primeng/scrollpanel";
import {I18nComponent} from './editor/common/i18n/i18n.component';
import {UiConfigCollectionService} from "./game/ui-config-collection.service";
import {PropertyEditorComponent} from './editor/property-editor/property-editor.component';
import {GltfEditorComponent} from "./editor/crud-editors/gltf-editor/gltf-editor.component";
import {Model3dComponent} from "./editor/common/model3d/model3d.component";
import {
  TerrainObjectEditorComponent
} from "./editor/crud-editors/terrain-object-editor/terrain-object-editor.component";
import {GroundEditorComponent} from "./editor/crud-editors/ground-editor/ground-editor.component";
import {
  ResourceItemTypeEditorComponent
} from "./editor/crud-editors/resource-item-type-editor/resource-item-type-editor.component";

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    GameComponent,
    RegisterComponent,
    NoCookies,
    EmailVerification,
    ResetPasswordComponent,
    ChangePasswordComponent,
    LogoutComponent,
    EditorDialogComponent,
    EditorPanelComponent,
    ItemCockpitComponent,
    MainCockpitComponent,
    QuestCockpitComponent,
    CrashPanelComponent,
    ServerPanelComponent,
    BackupRestoreComponent,
    ObjectTerrainEditorComponent,
    ImageTypePipe,
    FormatFileSizePipe,
    RadToDegreePipe,
    ImageEditorComponent,
    ImageGalleryItemComponent,
    AngleVector3EditorComponent,
    Vector3EditorComponent,
    TerrainObjectPositionComponent,
    ServerQuestEditorComponent,
    BaseItemTypeCountComponent,
    BaseItemTypeComponent,
    PlaceConfigComponent,
    ServerBotEditorComponent,
    LevelComponent,
    ServerStartRegionComponent,
    DecimalPositionComponent,
    ServerResourceRegionComponent,
    ResourceItemTypeComponent,
    AbstractCrudContainerComponent,
    ManuallyCrudContainerComponent,
    GeneratedCrudContainerComponent,
    GroundComponent,
    WaterComponent,
    TerrainObjectGeneratorComponent,
    BackendComponent,
    LevelEditorComponent,
    BaseItemTypeEditorComponent,
    TerrainTypeComponent,
    VelocityComponent,
    AccelerationComponent,
    AngleComponent,
    BaseItemTypesComponent,
    ParticleSystemComponent,
    ImageItemComponent,
    ImageGalleryComponent,
    PlanetEditorComponent,
    RadarComponent,
    RadarNoPowerComponent,
    BaseMgmtComponent,
    BoxItemTypeEditorComponent,
    InventoryItemEditorComponent,
    BoxItemTypeComponent,
    InventoryItemComponent,
    PercentInputComponent,
    BoxRegionComponent,
    InventoryComponent,
    QuestDialogComponent,
    UnlockComponent,
    UserMgmtComponent,
    TerrainEditorComponent,
    ShapeTerrainEditorComponent,
    FixHeightBrushComponent,
    FlattenBrushComponent,
    ParticleSystemComponent,
    ParticleSystemEditorComponent,
    VertexEditorComponent,
    BabylonMaterialEditorComponent,
    BabylonMaterialComponent,
    I18nComponent,
    PropertyEditorComponent,
    GltfEditorComponent,
    Model3dComponent,
    TerrainObjectEditorComponent,
    GroundEditorComponent,
    ResourceItemTypeEditorComponent
  ],
  bootstrap: [AppComponent], imports: [BrowserModule,
    FormsModule,
    AppRoutingModule,
    TreeTableModule,
    ButtonModule,
    BrowserAnimationsModule,
    SidebarModule,
    DialogModule,
    DropdownModule,
    MenubarModule,
    ToastModule,
    InputTextModule,
    InputNumberModule,
    InputSwitchModule,
    CarouselModule,
    ChartModule,
    RadioButtonModule,
    TableModule,
    TabViewModule,
    CheckboxModule,
    SliderModule,
    DataViewModule,
    FileUploadModule,
    CommonModule,
    CardModule,
    PanelModule,
    TreeModule,
    OverlayPanelModule,
    ListboxModule,
    ToolbarModule,
    SplitterModule,
    InputTextareaModule,
    ChipsModule,
    DividerModule,
    SelectButtonModule,
    AccordionModule,
    ToggleButtonModule,
    ProgressSpinnerModule,
    MenuModule,
    KnobModule,
    BadgeModule,
    ScrollPanelModule], providers: [
    FrontendService, {
      provide: ErrorHandler,
      useClass: GlobalErrorHandler
    },
    GwtAngularService,
    MessageService,
    EditorService,
    DatePipe,
    DecimalPipe,
    BabylonRenderServiceAccessImpl,
    ThreeJsWaterRenderService,
    GameMockService,
    BabylonModelService,
    ActionService,
    UiConfigCollectionService,
    provideHttpClient(withInterceptorsFromDi())
  ]
})
export class AppModule {
}
