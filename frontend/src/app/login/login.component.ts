import { Component } from '@angular/core';
import { ApiService } from '../api.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {

  username = '';
  password = '';
  otp = '';
  step = 1;

  // Validation errors
  errors: { [key: string]: string } = {};

  // Modal state
  showModal = false;
  modalTitle = '';
  modalMessage = '';
  modalType: 'error' | 'info' = 'error';

  constructor(private api: ApiService, private router: Router) { }

  // --- Validation ---
  validateUsername(): boolean {
    if (!this.username.trim()) {
      this.errors['username'] = 'Username is required';
      return false;
    }
    if (this.username.length > 25) {
      this.errors['username'] = 'Username must be at most 25 characters';
      return false;
    }
    if (!/^[a-zA-Z]+$/.test(this.username)) {
      this.errors['username'] = 'Username must contain only letters (no numbers or special characters)';
      return false;
    }
    delete this.errors['username'];
    return true;
  }

  validatePassword(): boolean {
    if (!this.password) {
      this.errors['password'] = 'Password is required';
      return false;
    }
    if (this.password.length < 6) {
      this.errors['password'] = 'Password must be at least 6 characters';
      return false;
    }
    if (this.password.length > 10) {
      this.errors['password'] = 'Password must be at most 10 characters';
      return false;
    }
    delete this.errors['password'];
    return true;
  }

  get isFormValid(): boolean {
    const userOk = this.username.trim().length > 0 && this.username.length <= 25 && /^[a-zA-Z]+$/.test(this.username);
    const passOk = this.password.length >= 6 && this.password.length <= 10;
    return userOk && passOk;
  }

  // --- Modal ---
  openModal(title: string, message: string, type: 'error' | 'info' = 'error') {
    this.modalTitle = title;
    this.modalMessage = message;
    this.modalType = type;
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
  }

  // --- Login & Verify ---
  onLogin() {
    if (!this.validateUsername() || !this.validatePassword()) {
      return;
    }

    this.api.login(this.username, this.password).subscribe({
      next: (res: any) => {
        this.openModal('OTP Sent ✉️', 'OTP Generated. Check Backend Console.', 'info');
        this.step = 2;
      },
      error: () => this.openModal('Login Failed', 'Invalid username or password. Please try again.')
    });
  }

  onVerify() {
    this.api.verifyOtp(this.username, this.otp).subscribe({
      next: (res: any) => {
        console.log('Login Response Full:', res);
        console.log('Role received:', res.role);

        localStorage.setItem('token', res.token);
        localStorage.setItem('role', res.role);
        localStorage.setItem('userId', res.userId);

        if (res.role === 'ROLE_ADMIN' || res.role === 'ADMIN') {
          console.log('Redirecting to OWNER (Admin Dashboard)');
          this.router.navigate(['/owner']);
        } else {
          console.log('Redirecting to DASHBOARD (User Dashboard)');
          this.router.navigate(['/dashboard']);
        }
      },
      error: () => this.openModal('Verification Failed', 'Invalid OTP. Please check the code and try again.')
    });
  }
}