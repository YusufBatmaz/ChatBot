import { useState } from 'react';
import './App.css';
import LoginPage from './components/Auth/LoginPage';
import RegisterPage from './components/Auth/RegisterPage';
import Chat from './components/Chat/Chat';
import Navbar from './components/Common/Navbar';
import type { User } from './types';

type Page = 'home' | 'login' | 'register' | 'chat';

function App() {
  const [currentPage, setCurrentPage] = useState<Page>('home');
  const [user, setUser] = useState<User | null>(null);

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
              <h1>ChatBot'a Hoşgeldiniz</h1>
              <p>Yapay zeka destekli chatbot ile sohbet etmeye başlayın</p>
              <div className="hero-buttons">
                <button onClick={handleLoginClick} className="hero-btn primary">
                  Giriş Yap
                </button>
                <button onClick={handleRegisterClick} className="hero-btn secondary">
                  Kayıt Ol
                </button>
              </div>
            </div>
          </div>
        );
    }
  };

  return (
    <div className="app">
      <Navbar
        onLoginClick={handleLoginClick}
        onRegisterClick={handleRegisterClick}
        isLoggedIn={!!user}
        onLogout={handleLogout}
        userName={user?.firstName}
      />
      <main className="main-content">
        {renderPage()}
      </main>
    </div>
  );
}

export default App;
