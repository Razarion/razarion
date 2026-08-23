import {Component, Input, Pipe, PipeTransform} from "@angular/core";
import {ImageGalleryItem} from "../../gwtangular/GwtAngularFacade";
import {getImageUrl} from "../../common";
import {FormatFileSizePipe} from '../../common/pipes/format-file-size-pipe';

@Pipe({
  name: 'typeTransform'
})
export class ImageTypePipe implements PipeTransform {
  transform(value: any): any {
    return value?.replace("image/", '');
  }
}

@Component({
  selector: 'image-gallery-item',
  styleUrl: 'image-gallery-item.component.scss',
  imports: [
    ImageTypePipe,
    FormatFileSizePipe
  ],
  template: `
    <table style="width: auto;height: auto">
      <tr>
        <td colspan="2">
          <img src="{{getImgUrl()}}" alt="Image Gallery Item"
               class="image-gallery-img">
        </td>
      </tr>
      <tr>
        <td>
          <div class="image-gallery-descr">{{ imageGalleryItem.id }}</div>
        </td>
        <td>
          <div class="image-gallery-descr"
               style="text-align: right;">{{ imageGalleryItem.internalName }}
          </div>
        </td>
      </tr>
      <tr>
        <td>
          <div class="image-gallery-descr">{{ imageGalleryItem.size | formatFileSize:false }}</div>
        </td>
        <td>
          <div class="image-gallery-descr"
               style="text-align: right;">{{ imageGalleryItem.type | typeTransform }}
          </div>
        </td>
      </tr>
    </table>
  `
})
export class ImageGalleryItemComponent {
  @Input('image-gallery-item')
  imageGalleryItem!: ImageGalleryItem;

  getImgUrl(): string {
    return getImageUrl(this.imageGalleryItem.id);
  }
}

