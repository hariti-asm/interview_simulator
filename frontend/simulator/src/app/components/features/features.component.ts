import { Component } from '@angular/core';

interface Feature {
  title: string;
  description: string;
  icon: string;
}

@Component({
  selector: 'app-features',
  templateUrl: './features.component.html',
  standalone: true
})
export class FeaturesComponent {
  features: Feature[] = [
    {
      title: 'AI-Driven Assessments',
      description: 'Advanced algorithms assess skills analytics and provide accurate candidate evaluation.',
      icon: 'lightning'
    },
    {
      title: 'Behavioral Analysis',
      description: 'Analyze candidate behavior, body language, and verbal responses in real-time.',
      icon: 'clipboard'
    },
    {
      title: 'Real-Time Feedback',
      description: 'Get instant performance feedback during interviews to make better decisions.',
      icon: 'chat'
    }
  ];
}
