import { Component, OnInit } from '@angular/core';
import { EditorPanel } from '../editor-model';
import { PlayerBaseDto } from 'src/app/gwtangular/GwtAngularFacade';
import { GwtAngularService } from 'src/app/gwtangular/GwtAngularService';
import { GwtHelper } from 'src/app/gwtangular/GwtHelper';
import { TypescriptGenerator } from 'src/app/backend/typescript-generator';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { MessageService } from "primeng/api";
import {Button} from 'primeng/button';
import {TableModule} from 'primeng/table';
import {PlanetMgmtControllerClient} from '../../generated/razarion-share';

@Component({
  selector: 'app-base-mgmt',
  imports: [
    Button,
    TableModule,
    FormsModule
  ],
  templateUrl: './base-mgmt.component.html'
})
export class BaseMgmtComponent extends EditorPanel implements OnInit {
  bases: PlayerBaseDto[] = [];
  /** Amount added per "Add money" click (editable in the header). */
  moneyAmount = 100000;
  private planetMgmtControllerClient: PlanetMgmtControllerClient;

  constructor(private gwtAngularService: GwtAngularService,
    private messageService: MessageService,
    private httpClient: HttpClient) {
    super();
    this.planetMgmtControllerClient = new PlanetMgmtControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
  }

  addMoney(base: PlayerBaseDto): void {
    const amount = Math.round(this.moneyAmount);
    this.httpClient.post(`/rest/planet-mgmt-controller/addResources/${base.getBaseId()}?amount=${amount}`, {})
      .subscribe({
        next: () => this.messageService.add({severity: 'success', summary: `Added ${amount} Razarion to ${base.getName()}`}),
        error: (reason: any) => this.messageService.add({severity: 'error', summary: 'Failed to add money', detail: reason, sticky: true}),
      });
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
