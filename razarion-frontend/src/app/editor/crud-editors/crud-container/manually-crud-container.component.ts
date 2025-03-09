import { Component, ComponentFactoryResolver, ViewChild, ViewContainerRef } from '@angular/core';
import { MessageService } from "primeng/api";
import { HttpClient } from "@angular/common/http";
import { Config, ObjectNameId } from "../../../generated/razarion-share";
import { AbstractCrudContainerComponent } from './crud-container.component';

@Component({
    selector: 'manually-crud-container',
    templateUrl: './crud-container.component.html',
    standalone: false
})
export class ManuallyCrudContainerComponent extends AbstractCrudContainerComponent {
  private editorUrl?: string;

  constructor(messageService: MessageService,
    private httpClient: HttpClient,
    resolver: ComponentFactoryResolver) {
    super(messageService, resolver);
  }

  initCustom(childComponent: any): void {
    this.editorUrl = childComponent.editorUrl;
    if (!this.editorUrl) {
      console.error(`No editorUrl in ${childComponent}`);
      this.messageService.add({
        severity: 'error',
        summary: `No editorUrl in ${childComponent}`,
        sticky: true
      });
      return;
    }
  }

  requestObjectNameId(): Promise<ObjectNameId[]> {
    return new Promise((resolve, reject) => {
      this.httpClient.get(`${this.editorUrl}/objectNameIds`).subscribe({
        next: (objectNameIds: any) => {
          resolve(objectNameIds);
        },
        error: (error: any) => {
          reject(error);
        }
      });
    });

  }

  read(id: number): Promise<Config> {
    return new Promise((resolve, reject) => {
      this.httpClient.get(`${this.editorUrl}/read/${id}`).subscribe({
        next: (configObject: any) => {
          resolve(configObject);
        },
        error: (error: any) => {
          reject(error);
        }
      });
    });
  }

  create(): Promise<any> {
    return new Promise((resolve, reject) => {
      this.httpClient.post(`${this.editorUrl}/create`, {}).subscribe({
        next: () => {
          resolve(undefined);
        },
        error: (error: any) => {
          reject(error);
        }
      });
    });
  }

  update(config: Config): Promise<void> {
    return new Promise((resolve, reject) => {
      this.httpClient.post(`${this.editorUrl}/update`, config).subscribe({
        next: (configObject: any) => {
          resolve();
        },
        error: (error: any) => {
          reject(error);
        }
      });
    });
  }

  delete(id: number): Promise<void> {
    return new Promise((resolve, reject) => {
      this.httpClient.delete(`${this.editorUrl}/delete/${id}`).subscribe({
        next: () => {
          resolve();
        },
        error: (error: any) => {
          reject(error);
        }
      });
    });
  }

  getEditorName(): string {
    return this.editorUrl!;
  }

}
