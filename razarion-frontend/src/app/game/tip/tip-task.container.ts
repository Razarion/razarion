import {AbstractTipTask, TipTaskContext} from './tiptask/abstract-tip-task';

export class TipTaskContainer {
  private abstractTipTasks: AbstractTipTask[] = [];
  private fallbackAbstractTipTasks: AbstractTipTask[] = [];
  private current: AbstractTipTask[] | null = this.abstractTipTasks;
  private currentTaskIndex = 0;
  public readonly tipTaskContext: TipTaskContext = new TipTaskContext();

  public add(abstractTipTask: AbstractTipTask) {
    this.abstractTipTasks.push(abstractTipTask);
  }

  public addFallback(abstractTipTask: AbstractTipTask) {
    this.fallbackAbstractTipTasks.push(abstractTipTask);
  }

  public getCurrentTask(): AbstractTipTask {
    return this.current![this.currentTaskIndex];
  }


  public next() {
    this.currentTaskIndex++;
    if (this.hasTip() && this.getCurrentTask().isFulfilled()) {
      this.next();
    }
  }

  public hasTip(): boolean {
    return this.current != null && this.currentTaskIndex < this.current.length;
  }

  public backtrackTask(): void {
    this.internalBacktrackTask(this.currentTaskIndex - 1);
  }

  private internalBacktrackTask(taskIndex: number) {
    if (taskIndex < 0) {
      this.currentTaskIndex = 0;
    } else {
      let task = this.current![taskIndex];
      if (task.isFulfilled()) {
        this.currentTaskIndex = taskIndex + 1;
      } else {
        this.internalBacktrackTask(taskIndex - 1);
      }
    }
  }

  public activateFallback() {
    if (this.fallbackAbstractTipTasks.length == 0) {
      this.current = null;
    } else {
      this.current = this.fallbackAbstractTipTasks;
      this.currentTaskIndex = 0;
    }
  }

  public clean() {
    if (this.hasTip()) {
      this.getCurrentTask().cleanup();
      this.current = null;
    }
  }

}
