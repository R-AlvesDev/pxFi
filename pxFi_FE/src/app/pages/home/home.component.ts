import { Component, OnInit, inject } from '@angular/core';
import { AsyncPipe, NgIf } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Observable, take, tap } from 'rxjs';
import { AuthService } from '../../services/auth/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [NgIf, AsyncPipe, RouterLink],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);

  isLoggedIn$: Observable<boolean>;



  constructor() {
    this.isLoggedIn$ = this.authService.loggedIn$;
  }

  ngOnInit(): void {
    this.isLoggedIn$.pipe(
      take(1),
      tap(isLoggedIn => {
        if (isLoggedIn) {
          this.router.navigate(['/accounts']);
        }
      })
    ).subscribe();
  }
}