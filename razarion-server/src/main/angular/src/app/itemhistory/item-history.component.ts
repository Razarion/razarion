import {Component, OnInit} from "@angular/core";
import {ItemTracking, ItemTrackingType} from "./item-history.dto";
import {Router} from "@angular/router";
import {ItemHistoryService} from "./item-history.service";

@Component({
  selector: 'backup-restore',
  templateUrl: './item-history.component.html',
  styleUrls: ['./item-history.component.css']
})

export class ItemHistoryComponent implements OnInit {
  itemTrackings: ItemTracking[];
  itemTrackingCount: number = 0;

  constructor(private itemHistoryService: ItemHistoryService, private route: Router) {
  }

  ngOnInit(): void {
    this.load();
  }

  load() {
    this.itemHistoryService.getItemHistory().then(itemTrackings => {
      this.itemTrackings = itemTrackings;
      this.itemTrackingCount = itemTrackings.length;
    });
  }

  onReload() {
    this.load();
    this.getBgColor(ItemTrackingType.BASE_CREATED);
  }

  getBgColor(type: ItemTrackingType): string {
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

}
