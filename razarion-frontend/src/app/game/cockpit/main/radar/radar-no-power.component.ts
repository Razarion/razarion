import { Component, ElementRef, OnInit, ViewChild } from "@angular/core";
import { RadarComponent } from "./radar.component";

@Component({
    selector: 'radar-no-power',
    template: `<div class="radarNoPower" #divElement>No Power</div>`,
    styleUrls: ['./radar-no-power.component.css'],
    standalone: false
})
export class RadarNoPowerComponent implements OnInit {
    @ViewChild('divElement', { static: true })
    divElement!: ElementRef<HTMLDivElement>;

    ngOnInit(): void {
        this.divElement.nativeElement.style.setProperty("width", RadarComponent.WIDTH + "px");
        this.divElement.nativeElement.style.setProperty("height", RadarComponent.HEIGHT + "px");
    }
}  