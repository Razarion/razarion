import {Component} from '@angular/core';
import {AngularTreeNodeData, GwtAngularPropertyTable, ObjectNameId} from "../../gwtangular/GwtAngularFacade";
import {MenuItem, MessageService, SortEvent, TreeNode} from "primeng/api";
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
  cols = [
    {field: 'name', header: 'Name'},
    {field: 'value', header: 'Value'}
  ];
  selectedDisplayObjectName: string | null= null;

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

  sortPropertyNames(event: SortEvent) {
    if (event.data == undefined) {
      return;
    }
    event.data.sort((node1: TreeNode, node2: TreeNode) => {
      const data1: AngularTreeNodeData = node1.data
      const data2: AngularTreeNodeData = node2.data

      if (!data1.canHaveChildren && data2.canHaveChildren) {
        return -1;
      } else if (data1.canHaveChildren && !data2.canHaveChildren) {
        return 1;
      }
      return (data1.name < data2.name ? -1 : (data1.name > data2.name ? 1 : 0));
    });
  }

  private updateDeleteSaveDisableState() {
    this.items[2].disabled = this.gwtAngularPropertyTable == null;
    this.items[3].disabled = this.gwtAngularPropertyTable == null;
    this.items = [...this.items];
  }

  private setupMenuItems(objectNameIds: ObjectNameId[]) {
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
}
