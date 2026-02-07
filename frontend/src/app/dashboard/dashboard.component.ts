import { Component, OnInit } from '@angular/core';
import { ApiService } from '../api.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  
  account: any = null;
  user: any = null;

  constructor(private api: ApiService, private router: Router) {}

  ngOnInit(): void {
    const userId = localStorage.getItem('userId');
    
    if (!userId) {
      alert("Session Expired. Please Login.");
      this.router.navigate(['/login']);
      return;
    }

    this.loadData(userId);
  }

  loadData(userId: any) {
    this.api.getAccount(userId).subscribe({
      next: (res: any) => {
        console.log("Dashboard Data:", res);
        this.account = res;
        this.user = res.user;
      },
      error: (err) => {
        console.error("Error fetching data:", err);
        if (err.status === 403) {
          alert("Session Invalid. Please Login Again.");
          localStorage.clear();
          this.router.navigate(['/login']);
        }
      }
    });
  }



  onDeposit() {
    const amount = prompt("Enter amount to Deposit:");
    if (amount) {
      this.api.post('/transactions/deposit', { userId: this.user.id, amount: amount })
        .subscribe(() => {
          alert('Deposit Successful!');
          this.loadData(this.user.id);
        });
    }
  }

  onWithdraw() {
    const amount = prompt("Enter amount to Withdraw:");
    if (amount) {
      this.api.post('/transactions/withdraw', { userId: this.user.id, amount: amount })
        .subscribe({
          next: () => {
            alert('Withdrawal Successful!');
            this.loadData(this.user.id);
          },
          error: () => alert('Insufficient Funds')
        });
    }
  }


  onTransfer() {

    const targetAccountId = prompt("Enter Receiver's User ID (Target):");
    if (!targetAccountId) return;


    const amount = prompt("Enter Amount to Transfer:");
    if (!amount) return;


    this.api.transfer(this.user.id, targetAccountId, amount).subscribe({
      next: (res) => {
        alert('Transfer Successful!');
        this.loadData(this.user.id);
      },
      error: (err) => {
        console.error(err);
        alert('Transfer Failed: ' + (err.error || "Check ID or Balance"));
      }
    });
  }

  onLogout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}