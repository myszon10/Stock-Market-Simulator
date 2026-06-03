import './Profile.css';

export default function Profile({ user, portfolio, onLogout }) {
  const cashBalance = portfolio ? portfolio.cashBalance : (user.cashBalance || 0);
  const totalStockValue = portfolio ? portfolio.totalStockValue : 0;
  const totalAccountValue = portfolio ? portfolio.totalAccountValue : cashBalance;
  const positions = portfolio ? (portfolio.positions || []) : [];

  const totalProfitLoss = positions.reduce((sum, p) => sum + Number(p.profitLoss), 0);
  const initialInvestment = positions.reduce(
    (sum, p) => sum + Number(p.averageBuyPrice) * p.quantity,
    0
  );
  const profitPercent = initialInvestment > 0
    ? ((totalProfitLoss / initialInvestment) * 100).toFixed(2)
    : '0.00';

  const formatCurrency = (val) =>
    Number(val).toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    });

  return (
    <div className="profile-header">
      <div className="profile-card">
        <div className="profile-avatar">
          <span>{user.username[0].toUpperCase()}</span>
        </div>
        <div className="profile-info">
          <h1 className="profile-username">{user.username}</h1>
        </div>
      </div>

      <div className="profile-stats">
        <div className="stat-box">
          <div className="stat-label">Cash Balance</div>
          <div className="stat-value">${formatCurrency(cashBalance)}</div>
          <div className="stat-unit">USD</div>
        </div>
        <div className="stat-box">
          <div className="stat-label">Total Portfolio Value</div>
          <div className="stat-value">${formatCurrency(totalAccountValue)}</div>
          <div className="stat-unit">USD</div>
        </div>
        <div className="stat-box">
          <div className="stat-label">Profit/Loss</div>
          <div className={`stat-value ${totalProfitLoss >= 0 ? 'profit-neutral' : 'profit-loss'}`}>
            {totalProfitLoss >= 0 ? '+' : ''}${formatCurrency(totalProfitLoss)}
          </div>
          <div className="stat-unit">{totalProfitLoss >= 0 ? '+' : ''}{profitPercent}%</div>
        </div>
      </div>

      <button className="logout-btn" onClick={onLogout}>
        <span>Wyloguj się</span>
      </button>
    </div>
  );
}
