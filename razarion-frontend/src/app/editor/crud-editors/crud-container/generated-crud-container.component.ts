import { Component, ComponentFactoryResolver, ViewChild, ViewContainerRef } from '@angular/core';
import { MessageService } from "primeng/api";
import { BaseItemType, Config, ObjectNameId } from "../../../generated/razarion-share";
import { AbstractCrudContainerComponent } from './crud-container.component';
import { HttpClient } from '@angular/common/http';
import { TypescriptGenerator } from 'src/app/backend/typescript-generator';

@Component({
    selector: 'generated-crud-container',
    templateUrl: './crud-container.component.html'
})
export class GeneratedCrudContainerComponent extends AbstractCrudContainerComponent {
  private editorControllerClient!: any;

  constructor(messageService: MessageService,
    private httpClient: HttpClient,
    resolver: ComponentFactoryResolver) {
    super(messageService, resolver);
  }

  initCustom(childComponent: any): void {
    if (!childComponent.editorControllerClient) {
      console.error(`No editorControllerClient in ${childComponent}`);
      this.messageService.add({
        severity: 'error',
        summary: `No editorControllerClient in ${childComponent}`,
        sticky: true
      });
      return;
    }

    this.editorControllerClient = new childComponent.editorControllerClient(TypescriptGenerator.generateHttpClientAdapter(this.httpClient));
  }

  requestObjectNameId(): Promise<ObjectNameId[]> {
    return this.editorControllerClient.getObjectNameIds();
  }

  read(id: number): Promise<Config> {
    return this.editorControllerClient.read(id);
  }

  create(): Promise<Config> {
    return this.editorControllerClient.create();
  }

  update(config: Config): Promise<void> {
    return this.editorControllerClient.update(<BaseItemType>config);
  }

  delete(id: number): Promise<void> {
    return this.editorControllerClient.delete(id);
  }

  getEditorName(): string {
    return "GeneratedCrudContainerComponent";
  }

}
