import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import {AppComponent} from './src/app/app.component';
import {LoginComponent} from './src/app/components/auth/login/login.component';
import {RegisterComponent} from './src/app/components/auth/register/register.component';
import {DashboardComponent} from './src/app/components/dashboard/dashboard.component';
import {ResetPasswordComponent} from './src/app/components/auth/reset-password/reset-password.component';
import {ProfileComponent} from './src/app/components/profile/profile.component';
import {AuthInterceptor} from './src/app/interceptors/auth.interceptor';
import {AuthService} from './src/app/services/auth.service';

@NgModule({
  declarations: [

  ],
  imports: [
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    RouterModule,
    AppComponent,
    RegisterComponent,
    ResetPasswordComponent,
    ProfileComponent,
    LoginComponent,
    DashboardComponent,
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
    AuthService
  ]})
export class AppModule { }
