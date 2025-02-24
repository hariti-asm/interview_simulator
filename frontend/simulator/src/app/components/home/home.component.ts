import { Component } from '@angular/core';
import {HeroComponent} from '../hero/hero.component';
import {FeaturesComponent} from '../features/features.component';
import {UsageComponent} from '../usage/usage.component';
import {StatsComponent} from '../stats/stats.component';
import {TestimonialsComponent} from '../testimonials/testimonials.component';
import {CtaComponent} from '../cta/cta.component';
import {FooterComponent} from '../footer/footer.component';
import {HeaderComponent} from '../header/header.component';
import {RouterOutlet} from '@angular/router';

@Component({
  selector: 'app-home',
  imports: [
    RouterOutlet,
    HeroComponent,
    FeaturesComponent,
    UsageComponent,
    StatsComponent,
    TestimonialsComponent,
    CtaComponent,
    FooterComponent,
    HeaderComponent
  ],
  templateUrl: './home.component.html',
  standalone: true,
  styleUrl: './home.component.css'
})
export class HomeComponent {

}
