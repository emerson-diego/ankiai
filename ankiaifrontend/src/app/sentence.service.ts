import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Sentence } from './sentence';

@Injectable({
  providedIn: 'root',
})
export class SentenceService {
  private apiUrl = 'http://localhost:8081/sentences'; // URL da sua API

  constructor(private http: HttpClient) {}

  // Obter todas as sentenças
  getAllSentences(): Observable<Sentence[]> {
    return this.http.get<Sentence[]>(this.apiUrl);
  }

  // Criar uma nova sentença
  createSentence(sentence: Sentence): Observable<Sentence> {
    return this.http.post<Sentence>(this.apiUrl, sentence);
  }
}
