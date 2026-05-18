// src/api/trading.js
import { post, get } from './client';

// Backend `POST /api/orders/buy` pobiera bieżącą cenę z MarketDataService —
// nie przesyłamy price w requeście.
export const buyStock = async (symbol, quantity) => {
  return post('/api/orders/buy', { symbol, quantity });
};

// Backend `GET /api/transactions` zwraca {id, symbol, side, quantity, price, createdAt}.
// UI używa pól transactionId i total, więc je dopełniamy w jednym miejscu.
export const fetchTransactions = async () => {
  const list = await get('/api/transactions');
  return list.map((tx) => {
    const price = Number(tx.price);
    const total = price * Number(tx.quantity);
    return {
      transactionId: tx.id,
      symbol: tx.symbol,
      side: tx.side,
      quantity: tx.quantity,
      price,
      total,
      createdAt: tx.createdAt,
    };
  });
};
