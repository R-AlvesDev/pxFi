import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type NotificationType = 'success' | 'error' | 'info';

export interface Notification {
  message: string;
  type: NotificationType;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notification = new BehaviorSubject<Notification | null>(null);
  public notification$ = this.notification.asObservable();

  private timeoutId: any;

  show(message: string, type: NotificationType = 'info', duration: number = 4000): void {
    if (this.timeoutId) {
      clearTimeout(this.timeoutId);
    }

    this.notification.next({ message, type });

    this.timeoutId = setTimeout(() => {
      this.hide();
    }, duration);
  }

  hide(): void {
    this.notification.next(null);
    if (this.timeoutId) {
      clearTimeout(this.timeoutId);
    }
  }
}