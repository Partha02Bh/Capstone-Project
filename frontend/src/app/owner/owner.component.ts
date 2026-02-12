import { Component, OnInit, HostListener } from '@angular/core';
import { ApiService } from '../api.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-owner',
  templateUrl: './owner.component.html',
  styleUrls: ['./owner.component.css']
})
export class OwnerComponent implements OnInit {

  allTransactions: any[] = [];
  searchTerm: string = '';

  constructor(private api: ApiService, private router: Router) { }

  ngOnInit() {
    // Prevent back navigation — push duplicate state
    history.pushState(null, '', location.href);
    this.fetchTransactions();
  }

  fetchTransactions() {
    this.api.getAllTransactions().subscribe((response: any) => {
      this.allTransactions = response;
    });
  }

  // Back button pressed → invalidate session → hard redirect to login
  @HostListener('window:popstate', ['$event'])
  onPopState(event: any) {
    localStorage.clear();
    window.location.href = '/login';
  }

  get filteredTransactions() {
    if (!this.searchTerm) {
      return this.allTransactions;
    }
    return this.allTransactions.filter(t =>
      t.accountId.toString().includes(this.searchTerm) ||
      (t.relatedAccountId && t.relatedAccountId.toString().includes(this.searchTerm))
    );
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}