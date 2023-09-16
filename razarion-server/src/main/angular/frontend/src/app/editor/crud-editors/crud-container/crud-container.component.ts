import {Component, ComponentFactoryResolver, ViewChild, ViewContainerRef} from '@angular/core';
import {EditorPanel} from "../../editor-model";
import {MenuItem, MessageService} from "primeng/api";
import {HttpClient} from "@angular/common/http";
import {ObjectNameId} from "../../../generated/razarion-share";

export interface CrudContainerChild<T> {
  init(config: T): void;

  exportConfig(): T;

  getId(): number;
}

@Component({
  selector: 'crud-container',
  templateUrl: './crud-container.component.html'
})
export class CrudContainerComponent extends EditorPanel {
  @ViewChild("configContainer", {read: ViewContainerRef})
  configContainer!: ViewContainerRef;
  editorUrl?: string;
  private crudContainerChild: CrudContainerChild<any> | null = null;

  items: MenuItem[] = [
    {
      label: 'Loading', icon: 'pi-spinner',
    }
  ];
  selectedDisplayObjectName: string | null = null;

  constructor(private messageService: MessageService,
              private httpClient: HttpClient,
              private resolver: ComponentFactoryResolver) {
    super();
  }

  onEditorModel(): void {
    if (!this.editorModel.childComponent) {
      console.error(`No config component : ${this.editorModel.editorComponent}`);
      this.messageService.add({
        severity: 'error',
        summary: `No config component : ${this.editorModel.editorComponent}`,
        sticky: true
      });
      return;
    }
    this.editorUrl = (<any>this.editorModel.childComponent).editorUrl;
    if (!this.editorUrl) {
      console.error(`No editorUrl in ${this.editorModel.childComponent}`);
      this.messageService.add({
        severity: 'error',
        summary: `No editorUrl in ${this.editorModel.childComponent}`,
        sticky: true
      });
      return;
    }
    this.requestObjectNameId();
  }

  setupMenuItems(objectNameIds: ObjectNameId[]) {
    let menuObjectNameIds: MenuItem[] = [];

    objectNameIds.forEach(objectNameId => {
      const displayObjectName = `${objectNameId.internalName} (${objectNameId.id})`;
      menuObjectNameIds.push({
        label: displayObjectName,
        command: () => {
          this.httpClient.get(`${this.editorUrl}/read/${objectNameId.id}`)
            .subscribe({
              next: (configObject: any) => {
                this.displayConfigComponent(configObject);
                this.items[0].label = displayObjectName;
                this.selectedDisplayObjectName = displayObjectName;
              },
              error: (error) => {
                console.error(error);
                this.messageService.add({
                  severity: 'error',
                  summary: `Can not load config '${displayObjectName}' for: '${this.editorUrl}'`,
                  detail: error.message,
                  sticky: true
                });
              }
            });
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
          this.httpClient.post(`${this.editorUrl}/create`, {})
            .subscribe({
              next: (configObject: any) => {
                this.displayConfigComponent(configObject);
                this.items[0].label = configObject.internalName;
                this.selectedDisplayObjectName = `? '${configObject.id}'`;
                this.requestObjectNameId();
              }, error: (error) => {
                console.error(error);
                this.messageService.add({
                  severity: 'error',
                  // summary: `Can not create config for: ${(<GenericPropertyEditorModel>this.editorModel).collectionName}`,
                  detail: error,
                  sticky: true
                });
              }
            });
        }
      },
      {
        label: "Save",
        disabled: !this.crudContainerChild,
        command: () => {
          this.httpClient.post(`${this.editorUrl}/update`, this.crudContainerChild!.exportConfig()).subscribe({
            next: (objectNameIds: any) => {
              this.messageService.add({
                severity: 'success',
                life: 300,
                summary: 'Saved'
              });
              this.setupMenuItems(objectNameIds);
            },
            error: (error: any) => {
              this.messageService.add({
                severity: 'error',
                summary: `Error loading objectNameIds for: ${this.editorUrl}`,
                detail: error.message,
                sticky: true
              });
              console.error(error);
            }
          });
        }
      },
      {
        label: "Delete",
        disabled: !this.crudContainerChild,
        command: () => {
          this.httpClient.delete(`${this.editorUrl}/delete/${this.crudContainerChild?.getId()}`)
            .subscribe(() => {
              this.requestObjectNameId();
              this.displayConfigComponent(null);
              this.items[0].label = 'Select...';
              this.selectedDisplayObjectName = null;
              this.messageService.add({
                severity: 'success',
                life: 300,
                summary: 'Deleted'
              });
            });
        }
      }
    ];
  }

  private requestObjectNameId(): void {
    this.httpClient.get(`${this.editorUrl}/objectNameIds`).subscribe({
      next: (objectNameIds: any) => {
        this.setupMenuItems(objectNameIds);
      },
      error: (error: any) => {
        this.messageService.add({
          severity: 'error',
          summary: `Error loading objectNameIds for: ${this.editorUrl}`,
          detail: error.message,
          sticky: true
        });
        console.error(error);
      }
    });
  }

  private updateDeleteSaveDisableState() {
    this.items[2].disabled = !this.crudContainerChild;
    this.items[3].disabled = !this.crudContainerChild;
    this.items = [...this.items];
  }

  private displayConfigComponent(configObject: any) {
    if (configObject) {
      // Promise solves Angular problem
      // Uncaught Error: NG0100: ExpressionChangedAfterItHasBeenCheckedError: Expression has changed after it was checked. Previous value: 'undefined'. Current value: '[object Object]'. It seems like the view has been created after its parent and its children have been dirty checked. Has it been created in a change detection hook?. Find more at https://angular.io/errors/NG0100
      Promise.resolve().then(() => {
        try {
          this.configContainer.clear();
          const factory = this.resolver.resolveComponentFactory(this.editorModel.childComponent!);
          let componentRef = this.configContainer.createComponent(factory);
          this.crudContainerChild = componentRef.instance;
          this.crudContainerChild!.init(configObject);
          this.updateDeleteSaveDisableState();
        } catch (error) {
          this.messageService.add({
            severity: 'error',
            summary: `Can not open editor: ${this.editorUrl}`,
            detail: String(error),
            sticky: true
          });
          console.error(error);
          throw error;
        }
      });
    } else {
      this.configContainer.clear();
      this.crudContainerChild = null;
      this.updateDeleteSaveDisableState();
    }
  }
}
