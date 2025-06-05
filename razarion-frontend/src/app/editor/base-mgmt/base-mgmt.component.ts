import { Component, OnInit } from '@angular/core';
import { EditorPanel } from '../editor-model';
import { PlayerBaseDto } from 'src/app/gwtangular/GwtAngularFacade';
import { GwtAngularService } from 'src/app/gwtangular/GwtAngularService';
import { GwtHelper } from 'src/app/gwtangular/GwtHelper';
import { TypescriptGenerator } from 'src/app/backend/typescript-generator';
import { HttpClient } from '@angular/common/http';
import { MessageService } from "primeng/api";
import {Button} from 'primeng/button';
import {TableModule} from 'primeng/table';
import {PlanetMgmtControllerClient} from '../../generated/razarion-share';

@Component({
  selector: 'app-base-mgmt',
  imports: [
    Button,
    TableModule
  ],
  templateUrl: './base-mgmt.component.html'
})
export class BaseMgmtComponent extends EditorPanel implements OnInit {
  bases: PlayerBaseDto[] = [];
  private planetMgmtControllerClient: PlanetMgmtControllerClient;

  constructor(private gwtAngularService: GwtAngularService,
    private messageService: MessageService,
    httpClient: HttpClient) {
    super();
    this.planetMgmtControllerClient = new PlanetMgmtControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
  }

  ngOnInit(): void {
    this.loadBases();
  }

  onBaseDelete(base: PlayerBaseDto): void {
    this.planetMgmtControllerClient.deleteBase(base.getBaseId()).then(() => {
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
