import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {HomeComponent} from './components/home/home.component';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  standalone: true,
  imports: [
    RouterOutlet,
    HomeComponent
  ],
  styleUrl: './app.component.css'
})
export class AppComponent {
  seo="seo.png";

  title = 'simulator';
}
