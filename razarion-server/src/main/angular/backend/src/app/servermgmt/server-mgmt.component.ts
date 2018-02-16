import {Component} from "@angular/core";
import {ServerMgmtService} from "./server-mgmt.service";

@Component({
  selector: 'server-mgmt',
  templateUrl: './server-mgmt.component.html',
  // styleUrls: ['./item-history.component.css']
})
export class ServerMgmt {

  constructor(private serverMgmtService: ServerMgmtService) {
  }

  onSendRestartLifecycle() {
    let check: boolean = confirm('Start the Server-Restart lifecycle');
    if (check) {
      this.serverMgmtService.sendRestartLifecycle();
    }
  }

}
