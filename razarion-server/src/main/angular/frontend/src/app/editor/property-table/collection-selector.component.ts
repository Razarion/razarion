import {EditorPanel, GenericPropertyEditorModel} from "../editor-model";
import {Component, ElementRef, ViewChild} from "@angular/core";
import {MenuItem, MessageService} from "primeng/api";
import {ObjectNameId} from "../../gwtangular/GwtAngularFacade";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {APPLICATION_PATH, EDITOR_PATH} from "../../common";
import {HttpClient} from "@angular/common/http";
import {JSONEditor} from "vanilla-jsoneditor";

@Component({
  selector: 'collection-selector',
  template: `
    <p-menubar class="mb-0" [model]="items"></p-menubar>
    <div #jsonEditorContainer class="jse-theme-dark"></div>
  `
})
export class CollectionSelectorComponent extends EditorPanel {
  items: MenuItem[] = [
    {
      label: 'Loading', icon: 'pi-spinner',
    }
  ];
  selectedDisplayObjectName: string | null = null;
  jsonObject: any;
  @ViewChild('jsonEditorContainer', {static: true})
  jsonEditorContainer!: ElementRef;
  jsonEditor!: JSONEditor;


  constructor(private gwtAngularService: GwtAngularService,
              private messageService: MessageService,
              private http: HttpClient) {
    super();
  }

  onEditorModel(): void {
    try {
      this.requestObjectNameId();
    } catch (error) {
      this.messageService.add({
        severity: 'error',
        summary: `Can not read ObjectNameId: ${(<GenericPropertyEditorModel>this.editorModel).collectionName}`,
        detail: String(error),
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
          this.http.get(`${this.url4Collection()}/read/${objectNameId.id}`)
            .subscribe((jsonObject: any) => {
              this.initJsonEditor();
              this.jsonEditor.set({json: jsonObject});
              this.jsonObject = jsonObject;
              this.updateDeleteSaveDisableState();
              this.items[0].label = displayObjectName;
              this.selectedDisplayObjectName = displayObjectName;
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
              this.jsonObject = value;
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
        disabled: this.jsonObject == null,
        command: () => {
          this.http.post(`${this.url4Collection()}/update`,(<any>this.jsonEditor.get()).json)
            .subscribe(() => {
              this.requestObjectNameId();
              this.messageService.add({
                severity: 'success',
                summary: 'Saved'
              });
            });
        }
      },
      {
        label: "Delete",
        disabled: this.jsonObject == null,
        command: () => {
          this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getGenericEditorFrontendProvider()
            .deleteConfig((<GenericPropertyEditorModel>this.editorModel).collectionName, this.jsonObject!).then(
            () => {
              this.jsonObject = null;
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
    this.items[2].disabled = this.jsonObject == null;
    this.items[3].disabled = this.jsonObject == null;
    this.items = [...this.items];
  }

  private initJsonEditor() {
    if (!this.jsonEditor) {
      this.jsonEditor = new JSONEditor({
        target: this.jsonEditorContainer.nativeElement,
        props: {
          onChange: (updatedContent, previousContent, {contentErrors, patchResult}) => {
            // content is an object { json: JSONValue } | { text: string }
            console.log('onChange', {updatedContent, previousContent, contentErrors, patchResult})
            // content = updatedContent
          }
        }
      })
    }
  }

  private url4Collection(): string {
    return `${APPLICATION_PATH}/${this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getGenericEditorFrontendProvider().getPathForCollection((<GenericPropertyEditorModel>this.editorModel).collectionName)}`
  }
}
