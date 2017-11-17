import {Component, OnInit} from "@angular/core";
import {BackupRestoreService} from "./backup-restore.service";
import {BackupPlanetOverview} from "./backup-restore.dto";
import {DatePipe} from "@angular/common";

@Component({
  selector: 'backup-restore',
  templateUrl: './backup-restore.component.html',
  styleUrls: ['./backup-restore.component.css']
})

export class BackupRestoreComponent implements OnInit {
  backupPlanetOverviews: BackupPlanetOverview[];

  constructor(private backupRestoreService: BackupRestoreService) {
  }

  ngOnInit(): void {
    this.backupRestoreService.loadAllBackupBaseOverviews().then(backupBaseOverviews => {
      this.setBackupBaseOverviews(backupBaseOverviews);
    });
  }

  onBackup() {
    this.backupRestoreService.backup().then(backupBaseOverviews => {
      this.setBackupBaseOverviews(backupBaseOverviews);
    });
  }

  onRestore(backupBaseOverview: BackupPlanetOverview) {
    if(confirm("Restore to '" + new DatePipe("en-US").transform(backupBaseOverview.date, 'dd.MM.yyyy HH:mm:ss') + "'? Current bases/units will be overridden.")) {
      this.backupRestoreService.restore(backupBaseOverview);
    }
  }

  onDelete(backupBaseOverview: BackupPlanetOverview) {
    if(confirm("Delete backup '" + new DatePipe("en-US").transform(backupBaseOverview.date, 'dd.MM.yyyy HH:mm:ss'))) {
      this.backupRestoreService.doDelete(backupBaseOverview).then(backupBaseOverviews => {
        this.setBackupBaseOverviews(backupBaseOverviews);
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
