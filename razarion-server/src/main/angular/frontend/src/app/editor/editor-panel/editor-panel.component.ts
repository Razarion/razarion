import {Component, Input, OnInit} from '@angular/core';
import {EditorModel} from "../editor-model";
import {MenuItem, MessageService} from "primeng/api";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {ObjectNameId} from "../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'app-editor-panel',
  templateUrl: './editor-panel.component.html',
  styleUrls: ['./editor-panel.component.scss']
})
export class EditorPanelComponent implements OnInit {
  @Input("editorModel")
  editorModel!: EditorModel;
  items: MenuItem[] = [];

  constructor(private gwtAngularService: GwtAngularService, private messageService: MessageService) {
  }

  ngOnInit(): void {
    this.items = [
      {
        label: 'Loading', icon: 'pi-spinner',
      }
    ];

    this.gwtAngularService.gwtAngularFacade.editorFrontendProvider
      .requestConfigs(this.editorModel.crudControllerIndex)
      .then(value => {
        this.setupMenuItems(value);
      }, reason => {
        this.messageService.add({
          severity: 'error',
          summary: 'Can not load config for: ' + this.editorModel.crudControllerName,
          detail: reason
        });
        console.error(reason);
      });
  }

  private setupMenuItems(objectNameIds: ObjectNameId[]) {
    let menuObjectNameIds: MenuItem[] = [];

    console.error(objectNameIds)

    objectNameIds.forEach(objectNameId => {
      menuObjectNameIds.push({label: `${objectNameId.getInternalName()} (${objectNameId.getId()})`,});
    })

    this.items = [
      {
        label: 'Select...',
        items: menuObjectNameIds,
      },
      {label: "New"},
      {label: "Save"},
      {label: "Delete"},
      {label: "Close"},
    ];
  }
}
