import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class IconService {
  private categoryIconMap: { [key: string]: string } = {
    // Default
    'default': 'cash-outline',
    // Income
    'Salary': 'cash-outline',
    'Freelance': 'briefcase-outline',
    'Investment': 'trending-up-outline',
    // Expenses
    'Groceries': 'cart-outline',
    'Rent': 'home-outline',
    'Utilities': 'water-outline',
    'Transport': 'car-sport-outline',
    'Restaurants': 'restaurant-outline',
    'Entertainment': 'film-outline',
    'Health': 'medkit-outline',
    'Shopping': 'pricetags-outline',
    'Travel': 'airplane-outline',
  };

  getIconForCategory(categoryName: string): string {
    return this.categoryIconMap[categoryName] || this.categoryIconMap['default'];
  }
}
