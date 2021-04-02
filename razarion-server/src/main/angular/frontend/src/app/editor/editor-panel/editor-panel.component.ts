import {Component, Input, OnInit} from '@angular/core';
import {EditorModel} from "../editor-model";
import {MenuItem, MessageService} from "primeng/api";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {GwtAngularPropertyTable, ObjectNameId} from "../../gwtangular/GwtAngularFacade";
import {GameComponent} from "../../game/game.component";

@Component({
  selector: 'app-editor-panel',
  templateUrl: './editor-panel.component.html',
  styleUrls: ['./editor-panel.component.scss']
})
export class EditorPanelComponent implements OnInit {
  @Input("editorModel")
  editorModel!: EditorModel;
  items: MenuItem[] = [];
  gwtAngularPropertyTable?: GwtAngularPropertyTable;

  constructor(private gwtAngularService: GwtAngularService, private messageService: MessageService, private gameComponent: GameComponent) {
  }

  ngOnInit(): void {
    this.items = [
      {
        label: 'Loading', icon: 'pi-spinner',
      }
    ];

    this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getGenericEditorFrontendProvider()
      .requestConfigs(this.editorModel.crudControllerIndex)
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
    this.gameComponent.removeEditorPanel(this.editorModel)
  }

  private setupMenuItems(objectNameIds: ObjectNameId[]) {
    let menuObjectNameIds: MenuItem[] = [];

    objectNameIds.forEach(objectNameId => {
      menuObjectNameIds.push({
        label: `${objectNameId.getInternalName()} (${objectNameId.getId()})`,
        command: () => {
          this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getGenericEditorFrontendProvider()
            .readConfig(this.editorModel.crudControllerIndex, objectNameId.getId())
            .then(value => this.gwtAngularPropertyTable = value,
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
      {label: "New"},
      {
        label: "Save",
        command: () => {
          this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getGenericEditorFrontendProvider()
            .updateConfig(this.editorModel.crudControllerIndex, this.gwtAngularPropertyTable!).then(
            () => {
              this.messageService.add({
                severity: 'success',
                summary:'Saved'
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
      {label: "Delete"}
    ];
  }
}
