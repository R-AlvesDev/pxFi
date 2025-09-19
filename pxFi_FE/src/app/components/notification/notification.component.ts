import { Component, inject } from '@angular/core';
import { AsyncPipe, NgClass } from '@angular/common';
import { Observable } from 'rxjs';
import { Notification, NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-notification',
  standalone: true,
  imports: [AsyncPipe, NgClass],
  templateUrl: './notification.component.html',
  styleUrls: ['./notification.component.scss']
})
export class NotificationComponent {
  notificationService = inject(NotificationService);

  notification$: Observable<Notification | null>;



  constructor() {
    this.notification$ = this.notificationService.notification$;
  }
}