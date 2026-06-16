import { useState, useEffect } from 'react';
import { fetchLeaderboard } from '../api/trading';
import './Leaderboard.css';

export default function Leaderboard({ currentUserId, onError }) {
  const [entries, setEntries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expandedUser, setExpandedUser] = useState(null);

  useEffect(() => {
    let cancelled = false;

    const loadLeaderboard = async () => {
      setLoading(true);
      try {
        const data = await fetchLeaderboard();
        if (!cancelled) setEntries(data);
      } catch (err) {
        if (onError) onError(err);
        if (!cancelled) setEntries([]);
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    loadLeaderboard();
    return () => { cancelled = true; };
  }, [onError]);

  const formatCurrency = (val) =>
    Number(val).toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    });

  const getMedalEmoji = (rank) => {
    if (rank === 1) return '🥇';
    if (rank === 2) return '🥈';
    if (rank === 3) return '🥉';
    return `#${rank}`;
  };

  const toggleExpand = (username) => {
    setExpandedUser((prev) => (prev === username ? null : username));
  };

  return (
    <div className="leaderboard-container">
      <div className="leaderboard-header">
        <h2 className="leaderboard-title">🏆 Leaderboard</h2>
        <span className="leaderboard-subtitle">Ranking graczy wg wartości portfela</span>
      </div>

      {loading && (
        <div className="leaderboard-loading">
          <span className="leaderboard-spinner"></span>
          Ładowanie rankingu...
        </div>
      )}

      {!loading && entries.length === 0 && (
        <p className="leaderboard-empty">Brak danych do wyświetlenia.</p>
      )}

      {!loading && entries.length > 0 && (
        <div className="leaderboard-list">
          {entries.map((entry) => {
            const isCurrentUser = entry.username === (entries.find(e => e.userId === currentUserId)?.username);
            const rank = entry.rank;
            const isExpanded = expandedUser === entry.username;
            const hasPositions = entry.positions && entry.positions.length > 0;

            return (
              <div
                key={entry.username}
                className={`leaderboard-row-wrapper ${isExpanded ? 'expanded' : ''}`}
              >
                <div
                  className={`leaderboard-row ${isCurrentUser ? 'leaderboard-row-current' : ''} ${rank <= 3 ? `leaderboard-row-top${rank}` : ''} clickable`}
                  onClick={() => toggleExpand(entry.username)}
                >
                  <div className="leaderboard-rank">
                    <span className={`rank-badge ${rank <= 3 ? `rank-${rank}` : ''}`}>
                      {getMedalEmoji(rank)}
                    </span>
                  </div>

                  <div className="leaderboard-user">
                    <div className="leaderboard-avatar">
                      {entry.username[0].toUpperCase()}
                    </div>
                    <div className="leaderboard-username-wrap">
                      <span className="leaderboard-username">{entry.username}</span>
                      {isCurrentUser && <span className="leaderboard-you-badge">Ty</span>}
                    </div>
                  </div>

                  <div className="leaderboard-stats">
                    <div className="leaderboard-stat">
                      <span className="leaderboard-stat-label">Wartość portfela</span>
                      <span className="leaderboard-stat-value-total">${formatCurrency(entry.totalAccountValue)}</span>
                    </div>
                  </div>

                  <div className={`leaderboard-expand-icon ${isExpanded ? 'rotated' : ''}`}>
                    ▾
                  </div>
                </div>

                {isExpanded && (
                  <div className="leaderboard-positions">
                    {hasPositions ? (
                      <table className="positions-table">
                        <thead>
                          <tr>
                            <th>Symbol</th>
                            <th>Ilość</th>
                            <th>Obecna cena</th>
                            <th>Wartość</th>
                          </tr>
                        </thead>
                        <tbody>
                          {entry.positions.map((pos) => (
                            <tr key={pos.symbol}>
                              <td className="pos-symbol">{pos.symbol}</td>
                              <td>{pos.quantity} szt.</td>
                              <td>${formatCurrency(pos.currentPrice)}</td>
                              <td className="pos-value">${formatCurrency(pos.currentValue)}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    ) : (
                      <p className="positions-empty">Brak akcji — tylko gotówka.</p>
                    )}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
