import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {HeaderComponent} from './components/header/header.component';
import {HeroComponent} from './components/hero/hero.component';
import {FeaturesComponent} from './components/features/features.component';
import {StatsComponent} from './components/stats/stats.component';
import {TestimonialsComponent} from './components/testimonials/testimonials.component';
import {FooterComponent} from './components/footer/footer.component';
import {CtaComponent} from './components/cta/cta.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, HeaderComponent, HeroComponent, FeaturesComponent, StatsComponent, TestimonialsComponent, FooterComponent, CtaComponent],
  templateUrl: './app.component.html',
  standalone: true,
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'simulator';
}
