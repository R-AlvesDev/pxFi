import { Component, OnInit } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from './services/auth/auth.service';
import { CategoryService } from './services/category.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  title = 'pxFi';

  constructor(private authService: AuthService, private categoryService: CategoryService) {}

  ngOnInit(): void {
    // Subscribe to the login status
    this.authService.loggedIn$.subscribe(isLoggedIn => {
      // If the user is logged in, fetch their categories immediately
      if (isLoggedIn) {
        this.categoryService.refreshCategories();
      }
    });
  }
}