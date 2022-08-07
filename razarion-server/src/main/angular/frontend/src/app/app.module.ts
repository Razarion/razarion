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
import {PropertyTableComponent} from "./editor/property-table/property-table.component";
import {DropdownModule} from 'primeng/dropdown';
import {MenubarModule} from 'primeng/menubar';
import {MessageService} from 'primeng/api';
import {ToastModule} from 'primeng/toast';
import {InputTextModule} from "primeng/inputtext";
import {EditorDialogComponent} from "./editor/editor-dialog/editor-dialog.component";
import {PropertyEditorComponent} from "./editor/property-table/property-editor.component";
import {EditorService} from "./editor/editor-service";
import {StringPropertyEditorComponent} from "./editor/property-table/editors/string-property-editor.component";
import {EnumPropertyEditorComponent} from "./editor/property-table/editors/enum-property-editor.component";
import {IntegerPropertyEditorComponent} from "./editor/property-table/editors/integer-property-editor.component";
import {IntegerMapPropertyEditorComponent} from "./editor/property-table/editors/integer-map-property-editor.component";
import {DecimalPositionPropertyEditorComponent} from "./editor/property-table/editors/decimal-position-property-editor.component";
import {DoublePropertyEditorComponent} from "./editor/property-table/editors/double-property-editor.component";
import {CollectionReferencePropertyEditorComponent} from "./editor/property-table/editors/collection-reference-property-editor.component";
import {BooleanPropertyEditorComponent} from "./editor/property-table/editors/boolean-property-editor.component";
import {PlaceConfigPropertyEditorComponent} from "./editor/property-table/editors/place-config-property-editor.component";
import {Rectangle2dPropertyEditorComponent} from "./editor/property-table/editors/rectangle-2d-property-editor.component";
import {RectanglePropertyEditorComponent} from "./editor/property-table/editors/rectangle-property-editor.component";
import {IndexPropertyEditorComponent} from "./editor/property-table/editors/index-property-editor.component";
import {VertexPropertyEditorComponent} from "./editor/property-table/editors/vertex-property-editor.component";
import {ColladaStringPropertyEditorComponent} from "./editor/property-table/editors/collada-string-property-editor.component";
import {I18nStringPropertyEditorComponent} from "./editor/property-table/editors/i18n-string-property-editor.component";
import {Polygon2dPropertyEditorComponent} from "./editor/property-table/editors/polygon-2d-property-editor.component";
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
import {CommonModule, DatePipe} from "@angular/common";
import {ServerPanelComponent} from "./editor/server-panel/server-panel.component";
import {Polygon2dEditorComponent} from "./editor/common/polygon-2d-editor.component";
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
import {ThreeJsRendererServiceImpl} from './game/renderer/three-js-renderer-service.impl';
import {GameMockService} from './game/renderer/game-mock.service';
import {ThreeJsModelService} from './game/renderer/three-js-model.service';
import {CollectionSelectorComponent} from "./editor/property-table/collection-selector.component";
import {ImagePropertyEditorComponent} from "./editor/property-table/editors/image-property-editor.component";
import {OverlayPanelModule} from "primeng/overlaypanel";
import {ListboxModule} from "primeng/listbox";
import {ToolbarModule} from "primeng/toolbar";
import {SplitterModule} from "primeng/splitter";
import {ThreeJsWaterRenderService} from "./game/renderer/three-js-water-render.service";
import {EulerPropertyEditorComponent} from "./editor/render-engine/editors/euler-property-editor.component";
import {UserdataPropertyEditorComponent} from "./editor/render-engine/editors/userdata-property-editor.component";
import {InputTextareaModule} from "primeng/inputtextarea";
import {AngleVertexEditorComponent} from "./editor/terrain-editor/angle-vertex-editor.component";
import {VertexEditorComponent} from "./editor/terrain-editor/vertex-editor.component";

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
    PropertyTableComponent,
    EditorPanelComponent,
    PropertyEditorComponent,
    StringPropertyEditorComponent,
    EnumPropertyEditorComponent,
    IntegerPropertyEditorComponent,
    IntegerMapPropertyEditorComponent,
    DecimalPositionPropertyEditorComponent,
    VertexPropertyEditorComponent,
    IndexPropertyEditorComponent,
    DoublePropertyEditorComponent,
    CollectionReferencePropertyEditorComponent,
    BooleanPropertyEditorComponent,
    PlaceConfigPropertyEditorComponent,
    Rectangle2dPropertyEditorComponent,
    RectanglePropertyEditorComponent,
    ColladaStringPropertyEditorComponent,
    I18nStringPropertyEditorComponent,
    Polygon2dPropertyEditorComponent,
    ImagePropertyEditorComponent,
    ItemCockpitComponent,
    MainCockpitComponent,
    RenderEngineComponent,
    CrashPanelComponent,
    ServerPanelComponent,
    Polygon2dEditorComponent,
    BackupRestoreComponent,
    TerrainEditorComponent,
    ImageTypePipe,
    FormatFileSizePipe,
    RadToDegreePipe,
    ImageEditorComponent,
    ImageGalleryItemComponent,
    CollectionSelectorComponent,
    EulerPropertyEditorComponent,
    UserdataPropertyEditorComponent,
    AngleVertexEditorComponent,
    VertexEditorComponent
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
    InputTextareaModule
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
    ThreeJsRendererServiceImpl,
    ThreeJsWaterRenderService,
    GameMockService,
    ThreeJsModelService
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
