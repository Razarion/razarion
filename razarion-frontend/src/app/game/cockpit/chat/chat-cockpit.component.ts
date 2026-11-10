import {AfterViewInit, Component, ElementRef, NgZone, ViewChild} from '@angular/core';
import {TableModule} from 'primeng/table';
import {ChatControllerClient, ChatMessage} from '../../../generated/razarion-share';
import {Button} from 'primeng/button';
import {InputText} from 'primeng/inputtext';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';

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
    ScrollPanelModule
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
  sending = false;
  sendError: string | null = null;
  /**
   * The clip being played, or null. Nothing of a clip is loaded before a player asks for it: the
   * chat shows only a button, and the video element exists only while this is set.
   */
  playingVideoId: number | null = null;
  videoError = false;
  private chatControllerClient: ChatControllerClient;

  constructor(httpClient: HttpClient,
              private zone: NgZone) {
    this.chatControllerClient = new ChatControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
  }

  onMessage(gwtAngularChatMessage: GwtAngularChatMessage): void {
    this.zone.run(() => {
      this.chatMessages.push({
        userName: gwtAngularChatMessage.userName,
        message: gwtAngularChatMessage.message,
        videoId: gwtAngularChatMessage.videoId!
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

  /**
   * Everybody may write: a player without a name writes under their ticker call sign, e.g. "Falcon-42" (ChatService). It took an
   * account and a name before, and so the questions of the anonymous players never arrived.
   */
  onSend() {
    const text = this.messageToSend.trim();
    if (!text || this.sending) {
      return;
    }
    this.sending = true;
    this.sendError = null;
    this.chatControllerClient.send(text)
      .then(() => {
        this.messageToSend = "";
        this.scrollToBottom();
      })
      .catch(error => {
        this.sendError = error?.status === 429 ? 'Too many messages, please wait a moment' : 'Could not send, please try again';
        console.warn(error);
      })
      .finally(() => this.sending = false);
  }

  playVideo(videoId: number) {
    this.videoError = false;
    this.playingVideoId = videoId;
  }

  closeVideo() {
    this.playingVideoId = null;
  }

  videoUrl(videoId: number): string {
    return `/rest/chat-controller/video/${videoId}`;
  }

  private scrollToBottom() {
    setTimeout(() => {
      const chatMessageDivHeight = this.chatMessageDiv.nativeElement.offsetHeight;
      this.scrollPanel.scrollTop(chatMessageDivHeight);
    }, 0);
  }
}
