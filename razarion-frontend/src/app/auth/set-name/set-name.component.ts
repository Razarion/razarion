import {Component, OnInit} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {ButtonModule} from 'primeng/button';
import {SetNameError, SetNameResult, UserControllerClient} from '../../generated/razarion-share';
import {HttpClient} from '@angular/common/http';
import {TypescriptGenerator} from '../../backend/typescript-generator';
import {UserService} from '../user.service';
import {CockpitDisplayService} from '../../game/cockpit/cockpit-display.service';

@Component({
  selector: 'set-name',
  imports: [
    FormsModule,
    ButtonModule
  ],
  templateUrl: './set-name.component.html'
})
export class SetNameComponent implements OnInit {
  name: string = '';
  errorMessage?: string;
  disableSaveButton = true;
  private userControllerClient: UserControllerClient;

  constructor(httpClient: HttpClient,
              private userService: UserService,
              private cockpitDisplayService: CockpitDisplayService) {
    this.userControllerClient = new UserControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
  }

  ngOnInit(): void {
    this.checkNameValid();
  }

  cancel() {
    this.cockpitDisplayService.showSetUserNameDialog = false;
  }

  setName() {
    this.disableSaveButton = true;
    this.errorMessage = undefined;

    this.userControllerClient.setName(this.name).then(result => {
      if (!result.errorResult) {
        this.userService.setName(result.userName);
        this.cockpitDisplayService.showSetUserNameDialog = false;
      } else {
        this.displayError(result);
      }
    })
  }

  onNameChanged() {
    this.checkNameValid();
  }

  private checkNameValid() {
    console.info(`Checking ${this.name}`);
    this.disableSaveButton = true;
    this.errorMessage = undefined;
    if (!this.name || this.name.trim().length < 3) {
      this.errorMessage = "Name must have at least 3 characters"
      return;
    }
    this.userControllerClient.verifySetName(this.name).then(result => {
      if (!result.errorResult) {
        this.disableSaveButton = false;
        this.errorMessage = undefined;
      } else {
        this.displayError(result);
      }
    })
  }

  private displayError(result: SetNameResult) {
    this.disableSaveButton = true;
    switch (result.errorResult) {
      case SetNameError.TO_SHORT:
        this.errorMessage = "Name must have at least 3 characters";
        break;
      case SetNameError.ALREADY_USED:
        this.errorMessage = "Name has already been taken";
        break;
      case SetNameError.UNKNOWN_ERROR:
        this.errorMessage = "Unknown error";
        break;
    }
  }

}
