import './MyHoldings.css';

export default function MyHoldings({ portfolio, stocks, onSellStock }) {
  const positions = portfolio ? (portfolio.positions || []) : [];

  const formatCurrency = (val) =>
    Number(val).toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    });

  // Mapowanie symboli na nazwy z listy dostępnych akcji
  const stockNameMap = {};
  if (stocks) {
    stocks.forEach((s) => {
      stockNameMap[s.symbol] = s.name;
    });
  }

  if (positions.length === 0) {
    return (
      <div className="my-holdings-container">
        <h2 className="my-holdings-title">Moje akcje</h2>
        <p className="my-holdings-empty">
          Nie posiadasz jeszcze żadnych akcji. Kup akcje z listy powyżej, aby rozpocząć handel.
        </p>
      </div>
    );
  }

  return (
    <div className="my-holdings-container">
      <h2 className="my-holdings-title">Moje akcje</h2>

      <div className="my-holdings-grid">
        {positions.map((pos) => {
          const pl = Number(pos.profitLoss);
          const name = stockNameMap[pos.symbol] || pos.symbol;

          return (
            <div key={pos.symbol} className="holding-card">
              <div className="holding-card-header">
                <div>
                  <div className="holding-card-symbol">{pos.symbol}</div>
                  <div style={{ fontSize: '0.85rem', color: '#6b7280', marginTop: '2px' }}>
                    {name}
                  </div>
                </div>
                <div className="holding-card-quantity">{pos.quantity} szt.</div>
              </div>

              <div className="holding-card-prices">
                <div className="holding-card-price-item">
                  <span className="holding-card-price-label">Śr. cena zakupu</span>
                  <span className="holding-card-price-value">${formatCurrency(pos.averageBuyPrice)}</span>
                </div>
                <div className="holding-card-price-item">
                  <span className="holding-card-price-label">Obecna cena</span>
                  <span className="holding-card-price-value">${formatCurrency(pos.currentPrice)}</span>
                </div>
              </div>

              <div className="holding-card-divider" />

              <div className="holding-card-footer">
                <div className="holding-card-value">
                  <span className="holding-card-value-label">Wartość</span>
                  <span className="holding-card-value-amount">${formatCurrency(pos.currentValue)}</span>
                </div>
                <div className="holding-card-pl">
                  <span className="holding-card-pl-label">Zysk/Strata</span>
                  <span className={`holding-card-pl-value ${pl >= 0 ? 'profit' : 'loss'}`}>
                    {pl >= 0 ? '+' : ''}${formatCurrency(pl)}
                  </span>
                </div>
              </div>

              <button
                className="holding-sell-btn"
                onClick={() => onSellStock({
                  symbol: pos.symbol,
                  name: name,
                  currentPrice: Number(pos.currentPrice),
                  ownedQuantity: pos.quantity,
                })}
              >
                Sprzedaj
              </button>
            </div>
          );
        })}
      </div>
    </div>
  );
}
