import {Role} from './role.enum';

export interface UserResponse {
  id: number;
  name: string;
  email: string;
  role: Role;
}
