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


constructor(private api: ApiService, private router: Router) {} 


register() {
  this.api.register(this.username, this.password, this.fullName, this.email, this.phone)
    .subscribe({
      next: (res) => {
        alert('Registration Successful!');

        this.router.navigateByUrl('/login'); 
      },
      error: (err) => alert('Registration Failed: ' + err.error)
    });
}
  goToLogin() {
    this.router.navigate(['/']);
  }
}