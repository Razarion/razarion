import {EditorPanel, GenericPropertyEditorModel} from "../editor-model";
import {Component, ElementRef, ViewChild} from "@angular/core";
import {MenuItem, MessageService} from "primeng/api";
import {ObjectNameId} from "../../gwtangular/GwtAngularFacade";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {APPLICATION_PATH} from "../../common";
import {HttpClient} from "@angular/common/http";
import {
  JSONEditor,
  JSONSelection,
  ReadonlyValue,
  renderJSONSchemaEnum,
  renderValue,
  RenderValueProps
} from "vanilla-jsoneditor";

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
              private httpClient: HttpClient) {
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
          this.httpClient.get(`${this.url4Collection()}/read/${objectNameId.id}`)
            .subscribe((jsonObject: any) => {
              this.initJsonEditor();
              this.jsonEditor.set({json: jsonObject});
              this.jsonObject = jsonObject;
              this.updateDeleteSaveDisableState();
              this.items[0].label = displayObjectName;
              this.selectedDisplayObjectName = displayObjectName;
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
          this.httpClient.post(`${this.url4Collection()}/create`, {})
            .subscribe({
              next: (jsonObject: any) => {
                this.initJsonEditor();
                this.jsonEditor.set({json: jsonObject});
                this.jsonObject = jsonObject;
                this.updateDeleteSaveDisableState();
                this.items[0].label = jsonObject.internalName;
                this.selectedDisplayObjectName = `? '${jsonObject.id}'`;
                this.requestObjectNameId();
              }, error: (error) => {
                console.error(error);
                this.messageService.add({
                  severity: 'error',
                  summary: `Can not create config for: ${(<GenericPropertyEditorModel>this.editorModel).collectionName}`,
                  detail: error,
                  sticky: true
                });
              }
            });
        }
      },
      {
        label: "Save",
        disabled: this.jsonObject == null,
        command: () => {
          console.log(this.jsonEditor.get())
          this.httpClient.post(`${this.url4Collection()}/update`, (<any>this.jsonEditor.get()).json || JSON.parse((<any>this.jsonEditor.get()).text))
            .subscribe(() => {
              this.requestObjectNameId();
              this.messageService.add({
                severity: 'success',
                life: 300,
                summary: 'Saved'
              });
            });
        }
      },
      {
        label: "Delete",
        disabled: this.jsonObject == null,
        command: () => {
          this.httpClient.delete(`${this.url4Collection()}/delete/${this.jsonObject.id}`)
            .subscribe(() => {
              this.requestObjectNameId();
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
    const schema = {
      properties: {
        meshContainerId: {
          type: 'number',
          // enum: [1, 2, 3]
        }
      },
      required: ["meshContainerId"]
    }


    if (!this.jsonEditor) {
      this.jsonEditor = new JSONEditor({
        target: this.jsonEditorContainer.nativeElement,
        props: {
          onRenderValue: (renderValueProps: RenderValueProps) => {

            return renderJSONSchemaEnum(renderValueProps, schema, {}) || renderValue(renderValueProps);

            let valueReadOnly = {
              value: "Vehicle_00 '12'",
                path: renderValueProps.path,
                selection: renderValueProps.selection,
                parser: renderValueProps.parser,
                readOnly: renderValueProps.readOnly,
                onPatch: renderValueProps.onPatch,
                enforceString: renderValueProps.enforceString,
                searchResultItems: renderValueProps.enforceString,
                isEditing: renderValueProps.isEditing,
                normalization: renderValueProps.normalization,
                onPasteJson: renderValueProps.onPasteJson,
                onSelect: renderValueProps.onSelect,
                onFind: renderValueProps.onFind,
                findNextInside: renderValueProps.findNextInside,
                focus: renderValueProps.focus,
            }

            return [{
              component: <any>ReadonlyValue, // TODO: casting should not be needed
              // props: <any>renderValueProps
              props: valueReadOnly
            },{
              component: <any>ReadonlyValue, // TODO: casting should not be needed
              // props: <any>renderValueProps
              props: {
                value: "⚒",
                path: renderValueProps.path,
                selection: renderValueProps.selection,
                parser: renderValueProps.parser,
                readOnly: renderValueProps.readOnly,
                onPatch: renderValueProps.onPatch,
                enforceString: renderValueProps.enforceString,
                searchResultItems: renderValueProps.enforceString,
                isEditing: renderValueProps.isEditing,
                normalization: renderValueProps.normalization,
                onPasteJson: renderValueProps.onPasteJson,
                onSelect: (selection: JSONSelection) =>{valueReadOnly.value = "12";console.error(`onSelect ${selection}`)},
                onFind: renderValueProps.onFind,
                findNextInside: renderValueProps.findNextInside,
                focus: renderValueProps.focus,
              }
            },{
              component: <any>ReadonlyValue, // TODO: casting should not be needed
              // props: <any>renderValueProps
              props: {
                value: "☒",
                path: renderValueProps.path,
                selection: renderValueProps.selection,
                parser: renderValueProps.parser,
                readOnly: renderValueProps.readOnly,
                onPatch: renderValueProps.onPatch,
                enforceString: renderValueProps.enforceString,
                searchResultItems: renderValueProps.enforceString,
                isEditing: renderValueProps.isEditing,
                normalization: renderValueProps.normalization,
                onPasteJson: renderValueProps.onPasteJson,
                onSelect: (selection: JSONSelection) =>{console.error(`onSelect ${selection}`)},
                onFind: renderValueProps.onFind,
                findNextInside: renderValueProps.findNextInside,
                focus: renderValueProps.focus,
              }
            }];
          },
          // onChange: (updatedContent, previousContent, {contentErrors, patchResult}) => {
          //   // content is an object { json: JSONValue } | { text: string }
          //   console.log('onChange', {updatedContent, previousContent, contentErrors, patchResult})
          //   // content = updatedContent
          // },
          // validator: createAjvValidator({ schema })
        }
      });
    }
  }

  private url4Collection(): string {
    return `${APPLICATION_PATH}/${this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getGenericEditorFrontendProvider().getPathForCollection((<GenericPropertyEditorModel>this.editorModel).collectionName)}`
  }
}
