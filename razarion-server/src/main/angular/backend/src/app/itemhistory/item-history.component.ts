import {Component, OnInit} from "@angular/core";
import {ItemTracking, ItemTrackingSearch, ItemTrackingType} from "./item-history.dto";
import {ItemHistoryService} from "./item-history.service";

@Component({
  selector: 'backup-restore',
  templateUrl: './item-history.component.html',
  styleUrls: ['./item-history.component.css']
})

export class ItemHistoryComponent implements OnInit {
  itemTrackings: ItemTracking[];
  searchFromDateString: string;
  searchToDateString: string;
  humanPlayerId: number;
  botId: number;
  searchCount: number;
  actualCount: number = 0;

  constructor(private itemHistoryService: ItemHistoryService) {
  }

  ngOnInit(): void {
    this.load();
  }

  load() {
    let itemTrackingSearch: ItemTrackingSearch = new ItemTrackingSearch();
    itemTrackingSearch.from = new Date(this.searchFromDateString);
    itemTrackingSearch.to = new Date(this.searchToDateString);
    itemTrackingSearch.humanPlayerId = this.humanPlayerId;
    itemTrackingSearch.botId = this.botId;
    itemTrackingSearch.count = this.searchCount;

    this.itemHistoryService.getItemHistory(itemTrackingSearch).then(itemTrackings => {
      this.itemTrackings = itemTrackings;
      this.actualCount = itemTrackings.length;
    });
  }

  onReload() {
    this.load();
  }

  displayBgColor(type: ItemTrackingType): string {
    switch (type) {
      case  ItemTrackingType.SERVER_START:
        return "#ff1217";
      case
      ItemTrackingType.BASE_CREATED:
        return "#d4ffc3";
      case
      ItemTrackingType.BASE_DELETE:
        return "#fcdeff";
      case
      ItemTrackingType.BASE_ITEM_SPAWN:
        return "#d3c6ff";
      case
      ItemTrackingType.BASE_ITEM_SPAWN_DIRECTLY :
        return "#faffc7";
      case
      ItemTrackingType.BASE_ITEM_BUILT :
        return "#f8ffcc";
      case
      ItemTrackingType.BASE_ITEM_FACTORIZED :
        return "#d3f5ff";
      case
      ItemTrackingType.BASE_ITEM_KILLED :
        return "#ffcad1";
      case
      ItemTrackingType.BASE_ITEM_REMOVED :
        return "#fff1e3";
      case
      ItemTrackingType.RESOURCE_ITEM_CREATED :
        return "#b49fe9";
      case
      ItemTrackingType.RESOURCE_ITEM_DELETED :
        return "#ffa6f1";
      case
      ItemTrackingType.BOX_ITEM_CREATED :
        return "#f7ffd4";
      case
      ItemTrackingType.BOX_ITEM_DELETED:
        return "#9dff63";
      default:
        return "white";
    }
  }

  displayItemTypeId(itemTracking: ItemTracking): string {
    if (itemTracking.itemTypeId == null) {
      return null;
    }

    switch (itemTracking.type) {
      case ItemTrackingType.BASE_ITEM_SPAWN:
      case ItemTrackingType.BASE_ITEM_SPAWN_DIRECTLY :
      case ItemTrackingType.BASE_ITEM_BUILT :
      case ItemTrackingType.BASE_ITEM_FACTORIZED :
      case ItemTrackingType.BASE_ITEM_KILLED :
      case ItemTrackingType.BASE_ITEM_REMOVED :
        return this.itemHistoryService.name4BaseItemTypeId(itemTracking.itemTypeId) + "(" + itemTracking.itemTypeId + ")";
      case ItemTrackingType.RESOURCE_ITEM_CREATED :
      case ItemTrackingType.RESOURCE_ITEM_DELETED :
        return this.itemHistoryService.name4ResourceItemTypeId(itemTracking.itemTypeId) + "(" + itemTracking.itemTypeId + ")";
      case ItemTrackingType.BOX_ITEM_CREATED :
      case ItemTrackingType.BOX_ITEM_DELETED:
        return this.itemHistoryService.name4BoxItemTypeId(itemTracking.itemTypeId) + "(" + itemTracking.itemTypeId + ")";
      default:
        return "???";
    }
  }

  displayTargetHumanPlayerId(itemTracking: ItemTracking): string {
    if (itemTracking.targetHumanPlayerId == null) {
      return null;
    }
    return this.getName4HumanPlayerId(itemTracking.targetHumanPlayerId);
  }

  displayActorHumanPlayerId(itemTracking: ItemTracking) {
    if (itemTracking.actorHumanPlayerId == null) {
      return null;
    }
    return this.getName4HumanPlayerId(itemTracking.actorHumanPlayerId);
  }

  private getName4HumanPlayerId(humanPlayerId: number): string {
    let name: string = this.itemHistoryService.name4HumanPlayerId(humanPlayerId);
    if (name != null) {
      return name + "(" + humanPlayerId + ")";
    } else {
      return humanPlayerId.toString();
    }
  }

  displayTargetBot(itemTracking: ItemTracking): string {
    if (itemTracking.targetBaseBotId == null) {
      return null;
    }
    return this.getBot4Id(itemTracking.targetBaseBotId);
  }

  displayActorBot(itemTracking: ItemTracking): string {
    if (itemTracking.actorBaseBotId == null) {
      return null;
    }
    return this.getBot4Id(itemTracking.actorBaseBotId);
  }

  private getBot4Id(botId: number): string {
    let name: string = this.itemHistoryService.name4BotId(botId);
    if (name != null) {
      return name + "(" + botId + ")";
    } else {
      return botId.toString();
    }
  }
}
