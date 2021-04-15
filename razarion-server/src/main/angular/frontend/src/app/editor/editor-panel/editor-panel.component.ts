import {Component, Input, OnInit} from '@angular/core';
import {EditorModel} from "../editor-model";
import {MenuItem, MessageService} from "primeng/api";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {GwtAngularPropertyTable, ObjectNameId} from "../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'app-editor-panel',
  templateUrl: './editor-panel.component.html',
  styleUrls: ['./editor-panel.component.scss']
})
export class EditorPanelComponent implements OnInit {
  @Input("editorModel")
  editorModel!: EditorModel;
  @Input("editorModels")
  editorModels!: EditorModel[];
  items: MenuItem[] = [];
  gwtAngularPropertyTable: GwtAngularPropertyTable | null = null;

  constructor(private gwtAngularService: GwtAngularService, private messageService: MessageService) {
  }

  ngOnInit(): void {
    this.items = [
      {
        label: 'Loading', icon: 'pi-spinner',
      }
    ];

    this.requestObjectNameId();
  }

  private requestObjectNameId(): void {
    this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getGenericEditorFrontendProvider()
      .requestObjectNameIds(this.editorModel.crudControllerIndex)
      .then(value => this.setupMenuItems(value),
        reason => {
          this.messageService.add({
            severity: 'error',
            summary: `Can not load configs for: ${this.editorModel.crudControllerName}`,
            detail: reason,
            sticky: true
          });
          console.error(reason);
        });
  }

  onClose() {
    this.editorModels.splice(this.editorModels.indexOf(this.editorModel), 1);
  }

  private setupMenuItems(objectNameIds: ObjectNameId[]) {
    let menuObjectNameIds: MenuItem[] = [];

    objectNameIds.forEach(objectNameId => {
      menuObjectNameIds.push({
        label: `${objectNameId.getInternalName()} (${objectNameId.getId()})`,
        command: () => {
          this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getGenericEditorFrontendProvider()
            .readConfig(this.editorModel.crudControllerIndex, objectNameId.getId())
            .then(value => {
                this.gwtAngularPropertyTable = value;
                this.updateDeleteSaveDisableState();
              },
              reason => {
                this.messageService.add({
                  severity: 'error',
                  summary: `Can not load config for: ${this.editorModel.crudControllerName} with id: ${objectNameId.getId()}`,
                  detail: reason,
                  sticky: true
                });
                console.error(reason);
              })
        }
      });
    })

    this.items = [
      {
        label: 'Select...',
        items: menuObjectNameIds,
      },
      {
        label: "New",
        command: () => {
          this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getGenericEditorFrontendProvider()
            .createConfig(this.editorModel.crudControllerIndex).then(
            value => {
              this.gwtAngularPropertyTable = value;
              this.requestObjectNameId();
            },
            reason => {
              this.messageService.add({
                severity: 'error',
                summary: `Can not create config for: ${this.editorModel.crudControllerName}`,
                detail: reason,
                sticky: true
              });
              console.error(reason);
            });
        }
      },
      {
        label: "Save",
        disabled: this.gwtAngularPropertyTable == null,
        command: () => {
          this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getGenericEditorFrontendProvider()
            .updateConfig(this.editorModel.crudControllerIndex, this.gwtAngularPropertyTable!).then(
            () => {
              this.requestObjectNameId();
              this.messageService.add({
                severity: 'success',
                summary: 'Saved'
              });
            },
            reason => {
              this.messageService.add({
                severity: 'error',
                summary: `Can not save config for: ${this.editorModel.crudControllerName} with value : ${this.gwtAngularPropertyTable}`,
                detail: reason,
                sticky: true
              });
              console.error(reason);
            });
        }
      },
      {
        label: "Delete",
        disabled: this.gwtAngularPropertyTable == null,
        command: () => {
          this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getGenericEditorFrontendProvider()
            .deleteConfig(this.editorModel.crudControllerIndex, this.gwtAngularPropertyTable!).then(
            () => {
              this.gwtAngularPropertyTable = null;
              this.requestObjectNameId();
              this.messageService.add({
                severity: 'success',
                summary: 'Deleted'
              });
            },
            reason => {
              this.messageService.add({
                severity: 'error',
                summary: `Can not delete config for: ${this.editorModel.crudControllerName} with value : ${this.gwtAngularPropertyTable}`,
                detail: reason,
                sticky: true
              });
              console.error(reason);
            });
        }
      }
    ];
  }

  private updateDeleteSaveDisableState() {
    this.items[2].disabled = this.gwtAngularPropertyTable == null;
    this.items[3].disabled = this.gwtAngularPropertyTable == null;
    this.items = [...this.items];
  }
}
