import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import './App.css';
import './i18n'; // Initialize i18n
import LoginPage from './components/Auth/LoginPage';
import RegisterPage from './components/Auth/RegisterPage';
import Chat from './components/Chat/Chat';
import Navbar from './components/Common/Navbar';
import UserProfile from './components/Profile/UserProfile';
import type { User } from './types';

type Page = 'home' | 'login' | 'register' | 'chat';

function App() {
  const { t } = useTranslation();
  const [currentPage, setCurrentPage] = useState<Page>('home');
  const [user, setUser] = useState<User | null>(null);
  const [showProfile, setShowProfile] = useState(false);

  // Tema durumu (light/dark) ve kalıcılık
  const [theme, setTheme] = useState<'light' | 'dark'>(() => {
    const saved = localStorage.getItem('theme');
    if (saved === 'light' || saved === 'dark') return saved;
    const prefersDark = window.matchMedia?.('(prefers-color-scheme: dark)')
      ? window.matchMedia('(prefers-color-scheme: dark)').matches
      : false;
    return prefersDark ? 'dark' : 'light';
  });

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
  }, [theme]);

  const toggleTheme = () => setTheme((prev) => (prev === 'light' ? 'dark' : 'light'));

  const handleLogin = (user: User) => {
    setUser(user);
    setCurrentPage('chat');
  };

  const handleLogout = () => {
    setUser(null);
    setCurrentPage('home');
  };

  const handleLoginClick = () => {
    setCurrentPage('login');
  };

  const handleRegisterClick = () => {
    setCurrentPage('register');
  };

  const handleBack = () => {
    setCurrentPage('home');
  };

  const renderPage = () => {
    switch (currentPage) {
      case 'login':
        return <LoginPage onLogin={handleLogin} onBack={handleBack} />;
      case 'register':
        return <RegisterPage onLogin={handleLogin} onBack={handleBack} />;
      case 'chat':
        return <Chat user={user!} onLogout={handleLogout} />;
      default:
        return (
          <div className="home-page">
            <div className="hero-section">
              <h1>{t('home.heroTitle')}</h1>
              <p>{t('home.heroSubtitle')}</p>
              <div className="hero-buttons">
                <button onClick={handleLoginClick} className="hero-btn primary">
                  {t('home.loginButton')}
                </button>
                <button onClick={handleRegisterClick} className="hero-btn secondary">
                  {t('home.registerButton')}
                </button>
              </div>
            </div>
          </div>
        );
    }
  };

  return (
    <div className={`app ${theme}`}>
      <Navbar
        onLoginClick={handleLoginClick}
        onRegisterClick={handleRegisterClick}
        isLoggedIn={!!user}
        onLogout={handleLogout}
        userName={user?.firstName}
        onProfileClick={user ? () => setShowProfile(true) : undefined}
        userId={user?.id || undefined}
        theme={theme}
        onToggleTheme={toggleTheme}
      />
      <main className="main-content">
        {renderPage()}
      </main>
      
      {showProfile && user && user.id && (
        <UserProfile
          userId={user.id}
          onClose={() => setShowProfile(false)}
        />
      )}
    </div>
  );
}

export default App;
