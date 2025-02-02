import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';

@Component({
  selector: 'app-train',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './train.component.html',
  styleUrls: ['./train.component.css'],
})
export class TrainComponent {
  result: any = null;
  private apiUrl = 'http://localhost:8080/generate/random';

  constructor(private http: HttpClient) {}

  train(): void {
    this.http.get<any>(this.apiUrl).subscribe({
      next: (data) => {
        this.result = data;
      },
      error: (err) => {
        console.error('Erro ao treinar:', err);
      },
    });
  }
}
