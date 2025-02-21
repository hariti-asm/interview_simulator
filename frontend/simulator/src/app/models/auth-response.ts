import {UserResponse} from './user-response';

export interface AuthResponse {
  token: string;
  refreshToken: string;
  rememberMeToken: string;
  user: UserResponse;
}
