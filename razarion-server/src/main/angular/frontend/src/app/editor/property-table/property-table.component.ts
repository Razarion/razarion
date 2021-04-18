import {Component} from '@angular/core';
import {GwtAngularPropertyTable, ObjectNameId} from "../../gwtangular/GwtAngularFacade";
import {MenuItem, MessageService} from "primeng/api";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {EditorPanel, GenericPropertyEditorModel} from "../editor-model";

@Component({
  selector: 'property-table',
  templateUrl: './property-table.component.html',
  styleUrls: ['./property-table.component.scss']
})
export class PropertyTableComponent extends EditorPanel {
  gwtAngularPropertyTable: GwtAngularPropertyTable | null = null;
  items: MenuItem[] = [
    {
      label: 'Loading', icon: 'pi-spinner',
    }
  ];

  constructor(private gwtAngularService: GwtAngularService, private messageService: MessageService) {
    super();
  }

  onEditorModel(): void {
    try {
      this.requestObjectNameId();
    } catch (error) {
      this.messageService.add({
        severity: 'error',
        summary: `Can not read ObjectNameId: ${(<GenericPropertyEditorModel>this.editorModel).crudControllerName}`,
        detail: error,
        sticky: true
      });
      console.error(error);
    }
  }

  private requestObjectNameId(): void {
    this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getGenericEditorFrontendProvider()
      .requestObjectNameIds((<GenericPropertyEditorModel>this.editorModel).crudControllerIndex)
      .then(value => this.setupMenuItems(value),
        reason => {
          this.messageService.add({
            severity: 'error',
            summary: `Can not load configs for: ${(<GenericPropertyEditorModel>this.editorModel).crudControllerName}`,
            detail: reason,
            sticky: true
          });
          console.error(reason);
        });
  }

  private setupMenuItems(objectNameIds: ObjectNameId[]) {
    let menuObjectNameIds: MenuItem[] = [];

    objectNameIds.forEach(objectNameId => {
      menuObjectNameIds.push({
        label: `${objectNameId.getInternalName()} (${objectNameId.getId()})`,
        command: () => {
          this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getGenericEditorFrontendProvider()
            .readConfig((<GenericPropertyEditorModel>this.editorModel).crudControllerIndex, objectNameId.getId())
            .then(value => {
                this.gwtAngularPropertyTable = value;
                this.updateDeleteSaveDisableState();
              },
              reason => {
                this.messageService.add({
                  severity: 'error',
                  summary: `Can not load config for: ${(<GenericPropertyEditorModel>this.editorModel).crudControllerName} with id: ${objectNameId.getId()}`,
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
            .createConfig((<GenericPropertyEditorModel>this.editorModel).crudControllerIndex).then(
            value => {
              this.gwtAngularPropertyTable = value;
              this.requestObjectNameId();
            },
            reason => {
              this.messageService.add({
                severity: 'error',
                summary: `Can not create config for: ${(<GenericPropertyEditorModel>this.editorModel).crudControllerName}`,
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
            .updateConfig((<GenericPropertyEditorModel>this.editorModel).crudControllerIndex, this.gwtAngularPropertyTable!).then(
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
                summary: `Can not save config for: ${(<GenericPropertyEditorModel>this.editorModel).crudControllerName} with value : ${this.gwtAngularPropertyTable}`,
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
            .deleteConfig((<GenericPropertyEditorModel>this.editorModel).crudControllerIndex, this.gwtAngularPropertyTable!).then(
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
                summary: `Can not delete config for: ${(<GenericPropertyEditorModel>this.editorModel).crudControllerName} with value : ${this.gwtAngularPropertyTable}`,
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
