import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { MessageService } from 'primeng/api';
import { TypescriptGenerator } from 'src/app/backend/typescript-generator';
import { getImageUrl } from 'src/app/common';
import { InventoryControllerClient, InventoryItem, LevelUnlockConfig, UnlockControllerClient } from 'src/app/generated/razarion-share';
import { GwtAngularService } from 'src/app/gwtangular/GwtAngularService';
import { GameComponent } from '../game.component';

@Component({
  selector: 'unlock',
  templateUrl: './unlock.component.html',
  styleUrls: ['./unlock.component.scss']
})
export class UnlockComponent implements OnInit {
  getImageUrl = getImageUrl;
  levelUnlockConfigs: LevelUnlockConfig[] = [];
  crystals?: number;
  private unlockControllerClient: UnlockControllerClient;
  private inventoryControllerClient: InventoryControllerClient;

  constructor(httpClient: HttpClient,
    public gwtAngularService: GwtAngularService,
    private messageService: MessageService,
    private gameComponent: GameComponent) {
    this.unlockControllerClient = new UnlockControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
    this.inventoryControllerClient = new InventoryControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
  }

  ngOnInit(): void {
    this.inventoryControllerClient.loadCrystals().then(crystals => this.crystals = crystals).catch((reason: any) => {
      this.messageService.add({
        severity: 'error',
        summary: `Failed loading crystals`,
        detail: reason,
        sticky: true
      });
    });

    this.unlockControllerClient.getAvailableLevelUnlockConfigs().then(levelUnlockConfigs => {
      let newLevelUnlockConfigs: LevelUnlockConfig[] = [];
      levelUnlockConfigs.forEach(levelUnlockConfig => {
        newLevelUnlockConfigs.push(levelUnlockConfig);
      });
      this.levelUnlockConfigs = newLevelUnlockConfigs;
    }).catch((reason: any) => {
      this.messageService.add({
        severity: 'error',
        summary: `Failed to load unlocks`,
        detail: reason,
        sticky: true
      });
    });
  }

  onUse(levelUnlockConfig: LevelUnlockConfig): void {
    this.unlockControllerClient.unlockViaCrystals(levelUnlockConfig.id).then(unlockResultInfo => {
      if(unlockResultInfo.notEnoughCrystals) {
        this.messageService.add({
          severity: 'error',
          summary: `Not enough crystals`,
          sticky: true
        });
      } else {
        this.gameComponent.showUnkock = false;
      }
    }).catch((reason: any) => {
      this.messageService.add({
        severity: 'error',
        summary: `Failed to unlock`,
        detail: reason,
        sticky: true
      });
    });
  }
}