import { Component, OnInit } from '@angular/core';
import { ApiService } from '../api.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

  // Data State
  account: any = null;
  user: any = null;
  transactions: any[] = [];

  // Modal State
  showModal = false;
  modalType: 'DEPOSIT' | 'WITHDRAW' | 'TRANSFER' | 'MESSAGE' | 'PROFILE' | 'SUCCESS' | 'FAILURE' = 'DEPOSIT';
  transactionAmount: number | null = null;
  targetUserId: string = '';

  // Message/Success Modal State
  modalTitle: string = '';
  modalMessage: string = '';
  isError: boolean = false;

  constructor(private api: ApiService, private router: Router) { }

  ngOnInit(): void {
    const userId = localStorage.getItem('userId');

    if (!userId) {
      this.showMessage("Session Expired", "Please Login Again.", true);
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

        // Fetch Transactions
        if (this.account && this.account.id) {
          this.api.getTransactions(this.account.id).subscribe({
            next: (txs: any) => {
              this.transactions = txs.sort((a: any, b: any) =>
                new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
              );
            },
            error: (err: any) => console.error("Error fetching transactions:", err)
          });
        }
      },
      error: (err) => {
        console.error("Error fetching data:", err);
        if (err.status === 403) {
          this.showMessage("Session Invalid", "Please Login Again.", true);
          localStorage.clear();
          this.router.navigate(['/login']);
        }
      }
    });
  }

  // Open Action Modal
  openAction(type: 'DEPOSIT' | 'WITHDRAW' | 'TRANSFER') {
    this.modalType = type;
    this.showModal = true;
    this.transactionAmount = null;
    this.targetUserId = '';
    this.modalTitle = type; // Default title
  }

  openProfile() {
    this.modalType = 'PROFILE';
    this.showModal = true;
    this.modalTitle = 'My Profile';
  }

  // Show Message Modal (Error or Info)
  showMessage(title: string, message: string, isError: boolean = false) {
    this.modalType = 'MESSAGE';
    this.modalTitle = title;
    this.modalMessage = message;
    this.isError = isError;
    this.showModal = true;
  }

  // Show Success Animation Modal
  showSuccess(message: string) {
    this.modalType = 'SUCCESS';
    this.modalTitle = 'Success';
    this.modalMessage = message;
    this.showModal = true;
    // Auto-close after 2 seconds
    setTimeout(() => {
      this.closeModal();
    }, 2000);
  }

  // Show Failure Animation Modal
  showFailure(message: string) {
    this.modalType = 'FAILURE';
    this.modalTitle = 'Failed';
    this.modalMessage = message;
    this.showModal = true;
    setTimeout(() => {
      this.closeModal();
    }, 2500); // Slightly longer for error reading
  }

  closeModal() {
    this.showModal = false;
  }

  // Submit Transaction
  onSubmit() {
    if (this.modalType === 'MESSAGE' || this.modalType === 'SUCCESS' || this.modalType === 'FAILURE') {
      this.closeModal();
      return;
    }

    if (!this.transactionAmount || this.transactionAmount <= 0) {
      this.showFailure("Invalid Amount");
      return;
    }

    if (this.modalType === 'DEPOSIT') {
      this.api.post('/transactions/deposit', { userId: this.user.id, amount: this.transactionAmount })
        .subscribe({
          next: () => {
            this.loadData(this.user.id);
            this.showSuccess("Deposit Successful!");
          },
          error: () => this.showFailure("Deposit Failed")
        });
    }
    else if (this.modalType === 'WITHDRAW') {
      this.api.post('/transactions/withdraw', { userId: this.user.id, amount: this.transactionAmount })
        .subscribe({
          next: () => {
            this.loadData(this.user.id);
            this.showSuccess("Withdrawal Successful!");
          },
          error: () => this.showFailure("Insufficient Funds")
        });
    }
    else if (this.modalType === 'TRANSFER') {
      if (!this.targetUserId) {
        this.showFailure("Missing Target ID");
        return;
      }
      this.api.transfer(this.user.id, this.targetUserId, this.transactionAmount).subscribe({
        next: (res) => {
          this.loadData(this.user.id);
          this.showSuccess("Transfer Successful!");
        },
        error: (err) => {
          console.error(err);
          this.showFailure(this.getErrorMessage(err) || "Transfer Failed");
        }
      });
    }
  }

  // Helper to parse error messages
  private getErrorMessage(err: any): string {
    if (err.error) {
      if (typeof err.error === 'string') {
        try {
          const parsed = JSON.parse(err.error);
          return parsed.message || parsed.error || err.error;
        } catch (e) {
          return err.error;
        }
      } else if (typeof err.error === 'object') {
        return err.error.message || err.error.error || "Unknown Error";
      }
    }
    return err.message || "Transaction Failed";
  }

  onLogout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}