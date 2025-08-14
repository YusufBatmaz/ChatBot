import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import axios from 'axios';
import './AuthPages.css';
import type { User } from '../../types';

interface LoginPageProps {
  onLogin: (user: User) => void;
  onBack: () => void;
}

export default function LoginPage({ onLogin, onBack }: LoginPageProps) {
  const { t } = useTranslation();
  const [form, setForm] = useState({
    email: '',
    password: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    
    if (!form.email || !form.password) {
      setError(t('auth.error.requiredFields'));
      return;
    }

    setLoading(true);
    try {
      const response = await axios.post<User>('/api/users/login', {
        email: form.email,
        password: form.password
      });
      onLogin(response.data);
    } catch (err: any) {
      if (err.response?.status === 401) {
        setError(t('auth.error.invalidCredentials'));
      } else {
        setError(t('auth.error.loginFailed'));
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-container">
        <div className="auth-header">
          <button onClick={onBack} className="back-btn">
            ← {t('common.back')}
          </button>
          <h2>{t('auth.loginTitle')}</h2>
        </div>
        
        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label htmlFor="email">{t('auth.email')}</label>
            <input
              type="email"
              id="email"
              name="email"
              value={form.email}
              onChange={handleChange}
              placeholder={t('auth.email')}
              disabled={loading}
              required
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="password">{t('auth.password')}</label>
            <input
              type="password"
              id="password"
              name="password"
              value={form.password}
              onChange={handleChange}
              placeholder={t('auth.password')}
              disabled={loading}
              required
            />
          </div>
          
          <button type="submit" className="submit-btn" disabled={loading}>
            {loading ? t('common.loading') : t('auth.loginButton')}
          </button>
          
          {error && <div className="error-message">{error}</div>}
        </form>
      </div>
    </div>
  );
}