import {ComponentFixture, TestBed} from '@angular/core/testing';

import {InterviewDetailComponent} from './interview-detail.component';

describe('InterviewDetailComponent', () => {
  let component: InterviewDetailComponent;
  let fixture: ComponentFixture<InterviewDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InterviewDetailComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(InterviewDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
