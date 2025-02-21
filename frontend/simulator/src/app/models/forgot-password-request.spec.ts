import { ForgotPasswordRequest } from './forgot-password-request';

describe('ForgotPasswordRequest', () => {
  it('should create a valid object', () => {
    const request: ForgotPasswordRequest = { email: 'test@example.com' };
    expect(request).toBeTruthy();
  });
});
