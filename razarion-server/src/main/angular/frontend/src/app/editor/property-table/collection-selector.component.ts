import {EditorPanel, GenericPropertyEditorModel} from "../editor-model";
import {Component} from "@angular/core";
import {MenuItem, MessageService} from "primeng/api";
import {GwtAngularPropertyTable, ObjectNameId} from "../../gwtangular/GwtAngularFacade";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";

@Component({
  selector: 'collection-selector',
  template: `
    <p-menubar class="mb-0" [model]="items"></p-menubar>
    <property-table [angular-tree-node-data]=gwtAngularPropertyTable></property-table>
  `
})
export class CollectionSelectorComponent extends EditorPanel {
  items: MenuItem[] = [
    {
      label: 'Loading', icon: 'pi-spinner',
    }
  ];
  selectedDisplayObjectName: string | null = null;
  gwtAngularPropertyTable: GwtAngularPropertyTable | null = null;

  constructor(private gwtAngularService: GwtAngularService, private messageService: MessageService) {
    super();
  }

  onEditorModel(): void {
    try {
      this.requestObjectNameId();
    } catch (error) {
      this.messageService.add({
        severity: 'error',
        summary: `Can not read ObjectNameId: ${(<GenericPropertyEditorModel>this.editorModel).collectionName}`,
        detail: "" + error,
        sticky: true
      });
      console.error(error);
    }
  }


  setupMenuItems(objectNameIds: ObjectNameId[]) {
    let menuObjectNameIds: MenuItem[] = [];

    objectNameIds.forEach(objectNameId => {
      const displayObjectName = `${objectNameId.internalName} (${objectNameId.id})`;
      menuObjectNameIds.push({
        label: displayObjectName,
        command: () => {
          this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getGenericEditorFrontendProvider()
            .readConfig((<GenericPropertyEditorModel>this.editorModel).collectionName, objectNameId.id)
            .then(value => {
                this.gwtAngularPropertyTable = value;
                this.updateDeleteSaveDisableState();
                this.items[0].label = displayObjectName;
                this.selectedDisplayObjectName = displayObjectName;
              },
              reason => {
                this.messageService.add({
                  severity: 'error',
                  summary: `Can not load config for: ${(<GenericPropertyEditorModel>this.editorModel).collectionName} with id: ${objectNameId.id}`,
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
        label: this.selectedDisplayObjectName != null ? this.selectedDisplayObjectName : 'Select...',
        items: menuObjectNameIds,
      },
      {
        label: "New",
        command: () => {
          this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getGenericEditorFrontendProvider()
            .createConfig((<GenericPropertyEditorModel>this.editorModel).collectionName).then(
            value => {
              this.gwtAngularPropertyTable = value;
              this.selectedDisplayObjectName = `? (${value.configId})`;
              this.requestObjectNameId();
            },
            reason => {
              this.messageService.add({
                severity: 'error',
                summary: `Can not create config for: ${(<GenericPropertyEditorModel>this.editorModel).collectionName}`,
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
            .updateConfig((<GenericPropertyEditorModel>this.editorModel).collectionName, this.gwtAngularPropertyTable!).then(
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
                summary: `Can not save config for: ${(<GenericPropertyEditorModel>this.editorModel).collectionName}`,
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
            .deleteConfig((<GenericPropertyEditorModel>this.editorModel).collectionName, this.gwtAngularPropertyTable!).then(
            () => {
              this.gwtAngularPropertyTable = null;
              this.selectedDisplayObjectName = null;
              this.requestObjectNameId();
              this.messageService.add({
                severity: 'success',
                summary: 'Deleted'
              });
            },
            reason => {
              this.messageService.add({
                severity: 'error',
                summary: `Can not delete config for: ${(<GenericPropertyEditorModel>this.editorModel).collectionName}`,
                detail: reason,
                sticky: true
              });
              console.error(reason);
            });
        }
      }
    ];
  }

  private requestObjectNameId(): void {
    this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getGenericEditorFrontendProvider()
      .requestObjectNameIds((<GenericPropertyEditorModel>this.editorModel).collectionName)
      .then(value => this.setupMenuItems(value),
        reason => {
          this.messageService.add({
            severity: 'error',
            summary: `Can not load configs for: ${(<GenericPropertyEditorModel>this.editorModel).collectionName}`,
            detail: reason,
            sticky: true
          });
          console.error(reason);
        });
  }

  private updateDeleteSaveDisableState() {
    this.items[2].disabled = this.gwtAngularPropertyTable == null;
    this.items[3].disabled = this.gwtAngularPropertyTable == null;
    this.items = [...this.items];
  }

}
