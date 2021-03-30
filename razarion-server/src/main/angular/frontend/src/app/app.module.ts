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
import {TreeTableModule} from "primeng/treetable";
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
    StringPropertyEditorComponent
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
    InputTextModule
  ],
  providers: [
    FrontendService, {
      provide: ErrorHandler,
      useClass: GlobalErrorHandler
    },
    GwtAngularService,
    MessageService,
    EditorService
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
