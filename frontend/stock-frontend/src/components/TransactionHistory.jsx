import { useState, useEffect } from 'react';
import { fetchTransactions } from '../api/trading';
import './TransactionHistory.css';

export default function TransactionHistory({ newTransactionsTrigger, onClose }) {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadData = async () => {
      setLoading(true);
      try {
        const data = await fetchTransactions();
        setTransactions(data);
      } catch (err) {
        setError('Nie udało się załadować historii transakcji');
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [newTransactionsTrigger]);

  if (loading && transactions.length === 0) {
    return <div className="history-loading">Ładowanie historii...</div>;
  }

  if (error) {
    return <div className="history-error">{error}</div>;
  }

  return (
    <div className="history-overlay">
      <div className="history-modal-content">
        <button className="history-close" onClick={onClose}>×</button>
        <h2 className="history-title">Historia Transakcji</h2>
        
        {transactions.length === 0 ? (
          <p className="history-empty">Brak wykonanych transakcji.</p>
        ) : (
        <div className="history-table-wrapper">
          <table className="history-table">
            <thead>
              <tr>
                <th>Data</th>
                <th>Akcja</th>
                <th>Operacja</th>
                <th>Ilość</th>
                <th>Cena</th>
                <th>Suma</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map(tx => (
                <tr key={tx.transactionId}>
                  <td>{new Date(tx.createdAt).toLocaleString()}</td>
                  <td className="tx-symbol">{tx.symbol}</td>
                  <td>
                    <span className={`tx-side tx-side-${tx.side.toLowerCase()}`}>
                      {tx.side}
                    </span>
                  </td>
                  <td>{tx.quantity}</td>
                  <td>${tx.price.toFixed(2)}</td>
                  <td className="tx-total">${tx.total.toFixed(2)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      </div>
    </div>
  );
}
