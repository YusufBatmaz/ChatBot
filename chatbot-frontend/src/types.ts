export interface User {
  id: string | null;
  firstName: string;
  lastName: string;
  email: string;
}

export interface Message {
  sender: 'user' | 'bot';
  content: string;
}
