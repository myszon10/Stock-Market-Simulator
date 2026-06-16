import { useState } from 'react';
import { buyStock, sellStock } from '../api/trading';
import './TradeModal.css';

export default function TradeModal({ stock, onClose, onTradeSuccess, onError, initialMode }) {
  const [mode, setMode] = useState(initialMode || 'buy'); // 'buy' or 'sell'
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const ownedQuantity = stock.ownedQuantity || 0;

  const handleBuy = async () => {
    if (quantity <= 0) {
      setError('Ilość musi być większa niż zero');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const transaction = await buyStock(stock.symbol, parseInt(quantity, 10));
      onTradeSuccess(transaction);
      onClose();
    } catch (err) {
      if (onError) {
        onError(err);
      } else {
        setError(err.message);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleSell = async () => {
    const qty = parseInt(quantity, 10);
    if (qty <= 0) {
      setError('Ilość musi być większa niż zero');
      return;
    }

    if (ownedQuantity > 0 && qty > ownedQuantity) {
      setError(`Posiadasz tylko ${ownedQuantity} szt. tej akcji.`);
      return;
    }

    setLoading(true);
    setError('');

    try {
      const transaction = await sellStock(stock.symbol, qty);
      onTradeSuccess(transaction);
      onClose();
    } catch (err) {
      if (onError) {
        onError(err);
      } else {
        setError(err.message);
      }
    } finally {
      setLoading(false);
    }
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

        {mode === 'sell' && ownedQuantity > 0 && (
          <div className="modal-holdings-info">
            <span>Posiadane akcje:</span>
            <strong>{ownedQuantity} szt.</strong>
          </div>
        )}

        {error && <div className="modal-error">{error}</div>}

        <div className="modal-input-group">
          <label htmlFor="quantity">
            Ilość akcji
            {mode === 'sell' && ownedQuantity > 0 && (
              <button
                type="button"
                className="modal-max-btn"
                onClick={() => setQuantity(ownedQuantity)}
              >
                MAX
              </button>
            )}
          </label>
          <input
            type="number"
            id="quantity"
            min="1"
            max={mode === 'sell' && ownedQuantity > 0 ? ownedQuantity : undefined}
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
              {loading ? <span className="modal-spinner"></span> : 'Sprzedaj'}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
