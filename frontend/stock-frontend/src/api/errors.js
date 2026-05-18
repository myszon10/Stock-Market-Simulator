import { ApiError } from './client';

const MESSAGES_PL = {
  USER_ALREADY_EXISTS: 'Użytkownik o tej nazwie już istnieje.',
  INVALID_CREDENTIALS: 'Błędna nazwa użytkownika lub hasło.',
  UNAUTHORIZED: 'Musisz się zalogować, aby kontynuować.',
  BAD_REQUEST: 'Niepoprawne dane.',
  INVALID_ORDER_REQUEST: 'Niepoprawne dane zamówienia.',
  INVALID_QUANTITY: 'Ilość akcji musi być większa od zera.',
  INSUFFICIENT_CASH: 'Nie masz wystarczających środków na ten zakup.',
  INSUFFICIENT_HOLDINGS: 'Nie masz wystarczająco akcji do tej operacji.',
  UNSUPPORTED_SYMBOL: 'Ten symbol akcji nie jest obsługiwany.',
  PRICE_UNAVAILABLE: 'Cena tej akcji jest chwilowo niedostępna.',
  QUOTE_NOT_AVAILABLE: 'Cena tej akcji jest chwilowo niedostępna.',
  EXTERNAL_SERVICE_UNAVAILABLE: 'Zewnętrzny serwis cen jest chwilowo niedostępny.',
  MISSING_API_KEY: 'Brak konfiguracji klucza API po stronie serwera.',
  USER_NOT_FOUND: 'Nie znaleziono użytkownika.',
  INTERNAL_SERVER_ERROR: 'Wewnętrzny błąd serwera. Spróbuj ponownie później.',
  NETWORK_ERROR: 'Brak połączenia z serwerem. Sprawdź czy backend działa.',
};

export const formatApiError = (err) => {
  if (err instanceof ApiError) {
    if (err.code && MESSAGES_PL[err.code]) return MESSAGES_PL[err.code];
    if (err.message) return err.message;
    return 'Wystąpił błąd po stronie serwera.';
  }
  if (err && err.message) return err.message;
  return 'Coś poszło nie tak. Spróbuj ponownie.';
};
