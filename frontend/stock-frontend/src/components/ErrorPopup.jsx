import { useEffect } from 'react';
import './ErrorPopup.css';

export default function ErrorPopup({ message, onClose }) {
  useEffect(() => {
    if (!message) return;
    const onKey = (e) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [message, onClose]);

  if (!message) return null;

  return (
    <div className="error-popup-overlay" onClick={onClose} role="presentation">
      <div
        className="error-popup-content"
        role="alertdialog"
        aria-modal="true"
        aria-labelledby="error-popup-title"
        onClick={(e) => e.stopPropagation()}
      >
        <button
          className="error-popup-close"
          onClick={onClose}
          aria-label="Zamknij"
        >
          ×
        </button>
        <div className="error-popup-icon" aria-hidden="true">!</div>
        <h2 id="error-popup-title" className="error-popup-title">Wystąpił błąd</h2>
        <p className="error-popup-message">{message}</p>
        <button className="error-popup-ok" onClick={onClose}>OK</button>
      </div>
    </div>
  );
}
