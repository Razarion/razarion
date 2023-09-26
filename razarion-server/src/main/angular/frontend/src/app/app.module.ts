import {BrowserModule} from '@angular/platform-browser';
import {ErrorHandler, NgModule} from '@angular/core';
import {AppComponent} from './app.component';
import {FrontendService} from "./service/frontend.service";
import {HttpClientModule} from "@angular/common/http";
import {HomeComponent} from "./home/home.component";
import {GameComponent} from "./game/game.component";
import {RegisterComponent} from "./register/register.component";
import {GlobalErrorHandler} from "./global.error.fandler";
import {NoCookies} from "./nocookies/nocookies.component";
import {FacebookAppStart} from "./facebookappstart/facebook-app-start.component";
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
import {RenderEngineComponent} from "./editor/render-engine/render-engine.component";
import {ChartModule} from 'primeng/chart';
import {RadioButtonModule} from 'primeng/radiobutton';
import {CrashPanelComponent} from "./editor/crash-panel/crash-panel.component";
import {TableModule} from 'primeng/table';
import {CommonModule, DatePipe, DecimalPipe} from "@angular/common";
import {ServerPanelComponent} from "./editor/server-panel/server-panel.component";
import {BackupRestoreComponent} from "./editor/backup-restore/backup-restore.component";
import {TerrainEditorComponent} from './editor/terrain-editor/terrain-editor.component';
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
import {CollectionSelectorComponent} from "./editor/property-table/collection-selector.component";
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
import {ObjectEditorComponent} from "./editor/terrain-editor/object-editor.component";
import {SlopeTerrainEditorComponent} from "./editor/terrain-editor/slope-terrain-editor.component";
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
import {SlopeEditorComponent} from "./editor/crud-editors/slope-editor/slope-editor.component";
import {CrudContainerComponent} from './editor/crud-editors/crud-container/crud-container.component';
import {GroundComponent} from './editor/common/ground/ground.component';
import {WaterComponent} from './editor/common/water/water.component';
import {BabylonModelComponent} from './editor/common/babylon-material/babylon-model.component';
import {DrivewayEditorComponent} from './editor/crud-editors/driveway-editor/driveway-editor.component';
import {ProgressSpinnerModule} from "primeng/progressspinner";

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    GameComponent,
    RegisterComponent,
    NoCookies,
    FacebookAppStart,
    EmailVerification,
    ResetPasswordComponent,
    ChangePasswordComponent,
    LogoutComponent,
    EditorDialogComponent,
    EditorPanelComponent,
    ItemCockpitComponent,
    MainCockpitComponent,
    QuestCockpitComponent,
    RenderEngineComponent,
    CrashPanelComponent,
    ServerPanelComponent,
    BackupRestoreComponent,
    TerrainEditorComponent,
    ObjectEditorComponent,
    SlopeTerrainEditorComponent,
    ImageTypePipe,
    FormatFileSizePipe,
    RadToDegreePipe,
    ImageEditorComponent,
    ImageGalleryItemComponent,
    CollectionSelectorComponent,
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
    SlopeEditorComponent,
    CrudContainerComponent,
    GroundComponent,
    WaterComponent,
    BabylonModelComponent,
    DrivewayEditorComponent,
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
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
    ProgressSpinnerModule
  ],
  providers: [
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
    BabylonModelService
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
