import { Component } from '@angular/core';
import {NgForOf, NgOptimizedImage} from '@angular/common';

interface Testimonial {
  name: string;
  position: string;
  content: string;
  avatar: string;
  rating: number;
}

@Component({
  selector: 'app-testimonials',
  templateUrl: './testimonials.component.html',
  standalone: true,
  imports: [
    NgForOf,
    NgOptimizedImage
  ]
})
export class TestimonialsComponent {
  testimonials: Testimonial[] = [
    {
      name: 'Sarah Johnson',
      position: 'HR Manager at TechCorp',
      content: 'This platform has completely transformed our hiring process. The AI-driven insights have helped us make better decisions.',
      avatar: '/api/placeholder/48/48',
      rating: 5
    },
    {
      name: 'Michael Anderson',
      position: 'Recruiting Lead',
      content: 'Efficient and accurate. We\'ve reduced our hiring time by 40% while improving the quality of our hires.',
      avatar: '/api/placeholder/48/48',
      rating: 5
    },
    {
      name: 'Emily Collins',
      position: 'Talent Acquisition',
      content: 'The behavioral analysis features provide incredible insights that we wouldn\'t catch in traditional interviews.',
      avatar: '/api/placeholder/48/48',
      rating: 5
    }
  ];
}

