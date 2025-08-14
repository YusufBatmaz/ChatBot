import { useTranslation } from 'react-i18next';
import './Navbar.css';
import LanguageSelector from './LanguageSelector';

interface NavbarProps {
  onLoginClick: () => void;
  onRegisterClick: () => void;
  isLoggedIn: boolean;
  onLogout: () => void;
  userName?: string;
  onProfileClick?: () => void;
  userId?: string; // pass to LanguageSelector for persisting language
  theme?: 'light' | 'dark';
  onToggleTheme?: () => void;
}

export default function Navbar({ onLoginClick, onRegisterClick, isLoggedIn, onLogout, userName, onProfileClick, userId, theme = 'light', onToggleTheme }: NavbarProps) {
  const { t } = useTranslation();
  
  return (
    <nav className="navbar">
      <div className="navbar-left">
        <div className="logo">
          <span className="logo-icon">🤖</span>
          <span className="logo-text">ChatBot</span>
        </div>
      </div>
      
      <div className="navbar-right">
        <button
          type="button"
          className="theme-toggle"
          aria-label={theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}
          title={theme === 'dark' ? 'Light' : 'Dark'}
          onClick={onToggleTheme}
        >
          {theme === 'dark' ? '🌙' : '☀️'}
        </button>
        <LanguageSelector userId={isLoggedIn ? userId : undefined} />
        
        {isLoggedIn ? (
          <div className="user-section">
            <span className="welcome-text">{t('common.welcome')}, {userName}!</span>
            {onProfileClick && (
              <button onClick={onProfileClick} className="profile-btn">
                {t('profile.title')}
              </button>
            )}
            <button onClick={onLogout} className="logout-btn">
              {t('common.logout')}
            </button>
          </div>
        ) : (
          <div className="auth-buttons">
            <button onClick={onLoginClick} className="login-btn">
              {t('common.login')}
            </button>
            <button onClick={onRegisterClick} className="register-btn">
              {t('common.register')}
            </button>
          </div>
        )}
      </div>
    </nav>
  );
} 