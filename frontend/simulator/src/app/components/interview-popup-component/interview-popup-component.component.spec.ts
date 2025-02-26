import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InterviewPopupComponentComponent } from './interview-popup-component.component';

describe('InterviewPopupComponentComponent', () => {
  let component: InterviewPopupComponentComponent;
  let fixture: ComponentFixture<InterviewPopupComponentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InterviewPopupComponentComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InterviewPopupComponentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
