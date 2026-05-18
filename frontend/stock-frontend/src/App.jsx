import { useState, useEffect, useCallback } from 'react';
import AuthForm from './components/AuthForm';
import Profile from './components/Profile';
import StocksList from './components/StocksList';
import TradeModal from './components/TradeModal';
import TransactionHistory from './components/TransactionHistory';
import ErrorPopup from './components/ErrorPopup';
import { fetchStocks as fetchStocksApi, logoutUser } from './api/auth';
import { ApiError } from './api/client';
import { formatApiError } from './api/errors';
import './App.css';

function App() {
  const [loggedInUser, setLoggedInUser] = useState(null);
  const [stocks, setStocks] = useState([]);
  const [selectedStock, setSelectedStock] = useState(null);
  const [txTrigger, setTxTrigger] = useState(0);
  const [showHistory, setShowHistory] = useState(false);
  const [globalError, setGlobalError] = useState(null);

  const reportError = useCallback((err) => setGlobalError(formatApiError(err)), []);
  const clearError = useCallback(() => setGlobalError(null), []);

  const clearSession = () => {
    setLoggedInUser(null);
    setStocks([]);
    setSelectedStock(null);
    setShowHistory(false);
    localStorage.removeItem('user');
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

  useEffect(() => {
    const savedUser = localStorage.getItem('user');
    if (savedUser) {
      try {
        setLoggedInUser(JSON.parse(savedUser));
        fetchStocks();
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
          <button className="history-btn-toggle" onClick={() => setShowHistory(true)}>
            Historia transakcji
          </button>
        </div>
      </header>

      <main className="app-main">
        <div className="dashboard">
          <Profile user={loggedInUser} onLogout={handleLogout} />

          {stocks.length > 0 && (
            <StocksList stocks={stocks} onSelectStock={setSelectedStock} />
          )}

          {stocks.length === 0 && (
            <div className="loading-message">Ładowanie akcji...</div>
          )}
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
          onClose={() => setSelectedStock(null)}
          onTradeSuccess={(tx) => {
            setTxTrigger((prev) => prev + 1);
            setLoggedInUser((prev) => {
              if (!prev) return prev;
              const delta = Number(tx.total);
              let nextBalance = Number(prev.cashBalance) - delta;
              if (tx.side === 'SELL') {
                nextBalance = Number(prev.cashBalance) + delta;
              }
              const next = { ...prev, cashBalance: nextBalance };
              localStorage.setItem('user', JSON.stringify(next));
              return next;
            });
          }}
          onError={reportError}
        />
      )}

      <ErrorPopup message={globalError} onClose={clearError} />
    </div>
  );
}

export default App;
