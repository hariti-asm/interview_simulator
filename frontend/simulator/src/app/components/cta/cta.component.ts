// cta.component.ts
import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgOptimizedImage } from '@angular/common';

interface ScoreMap {
  [key: string]: number;
}

@Component({
  selector: 'app-cta',
  templateUrl: './cta.component.html',
  standalone: true,
  imports: [CommonModule, NgOptimizedImage]
})
export class CtaComponent implements OnInit {
  seo="seo.png";

  readonly circumference = 2 * Math.PI * 28;

  scores: ScoreMap = {
    professionalism: 80,
    businessAcumen: 90,
    opportunistic: 65,
    closingTechnique: 85
  };

  ngOnInit(): void {
  }

  calculateOffset(percentage: number): number {
    return this.circumference - (this.circumference * percentage) / 100;
  }

  formatLabel(key: string): string {
    // Convert camelCase to Title Case with spaces
    return key
      .replace(/([A-Z])/g, ' $1')
      .replace(/^./, (str) => str.toUpperCase());
  }
}
