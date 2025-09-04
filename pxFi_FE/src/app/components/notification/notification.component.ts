import { Component } from '@angular/core';
import { AsyncPipe, NgClass, NgIf } from '@angular/common';
import { Observable } from 'rxjs';
import { Notification, NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-notification',
  standalone: true,
  imports: [AsyncPipe, NgIf, NgClass],
  templateUrl: './notification.component.html',
  styleUrls: ['./notification.component.scss']
})
export class NotificationComponent {
  notification$: Observable<Notification | null>;

  constructor(public notificationService: NotificationService) {
    this.notification$ = this.notificationService.notification$;
  }
}