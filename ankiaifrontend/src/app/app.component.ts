import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SentenceListComponent } from './sentence-list/sentence-list.component';
import { TrainComponent } from './train/train.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [FormsModule, SentenceListComponent, TrainComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent {
  title = 'ankiaibackend';
}
