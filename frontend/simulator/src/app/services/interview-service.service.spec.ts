import { TestBed } from '@angular/core/testing';

import { InterviewServiceService } from './interview-service.service';

describe('InterviewServiceService', () => {
  let service: InterviewServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(InterviewServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
