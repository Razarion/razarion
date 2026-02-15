import { HttpClient } from '@angular/common/http';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { TypescriptGenerator } from 'src/app/backend/typescript-generator';
import { AudioControllerClient } from 'src/app/generated/razarion-share';
import {Select} from 'primeng/select';
import {FormsModule} from '@angular/forms';
import {ButtonModule} from 'primeng/button';

@Component({
  selector: 'audio-editor',
  imports: [
    Select,
    FormsModule,
    ButtonModule
  ],
  templateUrl: './audio.component.html'
})
export class AudioComponent implements OnInit {
  @Input("audioId")
  audioId: number | null = null;
  @Output()
  audioIdChange = new EventEmitter<number | null>();
  private audioControllerClient: AudioControllerClient;
  audioOptions: { label: string, audioId: number }[] = [];
  private currentAudio: HTMLAudioElement | null = null;
  playingId: number | null = null;

  constructor(httpClient: HttpClient) {
    this.audioControllerClient = new AudioControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
  }

  ngOnInit(): void {
    this.audioControllerClient.getObjectNameIds().then(objectNameIds => {
      this.audioOptions = [];
      objectNameIds.forEach(objectNameId => {
        this.audioOptions.push({ label: `${objectNameId.internalName} '${objectNameId.id}'`, audioId: objectNameId.id });
      });
    });
  }

  onChange() {
    this.audioIdChange.emit(this.audioId);
  }

  onPlay(audioId: number | null) {
    if (!audioId) return;
    this.onStop();
    this.currentAudio = new Audio(`/rest/audio/${audioId}`);
    this.playingId = audioId;
    this.currentAudio.onended = () => {
      this.playingId = null;
      this.currentAudio = null;
    };
    this.currentAudio.onerror = () => {
      this.playingId = null;
      this.currentAudio = null;
    };
    this.currentAudio.play();
  }

  onStop() {
    if (this.currentAudio) {
      this.currentAudio.pause();
      this.currentAudio = null;
      this.playingId = null;
    }
  }
}
