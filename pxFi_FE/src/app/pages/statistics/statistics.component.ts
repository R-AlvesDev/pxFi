import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BaseChartDirective } from 'ng2-charts';
import { Chart, ChartConfiguration, ChartData, ChartType, registerables } from 'chart.js';
import { ApiService, StatisticsResponse, YearlyStatisticsResponse } from '../../services/api.service';

@Component({
  selector: 'app-statistics',
  standalone: true,
  imports: [CommonModule, FormsModule, BaseChartDirective],
  templateUrl: './statistics.component.html',
  styleUrls: ['./statistics.component.scss']
})
export class StatisticsComponent implements OnInit {
  // --- ADD THIS LINE ---
  // This gives us direct access to the chart instance in our template
  @ViewChild(BaseChartDirective) chart: BaseChartDirective | undefined;

  // View control
  viewMode: 'monthly' | 'yearly' = 'monthly';

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
  monthlyStats: StatisticsResponse | null = null;
  yearlyStats: YearlyStatisticsResponse | null = null;
  loading = false;
  error: string | null = null;

  // Doughnut Chart (Monthly)
  public doughnutChartData: ChartData<'doughnut'> = { labels: [], datasets: [{ data: [] }] };
  public doughnutChartType: ChartType = 'doughnut';
  public doughnutChartOptions: ChartConfiguration['options'] = { responsive: true, maintainAspectRatio: false };

  // Bar Chart (Yearly)
  public barChartData: ChartData<'bar'> = {
    labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
    datasets: [
      { data: [], label: 'Income', backgroundColor: 'rgba(75, 192, 192, 0.8)' },
      { data: [], label: 'Expenses', backgroundColor: 'rgba(255, 99, 132, 0.8)' }
    ]
  };
  public barChartType: ChartType = 'bar';
  public barChartOptions: ChartConfiguration['options'] = { responsive: true, maintainAspectRatio: false };

  constructor(private api: ApiService) {
    Chart.register(...registerables);
  }

  ngOnInit(): void {
    this.loadStatistics();
  }

  loadStatistics(): void {
    this.loading = true;
    this.error = null;
    this.monthlyStats = null;
    this.yearlyStats = null;

    if (this.viewMode === 'monthly') {
      this.api.getMonthlyStatistics(this.currentYear, this.currentMonth).subscribe({
        next: this.handleMonthlyResponse.bind(this),
        error: this.handleError.bind(this)
      });
    } else {
      this.api.getYearlyStatistics(this.currentYear).subscribe({
        next: this.handleYearlyResponse.bind(this),
        error: this.handleError.bind(this)
      });
    }
  }

  private handleMonthlyResponse(response: StatisticsResponse): void {
    this.monthlyStats = response;
    
    // 1. We mutate the existing object's properties.
    this.doughnutChartData.labels = response.expensesByCategory.map(item => item.categoryName || 'Uncategorized');
    this.doughnutChartData.datasets[0].data = response.expensesByCategory.map(item => item.total);

    // 2. We explicitly tell the chart to update itself.
    this.chart?.update();

    this.loading = false;
  }

  private handleYearlyResponse(response: YearlyStatisticsResponse): void {
    this.yearlyStats = response;
    this.barChartData.datasets[0].data = response.monthlyBreakdowns.map(m => m.income);
    this.barChartData.datasets[1].data = response.monthlyBreakdowns.map(m => m.expenses);

    // Also update the yearly chart for consistency
    this.chart?.update();
    
    this.loading = false;
  }

  private handleError(err: any): void {
    this.error = 'Failed to load statistics: ' + err.message;
    this.loading = false;
  }
}