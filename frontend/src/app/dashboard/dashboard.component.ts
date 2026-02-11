import { Component, OnInit, HostListener } from '@angular/core';
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
  modalType: 'DEPOSIT' | 'WITHDRAW' | 'TRANSFER' | 'TRANSFER_CONFIRM' | 'MESSAGE' | 'PROFILE' | 'SUCCESS' | 'FAILURE' = 'DEPOSIT';
  transactionAmount: number | null = null;
  amountInput: string = '';
  amountError: string = '';
  targetUserId: string = '';
  receiverName: string = '';
  receiverError: string = '';
  lookingUpReceiver: boolean = false;

  // Message/Success Modal State
  modalTitle: string = '';
  modalMessage: string = '';
  isError: boolean = false;

  constructor(private api: ApiService, private router: Router) { }

  ngOnInit(): void {
    // Prevent back navigation — push a duplicate state
    history.pushState(null, '', location.href);

    const userId = localStorage.getItem('userId');

    if (!userId) {
      this.showMessage("Session Expired", "Please Login Again.", true);
      this.router.navigate(['/login']);
      return;
    }

    this.loadData(userId);
  }

  // Back button pressed → invalidate session → hard redirect to login
  @HostListener('window:popstate', ['$event'])
  onPopState(event: any) {
    localStorage.clear();
    window.location.href = '/login';
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
    this.amountInput = '';
    this.amountError = '';
    this.targetUserId = '';
    this.receiverName = '';
    this.receiverError = '';
    this.lookingUpReceiver = false;
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

  // --- Amount Validation ---
  validateAmount(): void {
    const val = this.amountInput.trim();
    if (!val) {
      this.amountError = 'Amount is required';
      this.transactionAmount = null;
      return;
    }
    if (!/^\d+(\.\d{1,2})?$/.test(val)) {
      this.amountError = 'Amount must be a valid number (no letters or special characters)';
      this.transactionAmount = null;
      return;
    }
    const num = parseFloat(val);
    if (num === 0) {
      this.amountError = 'Amount cannot be zero';
      this.transactionAmount = null;
      return;
    }
    if (num < 0) {
      this.amountError = 'Amount cannot be negative';
      this.transactionAmount = null;
      return;
    }

    if (this.modalType === 'DEPOSIT') {
      if (num < 100) {
        this.amountError = 'Minimum deposit amount is $100';
        this.transactionAmount = null;
        return;
      }
      if (num > 10000) {
        this.amountError = 'Maximum deposit amount is $10,000';
        this.transactionAmount = null;
        return;
      }
    }
    if (this.modalType === 'WITHDRAW') {
      if (num < 100) {
        this.amountError = 'Minimum withdrawal amount is $100';
        this.transactionAmount = null;
        return;
      }
      if (num > 10000) {
        this.amountError = 'Maximum withdrawal amount is $10,000';
        this.transactionAmount = null;
        return;
      }
    }
    if (this.modalType === 'TRANSFER') {
      if (num < 100) {
        this.amountError = 'Minimum transfer amount is $100';
        this.transactionAmount = null;
        return;
      }
      if (num > 10000) {
        this.amountError = 'Maximum transfer amount is $10,000';
        this.transactionAmount = null;
        return;
      }
    }

    this.amountError = '';
    this.transactionAmount = num;
  }

  get isAmountValid(): boolean {
    const val = this.amountInput.trim();
    if (!val || !/^\d+(\.\d{1,2})?$/.test(val)) return false;
    const num = parseFloat(val);
    if (num <= 0) return false;
    if (this.modalType === 'DEPOSIT' && (num < 100 || num > 10000)) return false;
    if (this.modalType === 'WITHDRAW' && (num < 100 || num > 10000)) return false;
    if (this.modalType === 'TRANSFER' && (num < 100 || num > 10000)) return false;
    return true;
  }

  // --- Receiver Lookup ---
  lookupReceiver(): void {
    const id = this.targetUserId.trim();
    this.receiverName = '';
    this.receiverError = '';
    if (!id) return;

    // Don't look up yourself
    if (this.user && id === String(this.user.id)) {
      this.receiverError = 'Cannot transfer to yourself';
      return;
    }

    this.lookingUpReceiver = true;
    this.api.getUserName(id).subscribe({
      next: (res: any) => {
        this.receiverName = res.fullName;
        this.receiverError = '';
        this.lookingUpReceiver = false;
      },
      error: () => {
        this.receiverName = '';
        this.receiverError = 'User not found';
        this.lookingUpReceiver = false;
      }
    });
  }

  // --- Transfer Confirmation Flow ---
  showTransferConfirm() {
    this.validateAmount();
    if (this.amountError || !this.transactionAmount) return;
    if (!this.targetUserId || !this.receiverName) return;
    this.modalType = 'TRANSFER_CONFIRM';
    this.modalTitle = 'Confirm Transfer';
  }

  backToTransfer() {
    this.modalType = 'TRANSFER';
    this.modalTitle = 'TRANSFER';
  }

  // Submit Transaction
  onSubmit() {
    if (this.modalType === 'MESSAGE' || this.modalType === 'SUCCESS' || this.modalType === 'FAILURE') {
      this.closeModal();
      return;
    }

    // For TRANSFER_CONFIRM, skip validation (already validated)
    if (this.modalType !== 'TRANSFER_CONFIRM') {
      this.validateAmount();
      if (this.amountError || !this.transactionAmount || this.transactionAmount <= 0) {
        return;
      }
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
    else if (this.modalType === 'TRANSFER' || this.modalType === 'TRANSFER_CONFIRM') {
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