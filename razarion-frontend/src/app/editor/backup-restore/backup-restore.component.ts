import {Component, OnInit} from "@angular/core";
import {BackupPlanetOverview} from "./backup-restore.dto";
import {DatePipe} from "@angular/common";
import {EditorPanel} from "../editor-model";
import {MessageService} from "primeng/api";
import {URL_PLANET_MGMT} from "../../common";
import { HttpClient } from "@angular/common/http";

@Component({
    selector: 'backup-restore',
    templateUrl: './backup-restore.component.html'
})

export class BackupRestoreComponent extends EditorPanel implements OnInit {
  backupPlanetOverviews!: BackupPlanetOverview[];

  constructor(private messageService: MessageService,
              private http: HttpClient) {
    super();
  }

  ngOnInit(): void {
    this.http.get<BackupPlanetOverview[]>(URL_PLANET_MGMT + '/loadallbackupbaseoverviews')
      .subscribe(
        backupBaseOverviews => this.setBackupBaseOverviews(backupBaseOverviews),
        error => {
          this.messageService.add({
            severity: 'error',
            summary: `Error loading backups`,
            detail: `${JSON.stringify(error)}`,
            sticky: true
          });
        });
  }

  onBackup() {
    this.http.post<BackupPlanetOverview[]>(URL_PLANET_MGMT + '/dobackup', null)
      .subscribe(backupPlanetOverviews => {
          this.messageService.add({
            severity: 'success',
            life: 300,
            summary: 'Backup successful'
          });
          this.setBackupBaseOverviews(backupPlanetOverviews);
        },
        error => {
          this.messageService.add({
            severity: 'error',
            summary: `Backup failed`,
            detail: `${JSON.stringify(error)}`,
            sticky: true
          });
        });
  }

  onRestore(backupBaseOverview: BackupPlanetOverview) {
    if(confirm("Restore to '" + new DatePipe("en-US").transform(backupBaseOverview.date, 'dd.MM.yyyy HH:mm:ss') + "'? Current bases/units will be overridden.")) {
      this.http.post<BackupPlanetOverview[]>(URL_PLANET_MGMT + '/dorestore', backupBaseOverview)
        .subscribe(value => {
            this.messageService.add({
              severity: 'success',
              life: 300,
              summary: `Restored to ${new DatePipe("en-US").transform(backupBaseOverview.date, 'dd.MM.yyyy HH:mm:ss')}`
            });
          },
          error => {
            this.messageService.add({
              severity: 'error',
              summary: `Restored to ${new DatePipe("en-US").transform(backupBaseOverview.date, 'dd.MM.yyyy HH:mm:ss')} failed`,
              detail: `${JSON.stringify(error)}`,
              sticky: true
            });
          });
    }
  }

  onDelete(backupBaseOverview: BackupPlanetOverview) {
    if(confirm("Delete backup '" + new DatePipe("en-US").transform(backupBaseOverview.date, 'dd.MM.yyyy HH:mm:ss'))) {
      this.http.delete<BackupPlanetOverview[]>(URL_PLANET_MGMT + '/deletebackup/' + backupBaseOverview.planetId + "/" + JSON.stringify(backupBaseOverview.date))
        .subscribe(backupPlanetOverviews => {
            this.messageService.add({
              severity: 'success',
              life: 300,
              summary: `Deleted backup ${new DatePipe("en-US").transform(backupBaseOverview.date, 'dd.MM.yyyy HH:mm:ss')}`
            });
            this.setBackupBaseOverviews(backupPlanetOverviews);
          },
          error => {
            this.messageService.add({
              severity: 'error',
              summary: `Restored to ${new DatePipe("en-US").transform(backupBaseOverview.date, 'dd.MM.yyyy HH:mm:ss')} failed`,
              detail: `${JSON.stringify(error)}`,
              sticky: true
            });
          });
    }
  }

  private setBackupBaseOverviews(backupBaseOverviews: BackupPlanetOverview[]): void {
    this.backupPlanetOverviews = backupBaseOverviews;
    this.backupPlanetOverviews.sort((a, b) => {
      return new Date(b.date).getTime() - new Date(a.date).getTime();
    });
  }

}
