import {Role} from './role.enum';

export interface User {
  id: number;
  name: string;
  email: string;
  role: Role;
}
