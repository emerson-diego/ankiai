import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Sentence } from '../sentence';
import { SentenceService } from '../sentence.service';

@Component({
  selector: 'app-sentence-list',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './sentence-list.component.html',
  styleUrls: ['./sentence-list.component.css'],
})
export class SentenceListComponent implements OnInit {
  sentences: Sentence[] = [];
  newSentence: Sentence = { text: '' };

  constructor(private sentenceService: SentenceService) {}

  ngOnInit(): void {
    this.loadSentences();
  }

  loadSentences(): void {
    this.sentenceService.getAllSentences().subscribe((sentences) => {
      this.sentences = sentences
        ? sentences.sort((a, b) =>
            a && b && a.text && b.text ? a.text.localeCompare(b.text) : 0
          )
        : [];
    });
  }

  addSentence(): void {
    if (!this.newSentence.text.trim()) {
      return; // Evita envio de sentenças vazias
    }

    this.sentenceService.createSentence(this.newSentence).subscribe(() => {
      this.loadSentences();
      this.newSentence = { text: '' };
    });
  }
}
