import { Component } from '@angular/core';
import { ApiService } from '../api.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  username = '';
  password = '';
  fullName = '';
  email = '';
  phone = '';
  countryCode = '+91';

  // Validation error messages
  errors: { [key: string]: string } = {};

  // Modal State
  showModal = false;
  modalType: 'success' | 'error' = 'success';
  modalTitle = '';
  modalMessage = '';

  countryCodes = [
    { code: '+93', label: '🇦🇫 +93 AF' },
    { code: '+355', label: '🇦🇱 +355 AL' },
    { code: '+213', label: '🇩🇿 +213 DZ' },
    { code: '+54', label: '🇦🇷 +54 AR' },
    { code: '+61', label: '🇦🇺 +61 AU' },
    { code: '+43', label: '🇦🇹 +43 AT' },
    { code: '+880', label: '🇧🇩 +880 BD' },
    { code: '+32', label: '🇧🇪 +32 BE' },
    { code: '+55', label: '🇧🇷 +55 BR' },
    { code: '+1', label: '🇨🇦 +1 CA' },
    { code: '+56', label: '🇨🇱 +56 CL' },
    { code: '+86', label: '🇨🇳 +86 CN' },
    { code: '+57', label: '🇨🇴 +57 CO' },
    { code: '+45', label: '🇩🇰 +45 DK' },
    { code: '+20', label: '🇪🇬 +20 EG' },
    { code: '+358', label: '🇫🇮 +358 FI' },
    { code: '+33', label: '🇫🇷 +33 FR' },
    { code: '+49', label: '🇩🇪 +49 DE' },
    { code: '+233', label: '🇬🇭 +233 GH' },
    { code: '+30', label: '🇬🇷 +30 GR' },
    { code: '+852', label: '🇭🇰 +852 HK' },
    { code: '+36', label: '🇭🇺 +36 HU' },
    { code: '+91', label: '🇮🇳 +91 IN' },
    { code: '+62', label: '🇮🇩 +62 ID' },
    { code: '+98', label: '🇮🇷 +98 IR' },
    { code: '+964', label: '🇮🇶 +964 IQ' },
    { code: '+353', label: '🇮🇪 +353 IE' },
    { code: '+972', label: '🇮🇱 +972 IL' },
    { code: '+39', label: '🇮🇹 +39 IT' },
    { code: '+81', label: '🇯🇵 +81 JP' },
    { code: '+254', label: '🇰🇪 +254 KE' },
    { code: '+82', label: '🇰🇷 +82 KR' },
    { code: '+60', label: '🇲🇾 +60 MY' },
    { code: '+52', label: '🇲🇽 +52 MX' },
    { code: '+212', label: '🇲🇦 +212 MA' },
    { code: '+95', label: '🇲🇲 +95 MM' },
    { code: '+977', label: '🇳🇵 +977 NP' },
    { code: '+31', label: '🇳🇱 +31 NL' },
    { code: '+64', label: '🇳🇿 +64 NZ' },
    { code: '+234', label: '🇳🇬 +234 NG' },
    { code: '+47', label: '🇳🇴 +47 NO' },
    { code: '+92', label: '🇵🇰 +92 PK' },
    { code: '+63', label: '🇵🇭 +63 PH' },
    { code: '+48', label: '🇵🇱 +48 PL' },
    { code: '+351', label: '🇵🇹 +351 PT' },
    { code: '+974', label: '🇶🇦 +974 QA' },
    { code: '+7', label: '🇷🇺 +7 RU' },
    { code: '+966', label: '🇸🇦 +966 SA' },
    { code: '+65', label: '🇸🇬 +65 SG' },
    { code: '+27', label: '🇿🇦 +27 ZA' },
    { code: '+34', label: '🇪🇸 +34 ES' },
    { code: '+94', label: '🇱🇰 +94 LK' },
    { code: '+46', label: '🇸🇪 +46 SE' },
    { code: '+41', label: '🇨🇭 +41 CH' },
    { code: '+886', label: '🇹🇼 +886 TW' },
    { code: '+66', label: '🇹🇭 +66 TH' },
    { code: '+90', label: '🇹🇷 +90 TR' },
    { code: '+380', label: '🇺🇦 +380 UA' },
    { code: '+971', label: '🇦🇪 +971 AE' },
    { code: '+44', label: '🇬🇧 +44 GB' },
    { code: '+1', label: '🇺🇸 +1 US' },
    { code: '+84', label: '🇻🇳 +84 VN' }
  ];

  constructor(private api: ApiService, private router: Router) { }

  // --- Validation Methods ---

  validateFullName(): boolean {
    if (!this.fullName.trim()) {
      this.errors['fullName'] = 'Full name is required';
      return false;
    }
    if (this.fullName.length > 25) {
      this.errors['fullName'] = 'Full name must be at most 25 characters';
      return false;
    }
    if (!/^[a-zA-Z ]+$/.test(this.fullName)) {
      this.errors['fullName'] = 'Full name must contain only letters and spaces';
      return false;
    }
    delete this.errors['fullName'];
    return true;
  }

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

  validateEmail(): boolean {
    if (!this.email.trim()) {
      this.errors['email'] = 'Email is required';
      return false;
    }
    if (this.email.length > 25) {
      this.errors['email'] = 'Email must be at most 25 characters';
      return false;
    }
    if (!this.email.includes('@') || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.email)) {
      this.errors['email'] = 'Please enter a valid email address';
      return false;
    }
    delete this.errors['email'];
    return true;
  }

  validatePhone(): boolean {
    if (!this.phone.trim()) {
      this.errors['phone'] = 'Phone number is required';
      return false;
    }
    if (!/^\d{10}$/.test(this.phone)) {
      this.errors['phone'] = 'Phone number must be exactly 10 digits';
      return false;
    }
    delete this.errors['phone'];
    return true;
  }

  validateAll(): boolean {
    const results = [
      this.validateFullName(),
      this.validateUsername(),
      this.validatePassword(),
      this.validateEmail(),
      this.validatePhone()
    ];
    return results.every(r => r);
  }

  // Check validity without setting error messages (for button state)
  get isFormValid(): boolean {
    const nameOk = this.fullName.trim().length > 0 && this.fullName.length <= 25 && /^[a-zA-Z ]+$/.test(this.fullName);
    const userOk = this.username.trim().length > 0 && this.username.length <= 25 && /^[a-zA-Z]+$/.test(this.username);
    const passOk = this.password.length >= 6 && this.password.length <= 10;
    const emailOk = this.email.trim().length > 0 && this.email.length <= 25 && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.email);
    const phoneOk = /^\d{10}$/.test(this.phone);
    return nameOk && userOk && passOk && emailOk && phoneOk;
  }

  register() {
    if (!this.validateAll()) {
      return; // Stop if any validation fails
    }

    const fullPhone = this.countryCode + this.phone;

    this.api.register(this.username, this.password, this.fullName, this.email, fullPhone)
      .subscribe({
        next: (res) => {
          this.openModal('success', 'Registration Successful!', 'Your account has been created successfully. You will be redirected to the login page.');
        },
        error: (err) => {
          // Handle backend validation errors
          if (err.error?.errors) {
            this.errors = err.error.errors;
          } else {
            const rawMsg = err.error?.message || err.error || 'Unknown error';
            const friendlyMsg = this.sanitizeErrorMessage(rawMsg);
            this.openModal('error', 'Registration Failed', friendlyMsg);
          }
        }
      });
  }

  goToLogin() {
    this.router.navigate(['/']);
  }

  openModal(type: 'success' | 'error', title: string, message: string) {
    this.modalType = type;
    this.modalTitle = title;
    this.modalMessage = message;
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    if (this.modalType === 'success') {
      this.router.navigateByUrl('/login');
    }
  }

  sanitizeErrorMessage(raw: string): string {
    if (typeof raw !== 'string') return 'Something went wrong. Please try again.';

    const lower = raw.toLowerCase();

    if (lower.includes('duplicate entry') && lower.includes('email')) {
      return 'This email address is already registered. Please use a different email or try logging in.';
    }
    if (lower.includes('duplicate entry') && lower.includes('username')) {
      return 'This username is already taken. Please choose a different username.';
    }
    if (lower.includes('duplicate entry') || lower.includes('unique constraint') || lower.includes('constraint')) {
      return 'An account with these details already exists. Please use different credentials.';
    }
    if (lower.includes('connection') || lower.includes('timeout') || lower.includes('refused')) {
      return 'Unable to reach the server. Please check your connection and try again.';
    }
    if (lower.includes('could not execute') || lower.includes('sql') || lower.includes('hibernate')) {
      return 'An account with these details already exists. Please use different credentials.';
    }
    if (raw.length > 100) {
      return 'Something went wrong during registration. Please try again with different details.';
    }

    return raw;
  }
}