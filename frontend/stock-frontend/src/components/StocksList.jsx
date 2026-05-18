import { useState, useEffect } from 'react';
import { get } from '../api/client';
import './StocksList.css';

export default function StocksList({ stocks, onSelectStock }) {
  const [loading, setLoading] = useState(false);
  const [prices, setPrices] = useState({});

  useEffect(() => {
    if (stocks.length === 0) return;

    let cancelled = false;

    const fetchPrices = async () => {
      setLoading(true);

      const results = await Promise.all(
        stocks.map(async (stock) => {
          try {
            const quote = await get(`/api/stocks/${stock.symbol}/quote`);
            return [stock.symbol, Number(quote.price)];
          } catch (err) {
            console.error(`Failed to fetch price for ${stock.symbol}`, err);
            return [stock.symbol, 'N/A'];
          }
        })
      );

      if (cancelled) return;

      setPrices(Object.fromEntries(results));
      setLoading(false);
    };

    fetchPrices();

    return () => {
      cancelled = true;
    };
  }, [stocks]);

  return (
    <div className="stocks-container">
      <h2 className="stocks-title">Dostępne akcje</h2>

      {loading && <div className="loading">Ładowanie cen...</div>}

      <div className="stocks-grid">
        {stocks.map((stock) => (
          <div key={stock.symbol} className="stock-card">
            <div className="stock-header">
              <div className="stock-symbol">{stock.symbol}</div>
              <div className="stock-name">{stock.name}</div>
            </div>

            <div className="stock-price">
              {prices[stock.symbol] ? (
                <>
                  <span className="price-value">
                    ${typeof prices[stock.symbol] === 'number'
                      ? prices[stock.symbol].toFixed(2)
                      : prices[stock.symbol]}
                  </span>
                </>
              ) : (
                <span className="price-loading">Ładowanie...</span>
              )}
            </div>

            <button
              className="stock-btn"
              onClick={() => onSelectStock({ ...stock, currentPrice: prices[stock.symbol] })}
              disabled={!prices[stock.symbol] || prices[stock.symbol] === 'N/A'}
            >
              Handluj
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
