import { Component, OnInit } from '@angular/core';
import { EditorPanel } from '../editor-model';
import { Character, PlayerBaseDto } from 'src/app/gwtangular/GwtAngularFacade';
import { GwtAngularService } from 'src/app/gwtangular/GwtAngularService';
import { GwtHelper } from 'src/app/gwtangular/GwtHelper';
import { TypescriptGenerator } from 'src/app/backend/typescript-generator';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { MessageService } from "primeng/api";
import {Button} from 'primeng/button';
import {TableModule} from 'primeng/table';
import {Select} from 'primeng/select';
import {PlanetMgmtControllerClient} from '../../generated/razarion-share';

/**
 * One table row. The bases themselves are JS proxies carrying getter methods, not plain objects
 * with fields, so PrimeNG's field-path sorting and filtering have nothing to read on them. Every
 * value the table sorts, filters or shows therefore lives here as a plain field, taken once per
 * load - which is also what the rest of the table already was: a snapshot of the refresh moment.
 */
interface BaseRow {
  /** Kept for the actions (add money, delete), which still talk in bases. */
  base: PlayerBaseDto;
  name: string;
  character: Character;
  /** Items the base owns, buildings included. */
  units: number;
  /** Sortable level; null for bots and for anything the server did not report a level for. */
  level: number | null;
  levelText: string;
  baseId: number;
  /** Sortable age: epoch millis, 0 for bases that predate the timestamp. */
  createdMillis: number;
  age: string;
  createdAt: string;
}

@Component({
  selector: 'app-base-mgmt',
  imports: [
    Button,
    TableModule,
    Select,
    FormsModule
  ],
  templateUrl: './base-mgmt.component.html'
})
export class BaseMgmtComponent extends EditorPanel implements OnInit {
  /** All rows of the last load; `rows` is this list minus the character filter. */
  private allRows: BaseRow[] = [];
  rows: BaseRow[] = [];
  characterFilter: Character | null = null;
  readonly characterOptions = [
    {label: 'Human', value: Character.HUMAN},
    {label: 'Bot', value: Character.BOT},
    {label: 'Bot NCP', value: Character.BOT_NCP},
  ];
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
    // Counting walks every item on the planet, so it happens once per base per load instead of
    // from the template, which would repeat that scan on every change detection run.
    this.allRows = bases.map(base => this.toRow(base, baseItemUiService.getBaseItemCount(base.getBaseId())));
    this.applyCharacterFilter();
    // Levels are the one column that cannot come from the client: a client knows its own level and
    // nothing about anybody else's. They arrive after the rows are already on screen, so the rows
    // get patched instead of built with them.
    this.planetMgmtControllerClient.baseLevels()
      .then(levels => this.applyLevels(levels))
      .catch((reason: any) => this.messageService.add({
        severity: 'error',
        summary: 'Failed to load base levels',
        detail: reason
      }));
  }

  onCharacterFilterChange(): void {
    this.applyCharacterFilter();
  }

  private applyCharacterFilter(): void {
    this.rows = this.characterFilter === null
      ? [...this.allRows]
      : this.allRows.filter(row => row.character === this.characterFilter);
  }

  private applyLevels(levels: { [baseId: string]: number }): void {
    for (const row of this.allRows) {
      const level = levels[row.baseId];
      row.level = level !== undefined ? level : null;
      row.levelText = level !== undefined ? String(level) : '—';
    }
  }

  private toRow(base: PlayerBaseDto, units: number): BaseRow {
    const createdMillis = base.getCreatedMillis();
    return {
      base,
      name: base.getName(),
      character: base.getCharacter(),
      units,
      level: null,
      levelText: '—',
      baseId: base.getBaseId(),
      createdMillis,
      age: BaseMgmtComponent.age(createdMillis),
      createdAt: createdMillis ? new Date(createdMillis).toLocaleString() : 'Created before base ages were recorded',
    };
  }

  /**
   * How long the base has been standing, as something readable at a glance - the point of the
   * column is spotting the ones nobody has come back to, not knowing the exact minute. The exact
   * timestamp is in the column's tooltip, where the rounding is not good enough.
   *
   * Ages are computed against the browser clock while the timestamp comes from the server, so a
   * skewed client is off by that skew. Irrelevant at the resolution shown here.
   */
  private static age(createdMillis: number): string {
    if (!createdMillis) {
      // Bases that were already standing before the timestamp existed. Saying "unknown" is
      // honest; showing them as brand new or as ancient would both be made up.
      return '—';
    }
    const minutes = Math.floor((Date.now() - createdMillis) / 60000);
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

}
