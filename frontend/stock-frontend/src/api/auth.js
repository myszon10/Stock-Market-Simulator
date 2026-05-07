// export const loginUser = async (username, password) => {
//   const response = await fetch('/api/auth/login', {
//     method: 'POST',
//     headers: { 'Content-Type': 'application/json' },
//     body: JSON.stringify({ username, password }),
//   });

//   if (!response.ok) {
//     throw new Error('Błędny login lub hasło'); // Odpowiada błędowi 401 z dokumentacji
//   }
//   return response.json(); // Zwraca { userId, username }
// };

// export const registerUser = async (username, password) => {
//   const response = await fetch('/api/auth/register', {
//     method: 'POST',
//     headers: { 'Content-Type': 'application/json' },
//     body: JSON.stringify({ username, password }),
//   });

//   if (!response.ok) {
//     throw new Error('Niepoprawne dane lub użytkownik już istnieje'); // Odpowiada 400/409
//   }
//   return response.json(); // Zwraca { id, username, cashBalance }
// };



// Mocki na potrzeby testów:

const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

export const loginUser = async (username, password) => {
  await delay(1000); // Udajemy, że czekamy na serwer

    // zhardcodowane hasło do mocków
  if (password !== 'haslo123') {
    throw new Error('Błędny login lub hasło (mock)'); 
  }


  return {
    userId: 1,
    username: username,
    cashBalance: 10000
  };
};

export const registerUser = async (username, password) => {
  await delay(1000);

  // Symulujemy walidację (np. blokujemy krótkie hasła)
  if (password.length < 5) {
    throw new Error('Hasło musi mieć co najmniej 5 znaków (mock)'); 
  }

  // Zwracamy obiekt z saldem początkowym z dokumentacji (sekcja 9.1)
  return {
    id: 1,
    username: username,
    cashBalance: 10000
  };
};

// Mock stocków, żeby się wyświetlało
export const fetchStocks = async () => {
  await delay(800); // Udajemy, że serwer chwilę myśli
  return [
    { symbol: "AAPL", name: "Apple Inc." },
    { symbol: "MSFT", name: "Microsoft Corporation" },
    { symbol: "GOOGL", name: "Alphabet Inc." }
  ];
};

// Tu jest wersja z prawdziwymi fetchami, ale na potrzeby testów i braku backendu używamy mocka powyżej.
/*
export const fetchStocks = async () => {
  const response = await fetch('/api/stocks');
  if (!response.ok) {
    throw new Error('Nie udało się pobrać listy akcji');
  }
  return response.json();
};
*/