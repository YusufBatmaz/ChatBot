import { useState } from 'react';
import './Login.css';

interface LoginProps {
  onLogin: (user: User) => void;
}

export interface User {
  id: string | null;
  firstName: string;
  lastName: string;
  email: string;
}

export default function Login({ onLogin }: LoginProps) {
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');

  const handleRegister = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    setMessage('');
    if (!firstName.trim() || !lastName.trim() || !email.trim()) {
      alert('Lütfen tüm alanları doldurun.');
      return;
    }
    try {
      const response = await fetch('http://localhost:8080/api/users/register', {
        method: 'POST',
        body: JSON.stringify({ email, firstName, lastName }),
        headers: { 'Content-Type': 'application/json' },
      });
      if (response.status === 409) {
        setMessage('Bu email ile daha önce kayıt oldunuz.');
      } else if (response.ok) {
        setMessage('Kayıt başarılı! Giriş yapabilirsiniz.');
      } else {
        setMessage('Kayıt sırasında bir hata oluştu.');
      }
    } catch (error) {
      setMessage('Sunucuya ulaşılamıyor.');
    }
  };

  const handleLogin = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    setMessage('');
    if (!email.trim()) {
      alert('Lütfen email adresinizi girin.');
      return;
    }
    try {
      const response = await fetch('http://localhost:8080/api/users/login', {
        method: 'POST',
        body: JSON.stringify({ email }),
        headers: { 'Content-Type': 'application/json' },
      });
      if (response.ok) {
        setMessage('Giriş başarılı!');
        onLogin({ id: null, firstName, lastName, email });
      } else {
        setMessage('Kullanıcı bulunamadı veya bilgiler yanlış.');
      }
    } catch (error) {
      setMessage('Sunucuya ulaşılamıyor.');
    }
  };

  return (
    <div className="login-container">
      <h2>Giriş veya Kayıt Ol</h2>
      <form className="login-form" onSubmit={(e) => e.preventDefault()}>
        <input
          type="text"
          placeholder="Adınız"
          value={firstName}
          onChange={(e) => setFirstName(e.target.value)}
          className="login-input"
          required
        />
        <input
          type="text"
          placeholder="Soyadınız"
          value={lastName}
          onChange={(e) => setLastName(e.target.value)}
          className="login-input"
          required
        />
        <input
          type="email"
          placeholder="Email adresiniz"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          className="login-input"
          required
        />
        <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
          <button type="button" className="login-button" onClick={handleLogin}>
            Giriş Yap
          </button>
          <button type="button" className="register-button" onClick={handleRegister}>
            Kaydol
          </button>
        </div>
      </form>
      {message && <div className="message">{message}</div>}
    </div>
  );
}