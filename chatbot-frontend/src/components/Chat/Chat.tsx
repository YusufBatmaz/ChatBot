import { useState } from 'react';
import axios from 'axios';
import './Chat.css';
import type { User, Message } from '../../types';

interface ChatProps {
  user: User;
  onLogout: () => void;
}

export default function Chat({ user, onLogout }: ChatProps) {
  const [input, setInput] = useState('');
  const [messages, setMessages] = useState<Message[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim()) return;

    const userMessage: Message = { sender: 'user', content: input };
    setMessages((prev) => [...prev, userMessage]);
    setInput('');
    setIsLoading(true);

    try {
      const response = await axios.post(
        `http://localhost:8080/api/chat?userId=${user.id}`,
        { message: input }
      );
      const botMessage: Message = { sender: 'bot', content: response.data };
      setMessages((prev) => [...prev, botMessage]);
    } catch (error) {
      console.error('API error:', error);
      const errorMessage: Message = {
        sender: 'bot',
        content: 'Sunucuya bağlanırken bir hata oluştu.'
      };
      setMessages((prev) => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="chat-container">
      <header className="chat-header">
        <h2>Hoşgeldin, {user.firstName}!</h2>
        <button onClick={onLogout} className="logout-button">Çıkış Yap</button>
      </header>

      <div className="chat-history">
        {messages.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-icon">💬</div>
            <div className="empty-state-text">Henüz mesaj yok, başlayalım!</div>
          </div>
        ) : (
          <div className="messages-container">
            <div className="user-column">
              <h3>Senin Mesajların</h3>
              {messages
                .filter(msg => msg.sender === 'user')
                .map((msg, index) => (
                  <div key={index} className="message user-message">
                    <div className="message-content">{msg.content}</div>
                  </div>
                ))}
            </div>
            <div className="bot-column">
              <h3>Bot Cevapları</h3>
              {messages
                .filter(msg => msg.sender === 'bot')
                .map((msg, index) => (
                  <div key={index} className="message bot-message">
                    <div className="message-content">{msg.content}</div>
                  </div>
                ))}
            </div>
          </div>
        )}

        {isLoading && (
          <div className="bot-column">
            <div className="message bot-message">
              <div className="typing-indicator">
                <div className="typing-dot"></div>
                <div className="typing-dot"></div>
                <div className="typing-dot"></div>
              </div>
            </div>
          </div>
        )}
      </div>

      <form className="chat-form" onSubmit={handleSubmit}>
        <input
          type="text"
          className="chat-input"
          placeholder="Mesajını yaz..."
          value={input}
          onChange={(e) => setInput(e.target.value)}
          disabled={isLoading}
        />
        <button type="submit" className="send-button" disabled={isLoading || !input.trim()}>
          {isLoading ? 'Gönderiliyor...' : 'Gönder'}
        </button>
      </form>
    </div>
  );
}

