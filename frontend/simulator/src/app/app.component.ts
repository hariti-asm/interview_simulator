import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {HeaderComponent} from './components/header/header.component';
import {HeroComponent} from './components/hero/hero.component';
import {FeaturesComponent} from './components/features/features.component';
import {StatsComponent} from './components/stats/stats.component';
import {TestimonialsComponent} from './components/testimonials/testimonials.component';
import {FooterComponent} from './components/footer/footer.component';
import {CtaComponent} from './components/cta/cta.component';
import {UsageComponent} from './components/usage/usage.component';
import {LoginComponent} from './components/auth/login/login.component';
import {ForgetPasswordComponent} from './components/auth/forget-password/forget-password.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, HeaderComponent, HeroComponent, FeaturesComponent, StatsComponent, TestimonialsComponent, FooterComponent, CtaComponent, UsageComponent,ForgetPasswordComponent],
  templateUrl: './app.component.html',
  standalone: true,
  styleUrl: './app.component.css'
})
export class AppComponent {
  seo="seo.png";

  title = 'simulator';
}
