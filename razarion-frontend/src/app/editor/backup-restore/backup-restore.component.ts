import {Component, OnInit} from "@angular/core";
import {DatePipe} from "@angular/common";
import {EditorPanel} from "../editor-model";
import {MessageService} from "primeng/api";
import {HttpClient} from "@angular/common/http";
import {ButtonModule} from 'primeng/button';
import {TableModule} from 'primeng/table';
import {BackupPlanetOverview, PlanetMgmtControllerClient} from '../../generated/razarion-share';
import {TypescriptGenerator} from '../../backend/typescript-generator';

@Component({
  selector: 'backup-restore',
  imports: [
    ButtonModule,
    TableModule,
    DatePipe
  ],
  templateUrl: './backup-restore.component.html'
})

export class BackupRestoreComponent extends EditorPanel implements OnInit {
  backupPlanetOverviews!: BackupPlanetOverview[];
  private planetMgmtControllerClient: PlanetMgmtControllerClient;

  constructor(private messageService: MessageService,
              httpClient: HttpClient) {
    super();
    this.planetMgmtControllerClient = new PlanetMgmtControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
  }

  ngOnInit(): void {
    this.planetMgmtControllerClient.loadAllBackupBaseOverviews()
      .then(backupPlanetOverviews => {
        this.setBackupBaseOverviews(backupPlanetOverviews);
      }).catch(error => {
      this.messageService.add({
        severity: 'error',
        summary: `Error loading backups`,
        detail: `${JSON.stringify(error)}`,
        sticky: true
      });
    })
  }

  onBackup() {
    this.planetMgmtControllerClient.doBackup()
      .then(backupPlanetOverviews => {
        this.messageService.add({
          severity: 'success',
          life: 300,
          summary: 'Backup successful'
        });
        this.setBackupBaseOverviews(backupPlanetOverviews);
      }).catch(error => {
      this.messageService.add({
        severity: 'error',
        summary: `Backup failed`,
        detail: `${JSON.stringify(error)}`,
        sticky: true
      });
    })
  }

  onRestore(backupBaseOverview: BackupPlanetOverview) {
    if (confirm("Restore to '" + new DatePipe("en-US").transform(backupBaseOverview.date, 'dd.MM.yyyy HH:mm:ss') + "'? Current bases/units will be overridden.")) {
      this.planetMgmtControllerClient.doRestore(backupBaseOverview)
        .then(() => {
          this.messageService.add({
            severity: 'success',
            life: 300,
            summary: `Restored to ${new DatePipe("en-US").transform(backupBaseOverview.date, 'dd.MM.yyyy HH:mm:ss')}`
          });
        })
        .catch(error => {
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
    if (confirm("Delete backup '" + new DatePipe("en-US").transform(backupBaseOverview.date, 'dd.MM.yyyy HH:mm:ss'))) {
      this.planetMgmtControllerClient.deleteBackup(backupBaseOverview)
        .then(backupPlanetOverviews => {
          this.messageService.add({
            severity: 'success',
            life: 300,
            summary: `Deleted backup ${new DatePipe("en-US").transform(backupBaseOverview.date, 'dd.MM.yyyy HH:mm:ss')}`
          });
          this.setBackupBaseOverviews(backupPlanetOverviews);
        }).catch(error => {
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
