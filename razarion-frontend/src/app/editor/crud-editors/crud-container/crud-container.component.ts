import {Component, ComponentFactoryResolver, ViewChild, ViewContainerRef} from '@angular/core';
import {EditorPanel} from "../../editor-model";
import {MenuItem, MessageService} from "primeng/api";
import {Config, ObjectNameId} from "../../../generated/razarion-share";

export interface CrudContainerChild<T extends Config> {
  init(config: T): void;

  exportConfig(): T;

  getId(): number;
}

export interface CrudContainerChildPreUpdate<T extends Config> extends CrudContainerChild<T> {
  postUpdate(): Promise<void>;

  onUpdateSuccess(): void;
}


@Component({
    selector: 'crud-container',
    templateUrl: './crud-container.component.html',
    standalone: false
})
export class AbstractCrudContainerComponent extends EditorPanel {
  @ViewChild("configContainer", {read: ViewContainerRef})
  configContainer!: ViewContainerRef;
  private crudContainerChild: CrudContainerChild<any> | null = null;

  items: MenuItem[] = [
    {
      label: 'Loading', icon: 'pi-spinner',
    }
  ];
  selectedDisplayObjectName: string | null = null;

  initCustom(childComponent: any) {
    throw new Error('Method not implemented.');
  }

  requestObjectNameId(): Promise<ObjectNameId[]> {
    throw new Error('Method not implemented.');
  }

  read(id: number): Promise<Config> {
    throw new Error('Method not implemented.');
  }

  create(): Promise<Config> {
    throw new Error('Method not implemented.');
  }

  update(config: Config): Promise<void> {
    throw new Error('Method not implemented.');
  }

  delete(id: number): Promise<void> {
    throw new Error('Method not implemented.');
  }

  getEditorName(): string {
    throw new Error('Method not implemented.');
  }

  constructor(protected messageService: MessageService,
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
    this.initCustom(this.editorModel.childComponent);
    this.internalRequestObjectNameIds();
  }

  private setupMenuItems(objectNameIds: ObjectNameId[]) {
    let menuObjectNameIds: MenuItem[] = [];

    objectNameIds.forEach(objectNameId => {
      const displayObjectName = `${objectNameId.internalName} (${objectNameId.id})`;
      menuObjectNameIds.push({
        label: displayObjectName,
        command: () => {
          this.read(objectNameId.id).then(configObject => {
            this.displayConfigComponent(configObject);
            this.items[0].label = displayObjectName;
            this.selectedDisplayObjectName = displayObjectName;
          }).catch(error => {
            console.error(error);
            this.messageService.add({
              severity: 'error',
              summary: `Can not load config '${displayObjectName}' for: '${this.getEditorName}'`,
              detail: error.message,
              sticky: true
            });
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
          this.create().then(configObject => {
            this.displayConfigComponent(configObject);
            this.items[0].label = configObject.internalName;
            this.selectedDisplayObjectName = `? '${configObject.id}'`;
            this.internalRequestObjectNameIds();
          }).catch(error => {
            console.error(error);
            this.messageService.add({
              severity: 'error',
              // summary: `Can not create config for: ${(<GenericPropertyEditorModel>this.editorModel).collectionName}`,
              detail: error,
              sticky: true
            });
          });
        }
      },
      {
        label: "Save",
        disabled: !this.crudContainerChild,
        command: () => {
          let config = this.crudContainerChild!.exportConfig();
          this.update(config).then(() => {
            if ((<CrudContainerChildPreUpdate<any>>this.crudContainerChild).postUpdate) {
              (<CrudContainerChildPreUpdate<any>>this.crudContainerChild).postUpdate().then(() => {
                this.messageService.add({
                  severity: 'success',
                  life: 300,
                  summary: 'Saved'
                });
                this.internalRequestObjectNameIds();
                (<CrudContainerChildPreUpdate<any>>this.crudContainerChild).onUpdateSuccess();
              })
                .catch(error => {
                  this.messageService.add({
                    severity: 'error',
                    summary: `postUpdate failed: ${this.getEditorName}`,
                    detail: error.message,
                    sticky: true
                  });
                  console.error(error);
                })
            } else {
              this.messageService.add({
                severity: 'success',
                life: 300,
                summary: 'Saved'
              });
              this.internalRequestObjectNameIds();
            }
          }).catch(error => {
            this.messageService.add({
              severity: 'error',
              summary: `Error loading objectNameIds for: ${this.getEditorName}`,
              detail: error.message,
              sticky: true
            });
            console.error(error);
          });
        }
      },
      {
        label: "Delete",
        disabled: !this.crudContainerChild,
        command: () => {
          this.delete(this.crudContainerChild!.getId()).then(() => {
            this.internalRequestObjectNameIds();
            this.displayConfigComponent(null);
            this.items[0].label = 'Select...';
            this.selectedDisplayObjectName = null;
            this.messageService.add({
              severity: 'success',
              life: 300,
              summary: 'Deleted'
            });
          }).catch(error => {
            this.messageService.add({
              severity: 'error',
              summary: `Failed saving ${this.crudContainerChild!.getId()} ${this.getEditorName()}`,
              detail: `${JSON.stringify(error)}`,
              sticky: true
            });
          });
        }
      }
    ];
  }

  private internalRequestObjectNameIds() {
    this.requestObjectNameId().then(objectNameIds => {
      this.setupMenuItems(objectNameIds);
    }).catch(error => {
      console.error(error);
      this.messageService.add({
        severity: 'error',
        summary: `Can not load objectNameIds for: ${this.getEditorName()}`,
        detail: error.message,
        sticky: true
      });
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
            summary: `Can not open editor: ${this.getEditorName()}`,
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
