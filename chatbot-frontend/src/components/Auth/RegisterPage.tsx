// Bu dosya, chatbot projesinin bir parçasıdır.
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import axios from 'axios';
import './AuthPages.css';
import type { User } from '../../types';

interface RegisterPageProps {
  onLogin: (user: User) => void;
  onBack: () => void;
}

export default function RegisterPage({ onLogin, onBack }: RegisterPageProps) {
  const { t } = useTranslation();
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    
    if (!form.firstName || !form.lastName || !form.email || !form.password || !form.confirmPassword) {
      setError(t('auth.error.requiredFields'));
      return;
    }

    if (form.password !== form.confirmPassword) {
      setError(t('auth.error.passwordMismatch'));
      return;
    }

    if (form.password.length < 6) {
      setError(t('auth.error.passwordTooShort'));
      return;
    }

    setLoading(true);
    try {
      const response = await axios.post<User>('http://localhost:8080/api/users/register', {
        firstName: form.firstName,
        lastName: form.lastName,
        email: form.email,
        password: form.password
      });
      onLogin(response.data);
    } catch (err: any) {
      if (err.response?.status === 409) {
        setError(t('auth.error.emailExists'));
      } else {
        setError(t('auth.error.registerFailed'));
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
          <h2>{t('auth.registerTitle')}</h2>
        </div>
        
        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="firstName">{t('auth.firstName')}</label>
              <input
                type="text"
                id="firstName"
                name="firstName"
                value={form.firstName}
                onChange={handleChange}
                placeholder={t('auth.firstName')}
                disabled={loading}
                required
              />
            </div>
            
            <div className="form-group">
              <label htmlFor="lastName">{t('auth.lastName')}</label>
              <input
                type="text"
                id="lastName"
                name="lastName"
                value={form.lastName}
                onChange={handleChange}
                placeholder={t('auth.lastName')}
                disabled={loading}
                required
              />
            </div>
          </div>
          
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
          
          <div className="form-group">
            <label htmlFor="confirmPassword">{t('auth.confirmPassword')}</label>
            <input
              type="password"
              id="confirmPassword"
              name="confirmPassword"
              value={form.confirmPassword}
              onChange={handleChange}
              placeholder={t('auth.confirmPassword')}
              disabled={loading}
              required
            />
          </div>
          
          <button type="submit" className="submit-btn" disabled={loading}>
            {loading ? t('common.loading') : t('auth.registerButton')}
          </button>
          
          {error && <div className="error-message">{error}</div>}
        </form>
      </div>
    </div>
  );
} 