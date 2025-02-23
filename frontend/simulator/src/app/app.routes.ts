import { Routes } from '@angular/router';
import {LoginComponent} from './components/auth/login/login.component';
import {TestimonialsComponent} from './components/testimonials/testimonials.component';
import {FeaturesComponent} from './components/features/features.component';
import {AppComponent} from './app.component';
import {DashboardComponent} from './components/dashboard/dashboard.component';
import {AuthGuard} from './models/guards/auth-guard.guard';
import {RegisterComponent} from './components/auth/register/register.component';
import {HomeComponent} from './components/home/home.component';

export const routes: Routes = [

  { path: '', component: HomeComponent },
  { path: 'features', component: FeaturesComponent },
  {path:'register',component:RegisterComponent},
  { path: 'testimonials', component: TestimonialsComponent },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthGuard]
  },  { path: 'login', component: LoginComponent },
  { path: '**', redirectTo: '' }
];
