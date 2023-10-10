import {
  AfterViewInit,
  Component,
  ComponentFactory,
  ComponentFactoryResolver,
  Input,
  ViewChild,
  ViewContainerRef
} from '@angular/core';
import {EditorModel, EditorPanel} from "../editor-model";
import {MessageService} from "primeng/api";
import {GameComponent} from "../../game/game.component";
import {MainCockpitComponent} from "../../game/cockpit/main/main-cockpit.component";

@Component({
  selector: 'app-editor-panel',
  templateUrl: './editor-panel.component.html'
})
export class EditorPanelComponent implements AfterViewInit {
  @Input("mainCockpitComponent")
  mainCockpitComponent!: MainCockpitComponent;
  @ViewChild("editorContainer", {read: ViewContainerRef})
  editorContainer!: ViewContainerRef;
  @Input("editorModel")
  editorModel!: EditorModel;
  @Input("gameComponent")
  gameComponent!: GameComponent;

  constructor(private resolver: ComponentFactoryResolver, private messageService: MessageService) {
  }

  ngAfterViewInit(): void {
    // Promise solves Angular problem
    // Uncaught Error: NG0100: ExpressionChangedAfterItHasBeenCheckedError: Expression has changed after it was checked. Previous value: 'undefined'. Current value: '[object Object]'. It seems like the view has been created after its parent and its children have been dirty checked. Has it been created in a change detection hook?. Find more at https://angular.io/errors/NG0100
    Promise.resolve().then(() => {
      try {
        const factory: ComponentFactory<EditorPanel> = this.resolver.resolveComponentFactory(this.editorModel.editorComponent);
        let  componentRef = this.editorContainer.createComponent(factory);
        componentRef.instance.init(this.editorModel, this.mainCockpitComponent);
      } catch (error) {
        this.messageService.add({
          severity: 'error',
          summary: `Can not open editor: ${this.editorModel.name}`,
          detail: String(error),
          sticky: true
        });
        console.error(error);
        throw error;
      }
    });
  }

  onClose() {
    this.gameComponent.removeEditorModel(this.editorModel);
  }
}
