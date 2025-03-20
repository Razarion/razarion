import { Component, OnInit } from '@angular/core';
import { EditorPanel } from '../editor-model';
import { PlayerBaseDto } from 'src/app/gwtangular/GwtAngularFacade';
import { GwtAngularService } from 'src/app/gwtangular/GwtAngularService';
import { GwtHelper } from 'src/app/gwtangular/GwtHelper';
import { ServerGameEngineControllerClient } from 'src/app/generated/razarion-share';
import { TypescriptGenerator } from 'src/app/backend/typescript-generator';
import { HttpClient } from '@angular/common/http';
import { MessageService } from "primeng/api";

@Component({
    selector: 'app-base-mgmt',
    templateUrl: './base-mgmt.component.html'
})
export class BaseMgmtComponent extends EditorPanel implements OnInit {
  bases: PlayerBaseDto[] = [];
  private serverGameEngineControllerClient: ServerGameEngineControllerClient;

  constructor(private gwtAngularService: GwtAngularService,
    private messageService: MessageService,
    httpClient: HttpClient) {
    super();
    this.serverGameEngineControllerClient = new ServerGameEngineControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
  }

  ngOnInit(): void {
    this.loadBases();
  }

  onBaseDelete(base: PlayerBaseDto): void {
    this.serverGameEngineControllerClient.deleteBase(base.getBaseId()).then(() => {
      setTimeout(() => {
        this.loadBases();
      }, 1000);
      this.messageService.add({
        severity: 'success',
        summary: `Base removed`,
      });
    }).catch((reason: any) => {
      this.messageService.add({
        severity: 'error',
        summary: `Failed to remove base`,
        detail: reason,
        sticky: true
      });
    });
  }

  loadBases():void{
    this.bases = GwtHelper.gwtIssueArray(this.gwtAngularService.gwtAngularFacade.baseItemUiService.getBases());
  }

}
