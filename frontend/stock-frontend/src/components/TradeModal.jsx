import { useState } from 'react';
import { buyStock } from '../api/trading';
import './TradeModal.css';

export default function TradeModal({ stock, onClose, onTradeSuccess }) {
  const [mode, setMode] = useState('buy'); // 'buy' or 'sell'
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleBuy = async () => {
    if (quantity <= 0) {
      setError('Ilość musi być większa niż zero');
      return;
    }
    
    setLoading(true);
    setError('');

    try {
      const transaction = await buyStock(stock.symbol, parseInt(quantity, 10), stock.currentPrice);
      onTradeSuccess(transaction);
      onClose();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSell = async () => {
    // Na razie mock - bez implementacji
    setError('Opcja sprzedaży nie jest jeszcze zaimplementowana.');
  };

  const totalValue = (quantity * stock.currentPrice).toFixed(2);

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <button className="modal-close" onClick={onClose}>×</button>
        
        <div className="modal-tabs">
          <button 
            className={`modal-tab ${mode === 'buy' ? 'active' : ''}`}
            onClick={() => { setMode('buy'); setError(''); }}
          >
            Kup
          </button>
          <button 
            className={`modal-tab ${mode === 'sell' ? 'active' : ''}`}
            onClick={() => { setMode('sell'); setError(''); }}
          >
            Sprzedaj
          </button>
        </div>

        <h2 className="modal-title">
          {mode === 'buy' ? 'Kup akcje' : 'Sprzedaj akcje'} {stock.symbol}
        </h2>
        <p className="modal-subtitle">{stock.name}</p>

        <div className="modal-price-info">
          <span>Obecna cena:</span>
          <strong>${stock.currentPrice.toFixed(2)}</strong>
        </div>

        {error && <div className="modal-error">{error}</div>}

        <div className="modal-input-group">
          <label htmlFor="quantity">Ilość akcji</label>
          <input
            type="number"
            id="quantity"
            min="1"
            value={quantity}
            onChange={(e) => setQuantity(e.target.value)}
          />
        </div>

        <div className="modal-total">
          <span>{mode === 'buy' ? 'Przewidywany koszt:' : 'Przewidywany zysk:'}</span>
          <strong className={mode === 'sell' ? 'text-sell' : ''}>${totalValue}</strong>
        </div>

        <div className="modal-actions">
          <button className="modal-btn-cancel" onClick={onClose} disabled={loading}>
            Anuluj
          </button>
          {mode === 'buy' ? (
            <button className="modal-btn-buy" onClick={handleBuy} disabled={loading}>
              {loading ? <span className="modal-spinner"></span> : 'Kup teraz'}
            </button>
          ) : (
            <button className="modal-btn-sell" onClick={handleSell} disabled={loading}>
              Sprzedaj
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
