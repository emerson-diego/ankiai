import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SentenceListComponent } from './sentence-list/sentence-list.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [FormsModule, SentenceListComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent {
  title = 'ankiaibackend';
}
