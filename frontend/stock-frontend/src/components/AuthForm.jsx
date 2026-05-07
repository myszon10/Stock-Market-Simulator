// src/components/AuthForm.jsx
import { useState } from 'react';
import { loginUser, registerUser } from '../api/auth';
import './AuthForm.css';

export default function AuthForm({ onAuthSuccess }) {
  const [isLoginMode, setIsLoginMode] = useState(true);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      let userData;
      if (isLoginMode) {
        userData = await loginUser(username, password);
      } else {
        userData = await registerUser(username, password);
      }
      
      // Przekazanie danych użytkownika wyżej (np. do zapisu w kontekście)
      onAuthSuccess(userData);
      
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-wrapper">
        <div className="auth-header">
          <h1 className="auth-title">Stock Market Simulator</h1>
          <p className="auth-subtitle">Zarabiaj na wirtualnych akcjach</p>
        </div>

        <div className="auth-card">
          <h2 className="auth-card-title">
            {isLoginMode ? 'Zaloguj się' : 'Utwórz nowe konto'}
          </h2>

          {error && (
            <div className="error-message">
              <span>{error}</span>
            </div>
          )}

          <form className="auth-form" onSubmit={handleSubmit}>
            <div className="input-group">
              <label htmlFor="username">
                <span className="label-text">Nazwa użytkownika</span>
              </label>
              <input
                type="text"
                id="username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Wpisz swoją nazwę użytkownika"
                required
              />
            </div>

            <div className="input-group">
              <label htmlFor="password">
                <span className="label-text">Hasło</span>
              </label>
              <input
                type="password"
                id="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Wpisz swoje hasło"
                required
              />
            </div>

            <button type="submit" className="submit-btn" disabled={isLoading}>
              {isLoading ? (
                <>
                  <span className="spinner"></span>
                  Przetwarzanie...
                </>
              ) : (
                <span>{isLoginMode ? 'Zaloguj' : 'Zarejestruj'}</span>
              )}
            </button>
          </form>

          <div className="toggle-mode">
            <p className="toggle-text">
              {isLoginMode ? "Nie masz konta? " : "Masz już konto? "}
              <span
                className="toggle-link"
                onClick={() => {
                  setIsLoginMode(!isLoginMode);
                  setError('');
                  setUsername('');
                  setPassword('');
                }}
              >
                {isLoginMode ? 'Zarejestruj się' : 'Zaloguj się'}
              </span>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}