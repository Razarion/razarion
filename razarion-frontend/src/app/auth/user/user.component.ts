import {Component, OnInit} from '@angular/core';
import {UserService} from '../user.service';
import {ButtonModule} from 'primeng/button';
import {Router} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {NgIf} from '@angular/common';
import {Dialog} from 'primeng/dialog';
import {InputText} from 'primeng/inputtext';

@Component({
  selector: 'user',
  imports: [
    ButtonModule,
    FormsModule,
    NgIf,
    Dialog,
    InputText,
  ],
  templateUrl: './user.component.html'
})
export class UserComponent implements OnInit {
  login = "";
  playerName = ""
  showDeleteDialog: boolean = false;
  deleteConfirmationInput: string = '';


  constructor(private userService: UserService, private router: Router) {
  }

  ngOnInit(): void {
    this.login = this.userService.getAuthenticatedUserName();
  }

  logout() {
    this.userService.logout();
    this.router.navigate(['invalid-token']);
  }

  cancelDelete(): void {
    this.showDeleteDialog = false;
    this.deleteConfirmationInput = '';
  }

  deleteUser(): void {
    this.userService.deleteUser().then(() => {
      this.userService.logout();
      this.showDeleteDialog = false;
      window.location.replace("/");
    }).catch((error) => {
      console.error('Error deleting user:', error);
    })

  }

}
