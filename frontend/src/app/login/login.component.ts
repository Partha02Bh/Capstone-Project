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

  constructor(private api: ApiService, private router: Router){}

  onLogin(){

    this.api.login(this.username, this.password).subscribe({
      next: (res: any)=>{
        alert("OTP Generated. Check Backend Console.");
        this.step = 2;
      },
      error: () => alert("Invalid Credentials")
    });
  }

  onVerify(){

    this.api.verifyOtp(this.username, this.otp).subscribe({
      next: (res: any)=>{
        

        localStorage.setItem('token', res.token);
        localStorage.setItem('role', res.role);
        localStorage.setItem('userId', res.userId);


        if(res.role === 'ROLE_ADMIN' || res.role === 'ADMIN'){
          this.router.navigate(['/owner']);
        } else{
          this.router.navigate(['/dashboard']);
        }
      },
      error: () => alert("Invalid OTP")
    });
  }
}