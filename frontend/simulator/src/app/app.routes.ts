import {Routes} from '@angular/router';
import {LoginComponent} from './components/auth/login/login.component';
import {TestimonialsComponent} from './components/testimonials/testimonials.component';
import {FeaturesComponent} from './components/features/features.component';
import {DashboardComponent} from './components/dashboard/dashboard.component';
import {AuthGuard} from './models/guards/auth-guard.guard';
import {RegisterComponent} from './components/auth/register/register.component';
import {HomeComponent} from './components/home/home.component';
import {ProfileComponent} from './components/profile/profile.component';
import {InterviewPopupComponent} from './components/interview-popup-component/interview-popup-component.component';
import {InterviewDetailComponent} from './components/interview-detail/interview-detail.component';
import {AdminComponent} from './components/admin/admin.component';
import {ChangePasswordComponent} from './components/change-password/change-password.component';
import {RoleGuard} from './models/guards/role-guard.guard';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'features', component: FeaturesComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'testimonials', component: TestimonialsComponent },
  { path: 'change-password', component: ChangePasswordComponent },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthGuard]
  },
  { path: 'login', component: LoginComponent },
  { path: 'profile', component: ProfileComponent ,    canActivate: [AuthGuard] },
  { path: 'interview/setup', component: InterviewPopupComponent },
  { path: 'interviews/:id', component: InterviewDetailComponent },
  {
    path: 'admin',
    component: AdminComponent,
    canActivate: [RoleGuard],
    data: { role: 'ADMIN' }
  },  { path: '**', redirectTo: '' }
];
