import { Component, Input } from '@angular/core';
import { ModalController } from '@ionic/angular/standalone';
import { TestRuleResponse, Transaction } from '../../../services/api.service';
import { CommonModule } from '@angular/common';
import { IonHeader, IonToolbar, IonTitle, IonContent, IonList, IonItem, IonLabel, IonButton, IonButtons } from '@ionic/angular/standalone';

@Component({
  selector: 'app-test-rule-result-modal',
  templateUrl: './test-rule-result-modal.component.html',
  styleUrls: ['./test-rule-result-modal.component.scss'],
  standalone: true,
  imports: [CommonModule, IonHeader, IonToolbar, IonTitle, IonContent, IonList, IonItem, IonLabel, IonButton, IonButtons],
})
export class TestRuleResultModalComponent {
  @Input() testResults: TestRuleResponse | null = null;

  constructor(private modalCtrl: ModalController) {}

  dismiss() {
    this.modalCtrl.dismiss();
  }
}
