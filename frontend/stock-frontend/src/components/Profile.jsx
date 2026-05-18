import './Profile.css';

export default function Profile({ user, onLogout }) {
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
          <div className="stat-value">${(user.cashBalance || 0).toLocaleString('en-US', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2,
          })}</div>
          <div className="stat-unit">USD</div>
        </div>
        <div className="stat-box">
          <div className="stat-label">Total Portfolio Value</div>
          <div className="stat-value">$0.00</div>
          <div className="stat-unit">USD</div>
        </div>
        <div className="stat-box">
          <div className="stat-label">Profit/Loss</div>
          <div className="stat-value profit-neutral">+$0.00</div>
          <div className="stat-unit">0%</div>
        </div>
      </div>

      <button className="logout-btn" onClick={onLogout}>
        <span>Wyloguj się</span>
      </button>
    </div>
  );
}
