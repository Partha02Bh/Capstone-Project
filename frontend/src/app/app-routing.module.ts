import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';


import { LandingComponent } from './landing/landing.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { OwnerComponent } from './owner/owner.component';

const routes: Routes = [
  // 1. DEFAULT ROUTE: Shows Landing Page when opening the app
  { path: '', component: LandingComponent },

  // 2. AUTHENTICATION ROUTES
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },

  // 3. USER DASHBOARD
  { path: 'dashboard', component: DashboardComponent },

  // 4. ADMIN DASHBOARD
  { path: 'owner', component: OwnerComponent },

  // 5. WILDCARD: Redirects any unknown URL back to the Landing Page
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }