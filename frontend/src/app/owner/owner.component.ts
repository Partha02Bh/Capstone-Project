import { Component, OnInit } from '@angular/core';
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

  constructor(private api: ApiService, private router: Router){}

  ngOnInit() {
    this.api.getAllTransactions().subscribe((data: any) => {
      this.allTransactions = data;
    });
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

  logout(){
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}