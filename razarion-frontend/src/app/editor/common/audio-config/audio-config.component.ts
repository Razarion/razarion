import {Component, EventEmitter, Input, OnChanges, Output, SimpleChanges} from '@angular/core';
import {AudioComponent} from '../audio/audio.component';
import {InputNumber} from 'primeng/inputnumber';
import {FormsModule} from '@angular/forms';
import {AudioItemConfig as AudioItemConfigDto} from 'src/app/generated/razarion-share';

@Component({
  selector: 'audio-config-editor',
  imports: [
    AudioComponent,
    InputNumber,
    FormsModule,
  ],
  templateUrl: './audio-config.component.html'
})
export class AudioConfigComponent implements OnChanges {
  @Input()
  audioConfig: AudioItemConfigDto | null = null;
  @Output()
  audioConfigChange = new EventEmitter<AudioItemConfigDto | null>();

  audioId: number | null = null;
  pitchCentsMin = -200;
  pitchCentsMax = 200;
  volumeMin = 0.8;
  volumeMax = 1.0;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['audioConfig']) {
      const config = this.audioConfig;
      if (config) {
        this.audioId = config.audioId ?? null;
        this.pitchCentsMin = config.pitchCentsMin ?? -200;
        this.pitchCentsMax = config.pitchCentsMax ?? 200;
        this.volumeMin = config.volumeMin ?? 0.8;
        this.volumeMax = config.volumeMax ?? 1.0;
      } else {
        this.audioId = null;
        this.pitchCentsMin = -200;
        this.pitchCentsMax = 200;
        this.volumeMin = 0.8;
        this.volumeMax = 1.0;
      }
    }
  }

  onAudioIdChange(audioId: number | null): void {
    this.audioId = audioId;
    this.emitConfig();
  }

  onFieldChange(): void {
    this.emitConfig();
  }

  private emitConfig(): void {
    if (this.audioId == null) {
      this.audioConfigChange.emit(null);
    } else {
      this.audioConfigChange.emit({
        audioId: this.audioId,
        pitchCentsMin: this.pitchCentsMin,
        pitchCentsMax: this.pitchCentsMax,
        volumeMin: this.volumeMin,
        volumeMax: this.volumeMax,
      });
    }
  }
}
