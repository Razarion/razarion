import {AfterViewInit, Component, ElementRef, NgZone, ViewChild} from '@angular/core';
import {TableModule} from 'primeng/table';
import {ChatControllerClient, ChatMessage} from '../../../generated/razarion-share';
import {Button} from 'primeng/button';
import {InputText} from 'primeng/inputtext';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {NgForOf} from '@angular/common';
import {ScrollPanel, ScrollPanelModule} from 'primeng/scrollpanel';
import {HttpClient} from '@angular/common/http';
import {TypescriptGenerator} from '../../../backend/typescript-generator';
import {ChatCockpit, ChatMessage as GwtAngularChatMessage} from '../../../gwtangular/GwtAngularFacade';

@Component({
  selector: 'chat-cockpit',
  imports: [
    TableModule,
    Button,
    InputText,
    ReactiveFormsModule,
    FormsModule,
    NgForOf,
    ScrollPanelModule,
  ],
  templateUrl: './chat-cockpit.component.html',
  styleUrl: './chat-cockpit.component.scss'
})
export class ChatCockpitComponent implements ChatCockpit, AfterViewInit {
  @ViewChild('scrollPanel')
  scrollPanel!: ScrollPanel;
  @ViewChild('chatMessageDiv')
  chatMessageDiv!: ElementRef<HTMLDivElement>;
  chatMessages: ChatMessage[] = [];
  messageToSend = ""
  private chatControllerClient: ChatControllerClient;

  constructor(httpClient: HttpClient, private zone: NgZone) {
    this.chatControllerClient = new ChatControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
  }

  onMessage(gwtAngularChatMessage: GwtAngularChatMessage): void {
    this.zone.run(() => {
      this.chatMessages.push({
        userName: gwtAngularChatMessage.getUserName(),
        message: gwtAngularChatMessage.getMessage()
      });
      this.scrollToBottom();
    });
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.loadChatMessages();
    }, 10000);
  }

  private loadChatMessages() {
    this.chatControllerClient.getAllMessages()
      .then((chatMessages) => {
        this.chatMessages.length = 0;
        this.chatMessages = [...chatMessages];
        this.scrollToBottom();
      })
      .catch(error => {
        console.warn(error);
      });
  }

  onSend() {
    this.chatControllerClient.send(this.messageToSend)
      .then(value => {
        this.messageToSend = "";
        this.scrollToBottom();
      })
      .catch(error => {
        console.warn(error);
      });
  }

  private scrollToBottom() {
    setTimeout(() => {
      const chatMessageDivHeight = this.chatMessageDiv.nativeElement.offsetHeight;
      this.scrollPanel.scrollTop(chatMessageDivHeight);
    }, 0);
  }
}
