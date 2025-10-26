import { Component, Input } from '@angular/core';
import { ModalController } from '@ionic/angular/standalone';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { IonHeader, IonToolbar, IonTitle, IonContent, IonButton, IonButtons, IonList, IonItem, IonLabel, IonInput, IonSelect, IonSelectOption } from '@ionic/angular/standalone';
import { Category } from '../../../services/api.service';

@Component({
  selector: 'app-filter-modal',
  templateUrl: './filter-modal.component.html',
  styleUrls: ['./filter-modal.component.scss'],
  standalone: true,
  imports: [CommonModule, FormsModule, IonHeader, IonToolbar, IonTitle, IonContent, IonButton, IonButtons, IonList, IonItem, IonLabel, IonInput, IonSelect, IonSelectOption]
})
export class FilterModalComponent {
  @Input() selectedCategoryFilter!: string;
  @Input() mainCategories!: Category[];
  @Input() startDate!: string;
  @Input() endDate!: string;
  @Input() searchTerm!: string;

  constructor(private modalCtrl: ModalController) {}

  cancel() {
    return this.modalCtrl.dismiss(null, 'cancel');
  }

  confirm() {
    const filters = {
      selectedCategoryFilter: this.selectedCategoryFilter,
      startDate: this.startDate,
      endDate: this.endDate,
      searchTerm: this.searchTerm
    };
    return this.modalCtrl.dismiss(filters, 'confirm');
  }
}
