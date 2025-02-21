import {Role} from './role.enum';

export interface RegisterRequest {
  email: string;
  name:string;
  password: string;
  role:Role;
  rememberMe:boolean
}
