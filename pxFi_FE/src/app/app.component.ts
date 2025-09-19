import { Component, OnInit, inject } from '@angular/core';
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
  private authService = inject(AuthService);
  private categoryService = inject(CategoryService);

  title = 'pxFi';

  ngOnInit(): void {
    this.authService.loggedIn$.subscribe(isLoggedIn => {
      if (isLoggedIn) {
        this.categoryService.refreshCategories();
      }
    });
  }
}