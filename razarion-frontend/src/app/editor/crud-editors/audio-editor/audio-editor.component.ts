import {Component, OnInit} from '@angular/core';
import {EditorPanel} from '../../editor-model';
import {AudioControllerClient, AudioLibraryEntity} from 'src/app/generated/razarion-share';
import {TypescriptGenerator} from '../../../backend/typescript-generator';
import {HttpClient} from '@angular/common/http';
import {MessageService} from 'primeng/api';
import {FormsModule} from '@angular/forms';
import {FileUpload} from 'primeng/fileupload';
import {TableModule} from 'primeng/table';
import {ButtonModule} from 'primeng/button';

@Component({
  selector: 'audio-gallery-editor',
  imports: [
    FormsModule,
    FileUpload,
    TableModule,
    ButtonModule
  ],
  templateUrl: './audio-editor.component.html'
})
export class AudioEditorComponent extends EditorPanel implements OnInit {
  allAudioEntries: AudioLibraryEntity[] = [];
  private audioControllerClient: AudioControllerClient;
  private currentAudio: HTMLAudioElement | null = null;
  playingId: number | null = null;

  constructor(private messageService: MessageService,
              httpClient: HttpClient) {
    super();
    this.audioControllerClient = new AudioControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
  }

  ngOnInit(): void {
    this.loadAllAudio();
  }

  loadAllAudio() {
    this.audioControllerClient.readAll().then(entries => {
      this.allAudioEntries = entries;
    });
  }

  onCreate() {
    this.audioControllerClient.create().then(created => {
      this.loadAllAudio();
    });
  }

  onSave(entry: AudioLibraryEntity) {
    this.audioControllerClient.update(entry).then(() => {
      this.messageService.add({severity: 'success', life: 300, summary: 'Saved'});
      this.loadAllAudio();
    }).catch(error => {
      this.messageService.add({severity: 'error', summary: 'Save failed', detail: String(error), sticky: true});
    });
  }

  onDelete(entry: AudioLibraryEntity) {
    this.audioControllerClient.delete(entry.id).then(() => {
      this.messageService.add({severity: 'success', life: 300, summary: 'Deleted'});
      this.loadAllAudio();
    }).catch(error => {
      this.messageService.add({severity: 'error', summary: 'Delete failed', detail: String(error), sticky: true});
    });
  }

  onImportAudio(event: any, entry: AudioLibraryEntity) {
    const file: File = event.files[0];
    const reader = new FileReader();
    reader.onload = () => {
      const base64 = (reader.result as string).split(',')[1];
      entry.data = base64;
      entry.type = file.type;
      entry.size = file.size;
      this.audioControllerClient.update(entry).then(() => {
        this.messageService.add({severity: 'success', life: 3000, summary: `Audio uploaded: ${file.name}`});
        this.loadAllAudio();
      }).catch(error => {
        this.messageService.add({severity: 'error', summary: 'Upload failed', detail: String(error), sticky: true});
      });
    };
    reader.readAsDataURL(file);
  }

  onPlay(entry: AudioLibraryEntity) {
    this.onStop();
    this.currentAudio = new Audio(`/rest/audio/${entry.id}`);
    this.playingId = entry.id;
    this.currentAudio.onended = () => {
      this.playingId = null;
      this.currentAudio = null;
    };
    this.currentAudio.onerror = () => {
      this.messageService.add({severity: 'error', life: 3000, summary: `Failed to play: ${entry.internalName}`});
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
