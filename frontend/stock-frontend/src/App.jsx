import { useState, useEffect } from 'react';
import AuthForm from './components/AuthForm';
import Profile from './components/Profile';
import StocksList from './components/StocksList';
import TradeModal from './components/TradeModal';
import TransactionHistory from './components/TransactionHistory';
import { fetchStocks as fetchStocksApi } from './api/auth'; 
import './App.css';

function App() {
  const [loggedInUser, setLoggedInUser] = useState(null);
  const [stocks, setStocks] = useState([]);
  const [selectedStock, setSelectedStock] = useState(null);
  const [txTrigger, setTxTrigger] = useState(0);
  const [showHistory, setShowHistory] = useState(false);

  const handleAuthSuccess = (userData) => {
    setLoggedInUser(userData);
    localStorage.setItem('user', JSON.stringify(userData));
    fetchStocks();
  };

  const handleLogout = () => {
    setLoggedInUser(null);
    setStocks([]);
    setSelectedStock(null);
    localStorage.removeItem('user');
  };

  // const fetchStocks = async () => {
  //   try {
  //     const response = await fetch('http://localhost:9000/api/stocks');
  //     const data = await response.json();
  //     setStocks(data);
  //   } catch (err) {
  //     console.error('Failed to fetch stocks:', err);
  //   }
  // };

  const fetchStocks = async () => {
    try {
      const data = await fetchStocksApi();
      setStocks(data);
    } catch (err) {
      console.error('Failed to fetch stocks:', err);
    }
  };

  useEffect(() => {
    const savedUser = localStorage.getItem('user');
    if (savedUser) {
      try {
        setLoggedInUser(JSON.parse(savedUser));
        fetchStocks();
      } catch (err) {
        console.error('Failed to restore user session:', err);
        localStorage.removeItem('user');
      }
    }
  }, []);

  if (!loggedInUser) {
    return <AuthForm onAuthSuccess={handleAuthSuccess} />;
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
            <div className="loading-message">Loading stocks...</div>
          )}
        </div>
      </main>

      {showHistory && (
        <TransactionHistory newTransactionsTrigger={txTrigger} onClose={() => setShowHistory(false)} />
      )}

      {selectedStock && (
        <TradeModal 
          stock={selectedStock} 
          onClose={() => setSelectedStock(null)} 
          onTradeSuccess={(tx) => {
            setTxTrigger(prev => prev + 1);
            // Tutaj można by zaktualizować loggedInUser w przyszłości (np. cashBalance)
          }}
        />
      )}
    </div>
  );
}

export default App;
