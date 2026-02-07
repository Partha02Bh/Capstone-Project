import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) { }

  private getHeaders() {
    const token = localStorage.getItem('token');
    return {
      headers: new HttpHeaders({
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      })
    };
  }


  login(u: string, p: string) {
    return this.http.post(`${this.baseUrl}/login`, { username: u, password: p }, { responseType: 'text' });
  }

  verifyOtp(username: string, otp: string) {
    return this.http.post(`${this.baseUrl}/verify`, { username, otp });
  }

  register(u: string, p: string, name: string, email: string, phone: string) {
    const body = { username: u, password: p, fullName: name, email: email, phone: phone };
    return this.http.post(`${this.baseUrl}/register`, body);
  }

  getAccount(userId: any) {
    return this.http.get(`${this.baseUrl}/accounts/${userId}`, this.getHeaders());
  }

  post(endpoint: string, data: any) {
    return this.http.post(`${this.baseUrl}${endpoint}`, data, { ...this.getHeaders(), responseType: 'text' });
  }
  
  transfer(sourceId: any, targetId: any, amount: any) {
    const body = { sourceId, targetId, amount };
    return this.post('/transactions/transfer', body);
  }


  getAllTransactions() {
    return this.http.get<any[]>(`${this.baseUrl}/admin/transactions`, this.getHeaders());
  }
}