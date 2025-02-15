import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Sentence } from './sentence';
// Se você utiliza variáveis de ambiente, importe o environment:
// import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root',
})
export class SentenceService {
  // Definindo a URL base, que pode vir de variáveis de ambiente ou ser fixa
  private readonly BASE_URL: string = 'http://localhost:8081';
  private readonly SENTENCES_ENDPOINT: string = `${this.BASE_URL}/sentences`;
  private readonly AUDIO_ENDPOINT: string = `${this.BASE_URL}/generate/audio`;

  constructor(private http: HttpClient) {}

  // Obter todas as sentenças
  getAllSentences(): Observable<Sentence[]> {
    return this.http.get<Sentence[]>(this.SENTENCES_ENDPOINT);
  }

  // Criar uma nova sentença
  createSentence(sentence: Sentence): Observable<Sentence> {
    return this.http.post<Sentence>(this.SENTENCES_ENDPOINT, sentence);
  }

  // Obter áudio para a sentença
  getAudio(sentence: string): Observable<Blob> {
    const payload = { sentence };
    return this.http.post(this.AUDIO_ENDPOINT, payload, { responseType: 'blob' });
  }
}