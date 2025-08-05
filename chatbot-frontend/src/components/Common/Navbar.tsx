import './Navbar.css';

interface NavbarProps {
  onLoginClick: () => void;
  onRegisterClick: () => void;
  isLoggedIn: boolean;
  onLogout: () => void;
  userName?: string;
}

export default function Navbar({ onLoginClick, onRegisterClick, isLoggedIn, onLogout, userName }: NavbarProps) {
  return (
    <nav className="navbar">
      <div className="navbar-left">
        <div className="logo">
          <span className="logo-icon">🤖</span>
          <span className="logo-text">ChatBot</span>
        </div>
      </div>
      
      <div className="navbar-right">
        {isLoggedIn ? (
          <div className="user-section">
            <span className="welcome-text">Hoşgeldin, {userName}!</span>
            <button onClick={onLogout} className="logout-btn">
              Çıkış Yap
            </button>
          </div>
        ) : (
          <div className="auth-buttons">
            <button onClick={onLoginClick} className="login-btn">
              Giriş Yap
            </button>
            <button onClick={onRegisterClick} className="register-btn">
              Kaydol
            </button>
          </div>
        )}
      </div>
    </nav>
  );
} 