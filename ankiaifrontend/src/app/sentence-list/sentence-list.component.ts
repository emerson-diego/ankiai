import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
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

  // Propriedades para o treino
  result: any = null;
  private apiUrl = 'http://localhost:8081/generate/random';
  loading: boolean = false;

  constructor(
    private sentenceService: SentenceService,
    private http: HttpClient
  ) {}

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
      return; // Evita o envio de sentenças vazias
    }

    this.sentenceService.createSentence(this.newSentence).subscribe(() => {
      this.loadSentences();
      this.newSentence = { text: '' };
    });
  }

  train(): void {
    this.loading = true; // Inicia o estado de carregamento
    this.http.get<any>(this.apiUrl).subscribe({
      next: (data) => {
        this.result = data;
        this.loading = false; // Remove o estado de carregamento após a resposta
      },
      error: (err) => {
        console.error('Erro ao treinar:', err);
        this.loading = false; // Remove o estado de carregamento mesmo em erro
      },
    });
  }

  playAudio(sentenca: string): void {
    this.sentenceService.getAudio(sentenca).subscribe({
      next: (audioBlob: Blob) => {
        const audioUrl = URL.createObjectURL(audioBlob);
        const audio = new Audio(audioUrl);
        audio.play();
      },
      error: (error) => {
        console.error('Erro ao reproduzir o áudio:', error);
      },
    });
  }
}
