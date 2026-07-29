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
  /**
   * Item count per base id, taken once per load. Counting walks every item on the planet, so
   * doing it from the template would repeat that scan for every row on every change detection
   * run - and the rest of the table is a snapshot of the refresh moment anyway.
   */
  private itemCounts = new Map<number, number>();
  /**
   * Level number per base id, fetched from the server. Unlike everything else in this table it
   * cannot come from the client: a client knows its own level and nothing about anybody else's.
   * Arrives after the rows are already on screen, which is why the column reads from a map
   * instead of from the base.
   */
  private levels: { [baseId: string]: number } = {};
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
    const baseItemUiService = this.gwtAngularService.gwtAngularFacade.baseItemUiService;
    const bases: PlayerBaseDto[] = GwtHelper.gwtIssueArray(baseItemUiService.getBases());
    this.itemCounts = new Map(bases.map(base => [base.getBaseId(), baseItemUiService.getBaseItemCount(base.getBaseId())]));
    // Oldest first, because the reason to open this table is finding bases nobody came back to.
    // No sortable column header: the rows are JS proxies carrying getter methods, not plain
    // objects with fields, so PrimeNG's field-path sorting has nothing to read.
    // A base with no timestamp predates the feature and is therefore older than any base that
    // has one, which is exactly where sorting 0 first puts it.
    this.bases = bases.sort((a, b) => a.getCreatedMillis() - b.getCreatedMillis());
    this.planetMgmtControllerClient.baseLevels()
      .then(levels => this.levels = levels)
      .catch((reason: any) => this.messageService.add({
        severity: 'error',
        summary: 'Failed to load base levels',
        detail: reason
      }));
  }

  /** Level of the base, or a dash for bots and for anything the server did not report a level for. */
  level(base: PlayerBaseDto): string {
    const level = this.levels[base.getBaseId()];
    return level !== undefined ? String(level) : '—';
  }

  /** Items the base owns as of the last load, buildings included. */
  itemCount(base: PlayerBaseDto): number {
    return this.itemCounts.get(base.getBaseId()) ?? 0;
  }

  /**
   * How long the base has been standing, as something readable at a glance - the point of the
   * column is spotting the ones nobody has come back to, not knowing the exact minute.
   *
   * Ages are computed against the browser clock while the timestamp comes from the server, so a
   * skewed client is off by that skew. Irrelevant at the resolution shown here.
   */
  age(base: PlayerBaseDto): string {
    const created = base.getCreatedMillis();
    if (!created) {
      // Bases that were already standing before the timestamp existed. Saying "unknown" is
      // honest; showing them as brand new or as ancient would both be made up.
      return '—';
    }
    const minutes = Math.floor((Date.now() - created) / 60000);
    if (minutes < 1) {
      return 'just now';
    }
    if (minutes < 60) {
      return `${minutes} min`;
    }
    const hours = Math.floor(minutes / 60);
    if (hours < 24) {
      return `${hours} h ${minutes % 60} min`;
    }
    return `${Math.floor(hours / 24)} d ${hours % 24} h`;
  }

  /** Exact timestamp for the column's tooltip, where the rounding above is not good enough. */
  createdAt(base: PlayerBaseDto): string {
    const created = base.getCreatedMillis();
    return created ? new Date(created).toLocaleString() : 'Created before base ages were recorded';
  }

}
