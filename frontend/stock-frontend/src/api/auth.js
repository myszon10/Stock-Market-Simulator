export const loginUser = async (username, password) => {
  const response = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) {
    throw new Error('Błędny login lub hasło'); // Odpowiada błędowi 401 z dokumentacji
  }
  return response.json(); // Zwraca { userId, username }
};

export const registerUser = async (username, password) => {
  const response = await fetch('/api/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) {
    throw new Error('Niepoprawne dane lub użytkownik już istnieje'); // Odpowiada 400/409
  }
  return response.json(); // Zwraca { id, username, cashBalance }
};

export const fetchStocks = async () => {
  const response = await fetch('/api/stocks');
  if (!response.ok) {
    throw new Error('Nie udało się pobrać listy akcji');
  }
  return response.json();
};