import { useState, useEffect } from 'react';
import './StocksList.css';

export default function StocksList({ stocks, onSelectStock }) {
  const [loading, setLoading] = useState(false);
  const [prices, setPrices] = useState({});

  useEffect(() => {
    // Fetch prices for each stock
    const fetchPrices = async () => {
      setLoading(true);
      const priceMap = {};

    //   for (const stock of stocks) {
    //     try {
    //       const response = await fetch(`/api/stocks/${stock.symbol}/quote`);
    //       const data = await response.json();
    //       priceMap[stock.symbol] = data.price;
    //     } catch (err) {
    //       console.error(`Failed to fetch price for ${stock.symbol}`, err);
    //       priceMap[stock.symbol] = 'N/A';
    //     }
    //   }

    //   setPrices(priceMap);
    //   setLoading(false);
    // };

    for (const stock of stocks) {
        try {
          await new Promise(resolve => setTimeout(resolve, 500)); 
          
          const randomPrice = (Math.random() * 100 + 100).toFixed(2);
          priceMap[stock.symbol] = parseFloat(randomPrice);
          
        } catch (err) {
          console.error(`Failed to fetch price for ${stock.symbol}`, err);
          priceMap[stock.symbol] = 'N/A';
        }
      }

      setPrices(priceMap);
      setLoading(false);
    };

    if (stocks.length > 0) {
      fetchPrices();
    }
  }, [stocks]);

  return (
    <div className="stocks-container">
      <h2 className="stocks-title">Available Stocks</h2>

      {loading && <div className="loading">Loading prices...</div>}

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
                <span className="price-loading">Loading...</span>
              )}
            </div>

            <button
              className="stock-btn"
              onClick={() => onSelectStock({ ...stock, currentPrice: prices[stock.symbol] })}
              disabled={!prices[stock.symbol]}
            >
              Trade
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
