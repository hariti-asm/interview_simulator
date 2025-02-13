import { Component } from '@angular/core';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  standalone: true
})
export class FooterComponent {
  currentYear = new Date().getFullYear();

  footerLinks = {
    company: [
      { label: 'About Us', href: '#' }
    ]
      };}
