// src/api/trading.js

const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));


// Zmockowane dopóki nie ma endpointów

export const buyStock = async (symbol, quantity, price) => {
  await delay(1000); // Udajemy zapytanie do serwera

  const total = quantity * price;

  // Losowy błąd dla symulacji
  if (quantity > 1000) {
    throw new Error('Niewystarczające środki (mock)');
  }

  return {
    transactionId: Math.floor(Math.random() * 100000),
    symbol: symbol,
    side: 'BUY',
    quantity: quantity,
    price: price,
    total: total,
    createdAt: new Date().toISOString()
  };
};

export const fetchTransactions = async () => {
  await delay(800); // Udajemy zapytanie do serwera

  return [
    {
      transactionId: 101,
      symbol: 'AAPL',
      side: 'BUY',
      quantity: 10,
      price: 150.25,
      total: 1502.50,
      createdAt: new Date(Date.now() - 86400000).toISOString()
    },
    {
      transactionId: 102,
      symbol: 'MSFT',
      side: 'BUY',
      quantity: 5,
      price: 310.00,
      total: 1550.00,
      createdAt: new Date(Date.now() - 3600000).toISOString()
    }
  ];
};


/*
export const buyStock = async (symbol, quantity) => {
  const response = await fetch('/api/orders/buy', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ symbol, quantity }),
  });

  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(errorData.message || 'Wystąpił błąd podczas zakupu');
  }

  return response.json();
};

export const fetchTransactions = async () => {
  const response = await fetch('/api/transactions');
  
  if (!response.ok) {
    throw new Error('Nie udało się pobrać historii transakcji');
  }

  return response.json();
};
*/
