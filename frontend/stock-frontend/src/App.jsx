import { useState, useEffect, useCallback } from 'react';
import AuthForm from './components/AuthForm';
import Profile from './components/Profile';
import StocksList from './components/StocksList';
import MyHoldings from './components/MyHoldings';
import TradeModal from './components/TradeModal';
import TransactionHistory from './components/TransactionHistory';
import Leaderboard from './components/Leaderboard';
import ErrorPopup from './components/ErrorPopup';
import { fetchStocks as fetchStocksApi, logoutUser } from './api/auth';
import { fetchPortfolio } from './api/trading';
import { ApiError } from './api/client';
import { formatApiError } from './api/errors';
import './App.css';

function App() {
  const [loggedInUser, setLoggedInUser] = useState(null);
  const [stocks, setStocks] = useState([]);
  const [portfolio, setPortfolio] = useState(null);
  const [selectedStock, setSelectedStock] = useState(null);
  const [tradeMode, setTradeMode] = useState('buy');
  const [txTrigger, setTxTrigger] = useState(0);
  const [showHistory, setShowHistory] = useState(false);
  const [showLeaderboard, setShowLeaderboard] = useState(false);
  const [globalError, setGlobalError] = useState(null);

  const reportError = useCallback((err) => setGlobalError(formatApiError(err)), []);
  const clearError = useCallback(() => setGlobalError(null), []);

  const clearSession = () => {
    setLoggedInUser(null);
    setStocks([]);
    setPortfolio(null);
    setSelectedStock(null);
    setShowHistory(false);
    setShowLeaderboard(false);
    localStorage.removeItem('user');
  };

  const refreshPortfolio = async () => {
    try {
      const data = await fetchPortfolio();
      setPortfolio(data);
      // Synchronizuj cashBalance w user state i localStorage
      setLoggedInUser((prev) => {
        if (!prev) return prev;
        const next = { ...prev, cashBalance: data.cashBalance };
        localStorage.setItem('user', JSON.stringify(next));
        return next;
      });
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        clearSession();
        return;
      }
      // Portfolio niedostępne nie jest krytycznym błędem — nie blokujemy UI
      console.error('Failed to fetch portfolio', err);
    }
  };

  const fetchStocks = async () => {
    try {
      const data = await fetchStocksApi();
      setStocks(data);
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        clearSession();
        return;
      }
      reportError(err);
    }
  };

  const handleAuthSuccess = (userData) => {
    setLoggedInUser(userData);
    localStorage.setItem('user', JSON.stringify(userData));
    fetchStocks();
    refreshPortfolio();
  };

  const handleLogout = async () => {
    try {
      await logoutUser();
    } catch (err) {
      reportError(err);
    } finally {
      clearSession();
    }
  };

  // Otwórz TradeModal w trybie kupowania (z listy dostępnych akcji)
  const handleBuyFromList = (stockData) => {
    setTradeMode('buy');
    setSelectedStock(stockData);
  };

  // Otwórz TradeModal w trybie sprzedawania (z MyHoldings)
  const handleSellFromHoldings = (holdingData) => {
    setTradeMode('sell');
    setSelectedStock(holdingData);
  };

  useEffect(() => {
    const savedUser = localStorage.getItem('user');
    if (savedUser) {
      try {
        setLoggedInUser(JSON.parse(savedUser));
        fetchStocks();
        refreshPortfolio();
      } catch {
        localStorage.removeItem('user');
      }
    }
  }, []);

  if (!loggedInUser) {
    return (
      <>
        <AuthForm onAuthSuccess={handleAuthSuccess} onError={reportError} />
        <ErrorPopup message={globalError} onClose={clearError} />
      </>
    );
  }

  return (
    <div className="app-container">
      <header className="app-header">
        <div className="header-content">
          <h1 className="app-title">Stock Market Simulator</h1>
          <div className="header-actions">
            <button
              className={`header-btn ${showLeaderboard ? 'header-btn-active' : ''}`}
              onClick={() => setShowLeaderboard(!showLeaderboard)}
            >
              🏆 Leaderboard
            </button>
            <button className="header-btn" onClick={() => setShowHistory(true)}>
              📋 Historia transakcji
            </button>
          </div>
        </div>
      </header>

      <main className="app-main">
        <div className="dashboard">
          <Profile user={loggedInUser} portfolio={portfolio} onLogout={handleLogout} />

          {showLeaderboard && (
            <Leaderboard
              currentUserId={loggedInUser.userId}
              onError={reportError}
            />
          )}

          {stocks.length > 0 && (
            <StocksList stocks={stocks} onSelectStock={handleBuyFromList} />
          )}

          {stocks.length === 0 && (
            <div className="loading-message">Ładowanie akcji...</div>
          )}

          <MyHoldings
            portfolio={portfolio}
            stocks={stocks}
            onSellStock={handleSellFromHoldings}
          />
        </div>
      </main>

      {showHistory && (
        <TransactionHistory
          newTransactionsTrigger={txTrigger}
          onClose={() => setShowHistory(false)}
          onError={reportError}
        />
      )}

      {selectedStock && (
        <TradeModal
          stock={selectedStock}
          initialMode={tradeMode}
          onClose={() => setSelectedStock(null)}
          onTradeSuccess={(tx) => {
            setTxTrigger((prev) => prev + 1);
            // Odśwież portfolio z backendu
            refreshPortfolio();
          }}
          onError={reportError}
        />
      )}

      <ErrorPopup message={globalError} onClose={clearError} />
    </div>
  );
}

export default App;
