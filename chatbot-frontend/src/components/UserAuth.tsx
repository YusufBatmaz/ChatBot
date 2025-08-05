import { useState } from "react";
import axios from "axios";
import "../App.css";
import type { User } from "../types";

interface UserForm {
  firstName: string;
  lastName: string;
  email: string;
}

interface Props {
  onLogin: (user: User) => void;
}

const UserAuth = ({ onLogin }: Props) => {
  const [form, setForm] = useState<UserForm>({
    firstName: "",
    lastName: "",
    email: "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleRegister = async () => {
    setError("");
    if (!form.firstName || !form.lastName || !form.email) {
      setError("Tüm alanları doldurun.");
      return;
    }
    setLoading(true);
    try {
      const response = await axios.post<User>("http://localhost:8080/api/users/register", form);
      onLogin(response.data);
    } catch (err: any) {
      if (err.response && err.response.status === 409) {
        setError("Bu email ile zaten kayıtlı bir kullanıcı var.");
      } else {
        setError("Kullanıcı kaydı sırasında hata oluştu.");
      }
    } finally {
      setLoading(false);
    }
  };

  const handleLogin = async () => {
    setError("");
    if (!form.email) {
      setError("Email alanı zorunlu.");
      return;
    }
    setLoading(true);
    try {
      const response = await axios.post<User>("http://localhost:8080/api/users/login", { email: form.email });
      onLogin(response.data);
    } catch (err: any) {
      setError("Giriş başarısız. Email yanlış veya kayıtlı değil.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <h2>Kullanıcı Girişi / Kayıt</h2>
      <form className="auth-form" onSubmit={e => e.preventDefault()}>
        <input
          type="text"
          name="firstName"
          placeholder="İsim"
          value={form.firstName}
          onChange={handleChange}
          disabled={loading}
        />
        <input
          type="text"
          name="lastName"
          placeholder="Soyisim"
          value={form.lastName}
          onChange={handleChange}
          disabled={loading}
        />
        <input
          type="email"
          name="email"
          placeholder="Email"
          value={form.email}
          onChange={handleChange}
          disabled={loading}
        />
        <div style={{ display: "flex", gap: "10px", marginTop: "20px" }}>
          <button type="button" onClick={handleLogin} disabled={loading}>
            Giriş Yap
          </button>
          <button type="button" onClick={handleRegister} disabled={loading}>
            Kaydol
          </button>
        </div>
        {error && <p className="error-text">{error}</p>}
      </form>
    </div>
  );
};

export default UserAuth;
