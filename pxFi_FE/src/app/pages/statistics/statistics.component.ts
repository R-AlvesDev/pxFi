import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { ApiService, StatisticsResponse } from '../../services/api.service';

@Component({
  selector: 'app-statistics',
  standalone: true,
  imports: [CommonModule, FormsModule, BaseChartDirective],
  templateUrl: './statistics.component.html',
  styleUrls: ['./statistics.component.scss']
})
export class StatisticsComponent implements OnInit {
  // Date selection
  currentYear = new Date().getFullYear();
  currentMonth = new Date().getMonth() + 1;
  years: number[] = [this.currentYear, this.currentYear - 1, this.currentYear - 2];
  months = [
    { value: 1, name: 'January' }, { value: 2, name: 'February' },
    { value: 3, name: 'March' }, { value: 4, name: 'April' },
    { value: 5, name: 'May' }, { value: 6, name: 'June' },
    { value: 7, name: 'July' }, { value: 8, name: 'August' },
    { value: 9, name: 'September' }, { value: 10, name: 'October' },
    { value: 11, name: 'November' }, { value: 12, name: 'December' }
  ];

  // Data properties
  stats: StatisticsResponse | null = null;
  loading = false;
  error: string | null = null;

  // Chart properties
  public doughnutChartLabels: string[] = [];
  public doughnutChartData: ChartData<'doughnut'> = {
    labels: [],
    datasets: [{ data: [] }]
  };
  public doughnutChartType: ChartType = 'doughnut';
  public doughnutChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
  };

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.loadStatistics();
  }

  loadStatistics(): void {
    this.loading = true;
    this.error = null;
    this.stats = null;

    this.api.getMonthlyStatistics(this.currentYear, this.currentMonth).subscribe({
      next: (response) => {
        this.stats = response;
        // Prepare data for the chart
        this.doughnutChartLabels = response.expensesByCategory.map(item => item.categoryName || 'Uncategorized');
        this.doughnutChartData.datasets[0].data = response.expensesByCategory.map(item => item.total);
        this.doughnutChartData.labels = this.doughnutChartLabels;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load statistics: ' + err.message;
        this.loading = false;
      }
    });
  }
}