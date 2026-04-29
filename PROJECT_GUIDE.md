# Stock Market Simulator – Project Guide

> Dokument standaryzujący pracę zespołu nad projektem zaliczeniowym z przedmiotu **Programowanie w języku Scala**.
> Repozytorium: `https://github.com/myszon10/Stock-Market-Simulator`
> Zespół: Szymon Mażulis, Bartosz Radomski, Igor Oleksy, Maciej Rozpędek, Jakub Warkocki

---
## 1. Cel dokumentu

Ten dokument jest praktycznym guidelinem dla zespołu pracującego nad projektem. Nie traktujemy go jako sztywnego regulaminu, tylko jako wspólny punkt odniesienia: jak uruchomić projekt, jak mniej więcej dzielimy odpowiedzialności, jaką mamy architekturę i jak pisać kod, żeby dało się go łatwo łączyć z pracą innych osób.

Najważniejsze cele organizacyjne:
- każdy wie, za co odpowiada,
- zmiany są robione w osobnych branchach,
- kod trafia do głównej gałęzi tylko przez Pull Request,
- logika biznesowa jest testowana,
- aplikacja zawsze powinna dać się uruchomić z instrukcji w README,
- projekt jest gotowy do prezentacji postępów 15 maja,
- projekt jest funkcjonalnie zamknięty przed sesją, najlepiej około 7–10 czerwca.

---
## 2. Opis projektu

### 2.1. Nazwa projektu
**Stock Market Simulator**

### 2.2. Krótki opis
Aplikacja webowa pozwalająca użytkownikom symulować inwestowanie w akcje przy użyciu wirtualnych środków i rzeczywistych danych rynkowych pobieranych z zewnętrznego API.

Użytkownik może:
- założyć konto,
- zalogować się,
- otrzymać wirtualne saldo startowe,
- sprawdzać aktualne ceny wybranych akcji,
- kupować akcje,
- sprzedawać akcje,
- przeglądać swoje portfolio,
- przeglądać historię transakcji,
- porównać wynik z innymi użytkownikami w rankingu.

### 2.3. Zakres domenowy
Projekt nie jest pełną symulacją giełdy. Nie implementujemy rzeczywistego order booka, mechanizmu dopasowywania zleceń ani zaawansowanych instrumentów finansowych.

Projekt jest symulatorem portfela inwestycyjnego:
```text
użytkownik + wirtualne saldo + aktualne ceny + kupno/sprzedaż + portfolio + historia + ranking
```

### 2.4. Główne założenia biznesowe
- Każdy użytkownik zaczyna z takim samym saldem startowym, np. `100000 USD`.
- Wszystkie transakcje są rozliczane w USD.
- Kupujemy i sprzedajemy tylko całe akcje.
- Nie obsługujemy ułamkowych akcji.
- Nie obsługujemy short-sellingu.
- Nie obsługujemy zleceń limit, stop-loss ani take-profit.
- Cena transakcji jest pobierana w momencie składania zlecenia.
- Lista obsługiwanych spółek jest ograniczona do wcześniej ustalonej listy symboli.
- Dane rynkowe pochodzą z zewnętrznego API lub z mocka w trybie demonstracyjnym/testowym.

---
## 3. Stack technologiczny

### 3.1. Backend
- **Scala**
- **Play Framework**
- **sbt**
- **Play JSON**
- **Play WS** lub inny klient HTTP dostępny w projekcie
- **Anorm** lub prosty dostęp do bazy przez JDBC
- **H2** na start, opcjonalnie PostgreSQL/SQLite później

### 3.2. Frontend
Na potrzeby projektu wystarczy prosty interfejs:
- widoki Play/Twirl albo statyczny HTML + CSS + JavaScript,
- bez rozbudowanego frameworka frontendowego,
- priorytetem jest funkcjonalność, nie wygląd.

### 3.3. Testy
- ScalaTest albo mUnit,
- testy jednostkowe logiki biznesowej,
- testy serwisów,
- testy wybranych endpointów,
- mock zewnętrznego API.

### 3.4. Narzędzia zespołowe
- YouTrack do planowania zadań w sprintach,
- GitHub jako repozytorium kodu,
- branche `develop` + feature branche,
- Pull Requesty dla większych zmian,
- lekkie code review,
- GitHub Actions z `sbt test`.

---
## 4. Role i odpowiedzialności

Role nie oznaczają, że tylko jedna osoba może dotykać danego obszaru. Oznaczają, że dana osoba jest głównym właścicielem tematu i pilnuje jego jakości.

### 4.1. Szymon – backend/API integration lead
Odpowiedzialności:
- koordynacja ogólnego kierunku projektu,
- integracja z zewnętrznym API giełdowym,
- `MarketDataService`,
- `FinnhubMarketDataService`,
- `MockMarketDataService`,
- cache cen,
- endpointy związane z akcjami i cenami:
	- `/api/stocks`,
	- `/api/stocks/:symbol/quote`
- konfiguracja obsługi API key po stronie aplikacji,
- obsługa błędów związanych z pobieraniem cen,
- przygotowanie trybu mock/demo dla danych rynkowych,
- wsparcie review w backendzie.

### 4.2. Maciej – DevOps/database + auth backend
Odpowiedzialności:  
- setup projektu i repozytorium:  
- Play Framework, sbt, `.gitignore`, branch `develop`, CI (GitHub Actions),  
- konfiguracja aplikacji i lokalnego środowiska (w tym baza danych),  
- struktura bazy danych i migracje (users, transactions, holdings, price_cache),  
- repozytoria bazodanowe,  
- backend związany z użytkownikami:  
- model użytkownika,  
- rejestracja i logowanie,  
- hashowanie haseł,  
- `AuthService` i `AuthController`,  
- testy podstawowego flow auth,  
- pomoc przy problemach z uruchomieniem projektu u innych członków zespołu.

### 4.3. Bartek – trading/portfolio logic
Odpowiedzialności:
- logika kupowania akcji,
- logika sprzedawania akcji,
- walidacja transakcji,
- aktualizacja salda,
- aktualizacja pozycji w portfolio,
- historia transakcji,
- testy `TradingService` i `PortfolioService`.

### 4.4. Igor – UI/frontend
Odpowiedzialności:
- główny layout aplikacji,  
- ekran logowania/rejestracji,  
- dashboard użytkownika,  
- lista akcji,  
- formularz kupna/sprzedaży,  
- widok portfolio,  
- widok historii transakcji,  
- komunikaty błędów po stronie UI,  
- podstawowa estetyka aplikacji,  
- integracja widoków z endpointami backendowymi.

### 4.5. Kuba – frontend/backend support + leaderboard
Odpowiedzialności:
- ranking użytkowników,  
- endpoint leaderboard,  
- widok leaderboardu,  
- pomoc przy implementacji widoków frontendowych,  
- podpięcie frontendowych formularzy pod endpointy backendowe,  
- pomoc przy widoku historii transakcji,  
- pomoc przy widoku portfolio,  
- pomoc przy prostych endpointach backendowych,  
- pomoc przy obsłudze błędów po stronie UI,  
- testy najważniejszych fragmentów, które ta osoba implementuje.

---
## 5. Harmonogram

### 5.1. Kluczowe daty

| Data         | Cel                                             |
| ------------ | ----------------------------------------------- |
| 29 kwietnia  | Start planowania i organizacji pracy            |
| 15 maja      | Prezentacja postępów, około 10 minut            |
| 7–10 czerwca | Preferowany termin zakończenia implementacji    |
| 16 czerwca   | Teoretyczny koniec semestru/finalna prezentacja |

### 5.2. Sprint 0 (organizacja)
**29 kwietnia – 1 maja**

Cele:  
- ustalenie stacku,  
- utworzenie brancha `develop`,  
- przygotowanie podstawowej struktury repozytorium,  
- podstawowy szkielet aplikacji Play,  
- pierwsze taski w YouTracku,  
- pierwszy workflow CI.  
  
Done:  
- projekt kompiluje się lokalnie,  
- każdy członek zespołu potrafi uruchomić aplikację,  
- istnieje branch `develop`,  
- istnieje podstawowy README,  
- istnieje podstawowa struktura katalogów projektu,  
- w YouTracku są rozpisane najbliższe zadania sprintowe.

### 5.3. Sprint 1 (szkielet aplikacji i baza)
**2 maja – 10 maja**

Cele:  
- podstawowe modele domenowe,  
- podstawowa struktura bazy danych,  
- migracje/evolutions,  
- podstawowe repozytoria bazodanowe,  
- rejestracja,  
- logowanie,  
- lista obsługiwanych akcji,  
- pierwsza wersja `MarketDataService`,  
- przygotowanie fundamentu pod demo kupna akcji.  
  
Done:  
- aplikacja uruchamia się lokalnie z README,  
- działa podstawowa baza danych,  
- można utworzyć użytkownika,  
- można zalogować użytkownika,  
- użytkownik ma saldo startowe,  
- endpoint listy akcji działa,  
- istnieje podstawowy interfejs lub endpoint do pobrania ceny akcji,  
- pierwsze testy przechodzą.

### 5.4. Sprint 2 (API cen i demo kupna)
**11 maja – 14 maja**

Cele:
- integracja z API cen,
- mock API,
- cache cen,
- kupno akcji,
- historia transakcji,
- przygotowanie prezentacji postępów.

Done:
- można pobrać cenę akcji,
- można kupić akcje,
- saldo się zmienia,
- transakcja zapisuje się w historii,
- demo na 15 maja jest gotowe,
- istnieje tryb mock/demo na wypadek problemów z API.

### 5.5. Sprint 3 (pełna logika portfolio)
**16 maja – 24 maja**

Cele:  
- sprzedaż akcji,  
- walidacje transakcji,  
- średnia cena zakupu,  
- aktualna wartość portfolio,  
- profit/loss,  
- dopracowanie historii transakcji.  
  
Done:  
- można kupować i sprzedawać,  
- nie da się sprzedać więcej akcji niż się posiada,  
- nie da się kupić bez środków,  
- portfolio liczy się poprawnie,  
- historia transakcji pokazuje najważniejsze dane,  
- logika jest pokryta podstawowymi testami.

### 5.6. Sprint 4 (UI, ranking i stabilizacja)
**25 maja – 31 maja**

Cele:  
- kompletne UI dla głównych funkcji,  
- ranking,  
- lepsza obsługa błędów,  
- seed danych demo,  
- stabilizacja aplikacji po stronie backendu i frontendu.  
  
Done:  
- cała aplikacja jest klikalna przez przeglądarkę,  
- ranking działa,  
- użytkownik widzi jasne komunikaty błędów,  
- projekt da się zaprezentować bez Postmana,  
- podstawowe flow demo działa od początku do końca.

### 5.7. Sprint 5 (testy, refactor, dokumentacja)
**1 czerwca – 7 czerwca**

Cele:  
- sprzątanie kodu,  
- refactor najbardziej chaotycznych fragmentów,  
- testy krytycznych scenariuszy,  
- uzupełnienie README,  
- podstawowa dokumentacja API,  
- przygotowanie finalnego scenariusza demo.  
  
Done:  
- `sbt test` przechodzi,  
- README jest kompletne,  
- główne flow działa stabilnie,  
- projekt da się uruchomić od zera na podstawie instrukcji,  
- istnieje prosty backup demo,  
- nie dodajemy już dużych nowych funkcji.

### 5.8. Sprint 6 (bufor)
**8 czerwca – 12 czerwca**

Cele:
- bugfixy,
- kosmetyka UI,
- próby prezentacji,
- ostateczne poprawki.

W tym sprincie nie dodajemy dużych funkcji, chyba że cały MVP jest już stabilny.

---
## 6. Zakres funkcjonalny

### 6.1. MVP
MVP oznacza minimalny zestaw funkcji wymagany, aby projekt był sensowny i możliwy do zaprezentowania.

MVP obejmuje:
- rejestrację,
- logowanie,
- saldo startowe,
- listę akcji,
- pobieranie aktualnej ceny akcji,
- kupno akcji,
- sprzedaż akcji,
- portfolio,
- historię transakcji,
- ranking użytkowników,
- podstawowy interfejs użytkownika,
- testy krytycznej logiki biznesowej,
- dokumentację uruchomienia.

### 6.2. Funkcje opcjonalne
Funkcje opcjonalne można robić dopiero po stabilnym MVP:
- watchlista,
- wykres cen,
- filtrowanie historii transakcji,
- reset konta demo,
- Docker Compose,
- tryb admina,
- dodatkowe statystyki użytkownika.

### 6.3. Funkcje poza zakresem
Nie implementujemy:
- order booka,
- matching engine,
- zleceń limit/stop,
- short-sellingu,
- instrumentów pochodnych,
- wielu walut,
- zaawansowanego real-time streamingu,
- pełnej aplikacji brokerskiej.

---
## 7. Architektura aplikacji

### 7.1. Proponowana struktura katalogów

```text
app/
  controllers/
    AuthController.scala
    StockController.scala
    OrderController.scala
    PortfolioController.scala
    LeaderboardController.scala

  models/
    User.scala
    Stock.scala
    Quote.scala
    Transaction.scala
    Holding.scala
    Portfolio.scala
    errors/
      DomainError.scala

  services/
    MarketDataService.scala
    FinnhubMarketDataService.scala
    MockMarketDataService.scala
    TradingService.scala
    PortfolioService.scala
    LeaderboardService.scala
    AuthService.scala

  repositories/
    UserRepository.scala
    TransactionRepository.scala
    HoldingRepository.scala
    PriceCacheRepository.scala

conf/
  application.conf
  routes
  evolutions/
    default/
      1.sql

test/
  services/
  repositories/
  controllers/

public/
  stylesheets/
  javascripts/
```

### 7.2. Warstwy
#### Controllers
Odpowiadają za:
- przyjęcie requestu HTTP,
- parsowanie JSON/formularzy,
- wywołanie serwisu,
- zwrócenie odpowiedzi HTTP.

Controllers nie powinny zawierać logiki biznesowej.
#### Services
Odpowiadają za logikę biznesową:
- kupno,
- sprzedaż,
- walidacja,
- obliczanie wartości portfolio,
- ranking,
- integracja z API.

#### Repositories
Odpowiadają za komunikację z bazą danych:
- zapis użytkownika,
- odczyt holdingów,
- zapis transakcji,
- odczyt historii.

Repositories nie powinny zawierać logiki biznesowej.

#### Models
Zawierają klasy domenowe:
- `User`,
- `Quote`,
- `Transaction`,
- `Holding`,
- `Portfolio`,
- typy błędów domenowych.

### 7.3. Reguła zależności
Preferowany kierunek zależności:
```text
Controller -> Service -> Repository
```

Nie robimy zależności w odwrotną stronę.
Przykład błędnej zależności:
```text
Repository -> Controller
```

Przykład poprawnej zależności:
```text
OrderController -> TradingService -> UserRepository / HoldingRepository / TransactionRepository
```

---
## 8. Model domenowy

### 8.1. User

```scala
case class User(
  id: Long,
  username: String,
  passwordHash: String,
  cashBalance: BigDecimal
)
```

### 8.2. Stock

```scala
case class Stock(
  symbol: String,
  name: String
)
```

### 8.3. Quote

```scala
case class Quote(
  symbol: String,
  price: BigDecimal,
  fetchedAt: java.time.Instant
)
```

### 8.4. Transaction

```scala
enum TransactionSide:
  case Buy, Sell

case class Transaction(
  id: Long,
  userId: Long,
  symbol: String,
  side: TransactionSide,
  quantity: Int,
  price: BigDecimal,
  createdAt: java.time.Instant
)
```

### 8.5. Holding

```scala
case class Holding(
  userId: Long,
  symbol: String,
  quantity: Int,
  averageBuyPrice: BigDecimal
)
```

### 8.6. Portfolio

```scala
case class PortfolioPosition(
  symbol: String,
  quantity: Int,
  averageBuyPrice: BigDecimal,
  currentPrice: BigDecimal,
  currentValue: BigDecimal,
  profitLoss: BigDecimal
)

case class Portfolio(
  userId: Long,
  cashBalance: BigDecimal,
  positions: List[PortfolioPosition],
  totalStockValue: BigDecimal,
  totalAccountValue: BigDecimal
)
```

---
## 9. API – kontrakt endpointów

Kontrakt API powinien być stabilny. Jeśli ktoś zmienia request/response endpointu, musi zaktualizować dokumentację i poinformować zespół.

### 9.1. Auth
#### `POST /api/auth/register`

Request:
```json
{
  "username": "demo",
  "password": "password123"
}
```

Response `201 Created`:
```json
{
  "id": 1,
  "username": "demo",
  "cashBalance": 100000
}
```

Możliwe błędy:
- `400 Bad Request` – niepoprawne dane,
- `409 Conflict` – użytkownik już istnieje.

#### `POST /api/auth/login`

Request:
```json
{
  "username": "demo",
  "password": "password123"
}
```

Response `200 OK`:
```json
{
  "userId": 1,
  "username": "demo"
}
```

Możliwe błędy:
- `401 Unauthorized` — błędny login lub hasło.

#### `POST /api/auth/logout`

Response `200 OK`:
```json
{
  "message": "Logged out"
}
```

### 9.2. Stocks
#### `GET /api/stocks`

Response `200 OK`:
```json
[
  { "symbol": "AAPL", "name": "Apple Inc." },
  { "symbol": "MSFT", "name": "Microsoft Corporation" },
  { "symbol": "GOOGL", "name": "Alphabet Inc." }
]
```

#### `GET /api/stocks/:symbol/quote`

Response `200 OK`:
```json
{
  "symbol": "AAPL",
  "price": 182.45,
  "fetchedAt": "2026-05-10T12:30:00Z"
}
```

Możliwe błędy:
- `404 Not Found` – symbol nie jest obsługiwany,
- `503 Service Unavailable` – problem z zewnętrznym API.

### 9.3. Orders
#### `POST /api/orders/buy`

Request:
```json
{
  "symbol": "AAPL",
  "quantity": 3
}
```

Response `201 Created`:
```json
{
  "transactionId": 10,
  "symbol": "AAPL",
  "side": "BUY",
  "quantity": 3,
  "price": 182.45,
  "total": 547.35,
  "cashBalanceAfter": 99452.65
}
```

Możliwe błędy:
- `400 Bad Request` – ilość mniejsza lub równa zero,
- `400 Bad Request` – niewystarczające środki,
- `404 Not Found` – nieobsługiwany symbol,
- `503 Service Unavailable` – problem z ceną.

#### `POST /api/orders/sell`

Request:
```json
{
  "symbol": "AAPL",
  "quantity": 1
}
```

Response `201 Created`:
```json
{
  "transactionId": 11,
  "symbol": "AAPL",
  "side": "SELL",
  "quantity": 1,
  "price": 185.10,
  "total": 185.10,
  "cashBalanceAfter": 99637.75
}
```

Możliwe błędy:
- `400 Bad Request` – ilość mniejsza lub równa zero,
- `400 Bad Request` – użytkownik nie posiada wystarczającej liczby akcji,
- `404 Not Found` – nieobsługiwany symbol,
- `503 Service Unavailable` – problem z ceną.

### 9.4. Portfolio
#### `GET /api/portfolio`

Response `200 OK`:
```json
{
  "userId": 1,
  "cashBalance": 99452.65,
  "positions": [
    {
      "symbol": "AAPL",
      "quantity": 3,
      "averageBuyPrice": 182.45,
      "currentPrice": 185.10,
      "currentValue": 555.30,
      "profitLoss": 7.95
    }
  ],
  "totalStockValue": 555.30,
  "totalAccountValue": 100007.95
}
```

### 9.5. Transactions
#### `GET /api/transactions`

Response `200 OK`:
```json
[
  {
    "id": 10,
    "symbol": "AAPL",
    "side": "BUY",
    "quantity": 3,
    "price": 182.45,
    "createdAt": "2026-05-10T12:45:00Z"
  }
]
```

### 9.6. Leaderboard
#### `GET /api/leaderboard`

Response `200 OK`:
```json
[
  {
    "rank": 1,
    "username": "demo",
    "totalAccountValue": 100007.95,
    "profitLoss": 7.95
  }
]
```

### 9.7. Healthcheck
#### `GET /api/health`

Response `200 OK`:
```json
{
  "status": "ok"
}
```

---

## 10. Baza danych
### 10.1. Minimalny schemat

#### `users`

| Kolumna         | Typ                     | Opis                       |
| --------------- | ----------------------- | -------------------------- |
| `id`            | BIGINT / AUTO_INCREMENT | ID użytkownika             |
| `username`      | VARCHAR                 | unikalna nazwa użytkownika |
| `password_hash` | VARCHAR                 | hash hasła                 |
| `cash_balance`  | DECIMAL                 | saldo użytkownika          |
| `created_at`    | TIMESTAMP               | data utworzenia            |

#### `transactions`

|Kolumna|Typ|Opis|
|---|---|---|
|`id`|BIGINT / AUTO_INCREMENT|ID transakcji|
|`user_id`|BIGINT|właściciel transakcji|
|`symbol`|VARCHAR|symbol akcji|
|`side`|VARCHAR|BUY lub SELL|
|`quantity`|INT|liczba akcji|
|`price`|DECIMAL|cena jednostkowa|
|`created_at`|TIMESTAMP|data transakcji|

#### `holdings`

|Kolumna|Typ|Opis|
|---|---|---|
|`user_id`|BIGINT|właściciel pozycji|
|`symbol`|VARCHAR|symbol akcji|
|`quantity`|INT|liczba posiadanych akcji|
|`average_buy_price`|DECIMAL|średnia cena zakupu|

#### `price_cache`

|Kolumna|Typ|Opis|
|---|---|---|
|`symbol`|VARCHAR|symbol akcji|
|`price`|DECIMAL|ostatnio pobrana cena|
|`fetched_at`|TIMESTAMP|czas pobrania|

### 10.2. Zasady pracy z bazą
- Zmiany schematu bazy robimy przez evolutions/migracje.
- Nie zmieniamy ręcznie lokalnej bazy bez zapisania zmian w repo.
- Każda zmiana w schemacie musi być opisana w PR.
- Dane demo/seedy powinny być deterministyczne.
- Nie commitujemy plików lokalnej bazy, jeśli są generowane przez aplikację.

---

## 11. Zewnętrzne API giełdowe
### 11.1. Wymagania

Integracja z API powinna być ukryta za interfejsem:
```scala
trait MarketDataService {
  def getQuote(symbol: String): Future[Either[MarketDataError, Quote]]
}
```

Dzięki temu reszta aplikacji nie musi wiedzieć, czy cena pochodzi z Finnhub, mocka, cache czy innego źródła.

### 11.2. Implementacje

W projekcie powinny istnieć co najmniej dwie implementacje:
```text
MarketDataService
  ├── FinnhubMarketDataService
  └── MockMarketDataService
```

### 11.3. Zasady bezpieczeństwa API key
- Nie commitujemy kluczy API do repozytorium.
- Klucze API przechowujemy w zmiennych środowiskowych lub lokalnym pliku konfiguracyjnym ignorowanym przez Git.
- W README opisujemy, jak ustawić klucz lokalnie.
- Demo powinno działać także z mockiem, gdy API nie działa lub limit zapytań się skończy.

Przykład zmiennej środowiskowej:
```bash
export FINNHUB_API_KEY="your_api_key_here"
```

### 11.4. Cache cen

Cache jest potrzebny, żeby:
- ograniczyć liczbę zapytań do API,
- uniknąć przekroczenia limitów,
- przyspieszyć aplikację,
- ustabilizować demo.

Proponowana zasada:
```text
Jeśli cena dla symbolu została pobrana mniej niż 30–60 sekund temu, używamy ceny z cache.
W przeciwnym razie pobieramy nową cenę z API.
```

---

## 12. Zasady pracy z Git
### 12.1. Główne branche

#### `main`
- stabilna wersja projektu,
- kod na `main` powinien być wersją możliwą do pokazania,
- nie commitujemy bezpośrednio na `main`,
- do `main` mergujemy tylko większe, stabilne wersje z `develop`.

#### `develop`
- główna gałąź robocza zespołu,
- feature branche wychodzą z `develop`,
- większość Pull Requestów trafia do `develop`,
- `develop` powinien regularnie się kompilować i przechodzić testy.

### 12.2. Nazewnictwo branchy

Format:
```text
<type>/<short-description>
```

Przykłady:
```text
feature/auth-register
feature/finnhub-market-data
feature/buy-order
feature/portfolio-view
fix/login-validation
fix/price-cache-expiration
refactor/trading-service
 docs/update-api-docs
```

Dozwolone typy:

| Typ        | Kiedy używać                          |
| ---------- | ------------------------------------- |
| `feature`  | nowa funkcjonalność                   |
| `fix`      | poprawka błędu                        |
| `refactor` | zmiana struktury bez zmiany działania |
| `docs`     | dokumentacja                          |
| `test`     | testy                                 |
| `chore`    | konfiguracja, build, porządki         |

### 12.3. Commity

Stosujemy krótkie, czytelne komunikaty commitów.

Format:
```text
<type>: <short description>
```

Przykłady:
```text
feature: add user registration endpoint
feature: implement Finnhub quote client
fix: reject buy order when cash is insufficient
test: add TradingService buy order tests
docs: describe local setup in README
chore: add GitHub Actions workflow
```

Zasady:
- jeden commit powinien dotyczyć jednego logicznego tematu,
- nie commitujemy losowych zmian z IDE,
- przed commitem sprawdzamy `git diff`,
- nie commitujemy sekretów, kluczy API ani lokalnych plików konfiguracyjnych.

### 12.4. Aktualizowanie brancha

Przed rozpoczęciem pracy:
```bash
git checkout develop  
git pull origin develop  
git checkout -b feature/my-task
```

Przed wystawieniem PR:
```bash
git checkout develop
git pull origin develop
git checkout feature/my-task
git merge develop
sbt test
```

Jeżeli zmiana jest bardzo mała, np. literówka w README, można ją zrobić szybciej, ale większy kod aplikacji powinien iść przez osobny branch.

### 12.5. Konflikty  
  
Jeśli pojawi się konflikt:  
1. Sprawdzamy, czego dotyczy konflikt.  
2. Jeśli konflikt dotyczy kodu innej osoby, najlepiej szybko ją zapytać.  
3. Po rozwiązaniu konfliktu uruchamiamy przynajmniej:

```bash
sbt compile
```

---

## 13. Zadania w YouTracku

YouTrack ma nam pomóc zobaczyć:  
- co jest aktualnie do zrobienia,  
- kto czym się zajmuje,  
- co jest najważniejsze przed najbliższą prezentacją,  
- które zadania są zablokowane albo wymagają decyzji.  
  
Nie potrzebujemy bardzo formalnego procesu ani rozbudowanych statusów. Wystarczy, żeby task miał:  
- krótki tytuł,  
- osobę odpowiedzialną,  
- krótki opis, co trzeba zrobić,  
- ewentualnie link do brancha lub PR.

 Przykładowe taski:
```text
Implement user registration  
Add login endpoint  
Create MarketDataService interface  
Connect Finnhub quote API  
Implement buy order logic  
Create portfolio view  
Add leaderboard endpoint  
Connect leaderboard UI
```

Jeżeli zadanie robi się zbyt duże, dzielimy je na mniejsze.

---

## 14. Pull Requesty  
  
Pull Requesty służą głównie temu, żeby nie psuć sobie nawzajem kodu na `develop`.  
  
Nie potrzebujemy rozbudowanego template'u PR. Wystarczy, żeby w opisie PR znalazło się:  
- co zostało zmienione,  
- jak to najprościej sprawdzić,  
- czy zmiana dotyczy backendu, frontendu, bazy, testów lub konfiguracji.

Przykład opisu PR:
```md  
## Co zmienia ten PR?  
  
Dodaje endpoint pobierania aktualnej ceny akcji z MarketDataService.  
  
## Jak sprawdzić?  
  
1. Uruchomić aplikację:  
`sbt run`  
  
2. Wejść na:  
`GET /api/stocks/AAPL/quote`  
  
3. Sprawdzić, czy zwraca JSON z ceną.
```

---

## 15. Standardy kodu Scala
### 15.1. Ogólne zasady
- Preferujemy `val` zamiast `var`.
- Preferujemy niemutowalne kolekcje.
- Używamy `case class` dla prostych modeli danych.
- Używamy `enum` lub `sealed trait` dla ograniczonego zbioru wartości.
- Nie używamy `null` w logice aplikacji.
- Używamy `Option`, `Either` albo własnych typów błędów.
- Unikamy rzucania wyjątków dla normalnych błędów domenowych.
- Funkcje powinny być możliwie krótkie i czytelne.
- Nazwy powinny opisywać intencję.

### 15.2. Przykład dobrego modelowania błędów

Zamiast:
```scala
throw new RuntimeException("Not enough cash")
```

Preferujemy:
```scala
sealed trait TradingError
object TradingError {
  case object InsufficientCash extends TradingError
  case object InsufficientHoldings extends TradingError
  case object InvalidQuantity extends TradingError
  case class UnsupportedSymbol(symbol: String) extends TradingError
}
```

A w serwisie:
```scala
def buy(userId: Long, symbol: String, quantity: Int): Future[Either[TradingError, Transaction]]
```

### 15.3. Controllers nie zawierają logiki biznesowej

Źle:
```scala
class OrderController(...) extends Controller {
  def buy = Action.async(parse.json) { request =>
    // Parsowanie requestu
    // Pobranie ceny
    // Sprawdzenie salda
    // Aktualizacja bazy
    // Wyliczanie portfolio
    // Tworzenie odpowiedzi
  }
}
```

Dobrze:
```scala
class OrderController(tradingService: TradingService, cc: ControllerComponents)
  extends AbstractController(cc) {

  def buy = Action.async(parse.json) { request =>
    // Parsowanie requestu
    // Wywołanie tradingService.buy(...)
    // Zamiana wyniku na HTTP response
  }
}
```

### 15.4. BigDecimal dla pieniędzy

Do kwot pieniędzy używamy `BigDecimal`, nie `Double`.

Źle:
```scala
val price: Double = 182.45
```

Dobrze:
```scala
val price: BigDecimal = BigDecimal("182.45")
```

### 15.5. Komentarze

Komentarze powinny wyjaśniać **dlaczego** coś robimy, a nie oczywiste **co** robi kod.

Dobry komentarz:
```scala
// We cache prices to avoid hitting the external API rate limit during demo.
```

Słaby komentarz:
```scala
// Add one to counter.
```

### 15.6. Formatowanie
- Trzymamy jednolity styl formatowania.
- Jeśli dodamy Scalafmt, każdy powinien go używać.
- Nie formatujemy całego projektu przy okazji małego PR, jeśli nie jest to osobne issue.

---

## 16. Obsługa błędów

### 16.1. Kategorie błędów

|Kategoria|Przykład|HTTP|
|---|---|---|
|Błąd walidacji|ilość akcji <= 0|400|
|Błąd domenowy|brak środków|400|
|Brak zasobu|nieznany symbol|404|
|Brak autoryzacji|użytkownik niezalogowany|401|
|Konflikt|username zajęty|409|
|Błąd zewnętrznego API|Finnhub niedostępny|503|
|Błąd serwera|nieoczekiwany wyjątek|500|

### 16.2. Format odpowiedzi błędu

```json
{
  "error": "INSUFFICIENT_CASH",
  "message": "User does not have enough cash to complete this order."
}
```

### 16.3. Zasada

Frontend powinien dostawać czytelny komunikat, a backend powinien zachować szczegóły techniczne w logach.

---

## 17. Testy
### 17.1. Co testujemy obowiązkowo?

Obowiązkowo testujemy logikę, która może łatwo się popsuć:
- kupno akcji,
- sprzedaż akcji,
- brak środków,
- brak wystarczających holdingów,
- niepoprawna ilość,    
- obliczanie średniej ceny zakupu,
- obliczanie wartości portfolio,
- ranking,
- cache cen,
- mock API.

### 17.2. Typy testów
#### Unit tests

Testują pojedynczy serwis lub funkcję.

Przykład:
```text
TradingService should reject buy order when user has insufficient cash
```

#### Integration tests

Testują kilka komponentów razem, np. controller + service + testowa baza.

#### Manual demo tests

Scenariusze ręczne używane przed prezentacją.

### 17.3. Nazewnictwo testów

Test powinien jasno mówić, co sprawdza.

Przykłady:
```text
buy should create transaction when user has enough cash
buy should decrease cash balance after successful order
buy should reject order when quantity is zero
sell should reject order when user does not own enough shares
portfolio should calculate total account value using current prices
leaderboard should sort users by total account value descending
```

### 17.4. Uruchamianie testów

```bash
sbt test
```

Przed każdym PR autor powinien lokalnie uruchomić:

```bash
sbt compile
sbt test
```

### 17.5. Mockowanie API

Testy nie powinny zależeć od prawdziwego zewnętrznego API.

W testach używamy `MockMarketDataService`, np.:

```scala
class MockMarketDataService(prices: Map[String, BigDecimal]) extends MarketDataService {
  override def getQuote(symbol: String): Future[Either[MarketDataError, Quote]] = ???
}
```

---

## 18. Konfiguracja lokalna
### 18.1. Wymagania
- JDK zgodne z projektem,
- sbt,
- Git,
- IntelliJ IDEA z pluginem Scala albo inne IDE,
- opcjonalnie konto/API key w Finnhub.

### 18.2. Klonowanie repo

```bash
git clone https://github.com/myszon10/Stock-Market-Simulator.git
cd Stock-Market-Simulator
```

### 18.3. Uruchomienie aplikacji

```bash
sbt run
```

Domyślnie aplikacja powinna być dostępna pod adresem:

```text
http://localhost:9000
```

### 18.4. Zmienne środowiskowe

Przykład:
```bash
export FINNHUB_API_KEY="your_api_key_here"
export MARKET_DATA_MODE="finnhub"
```

Tryb mock:
```bash
export MARKET_DATA_MODE="mock"
```

### 18.5. Tryb demo

Na potrzeby prezentacji aplikacja powinna mieć stabilny tryb demo:
- testowi użytkownicy,
- przewidywalne ceny mockowane,
- brak zależności od limitów API,
- prosta ścieżka pokazania kupna/sprzedaży.

---

## 19. GitHub Actions / CI  
  
CI powinno uruchamiać się przynajmniej na Pull Requestach do `develop` oraz przy pushach do `develop` i `main`.  
  
Minimalne kroki:  
- checkout kodu,  
- setup JDK,  
- cache zależności sbt,  
- `sbt compile`,  
- `sbt test`.  
  
Jeśli CI nie przechodzi, nie mergujemy większych zmian, dopóki nie wiadomo, co się zepsuło

---

## 20. Dokumentacja w repozytorium

### 20.1. Minimalne pliki dokumentacji

Repo powinno zawierać:
```text
README.md
PROJECT_GUIDE.md
```

Opcjonalnie, jeśli faktycznie będzie potrzebne:
```text
docs/API.md
docs/ARCHITECTURE.md
docs/DEMO_SCRIPT.md
```

### 20.2. README.md powinno zawierać
- nazwę projektu,
- opis projektu,
- skład zespołu,
- stack technologiczny,
- instrukcję uruchomienia,
- instrukcję testów,
- opis konfiguracji API key,
- link do dokumentacji API,
- krótki opis architektury,
- status projektu.

### 20.3. PROJECT_GUIDE.md powinien zawierać

Ten dokument lub jego skróconą wersję.

---

## 21. Prezentacja postępów – 15 maja

### 21.1. Cel prezentacji

Pokazać, że projekt jest dobrze zaplanowany, zespół pracuje równolegle, a podstawowy flow aplikacji już działa.

### 21.2. Proponowana struktura 10-minutowej prezentacji

|Czas|Temat|
|--:|---|
|1 min|Cel projektu|
|1 min|Zakres MVP|
|2 min|Architektura i stack|
|3 min|Demo: login, akcje, cena, kupno, historia|
|1 min|Co już działa|
|1 min|Problemy i decyzje techniczne|
|1 min|Plan do finalnej prezentacji|

### 21.3. Minimalne demo na 15 maja

Scenariusz:
1. Otwieramy aplikację.
2. Logujemy się jako demo user.
3. Pokazujemy saldo startowe.
4. Pokazujemy listę akcji.
5. Pobieramy aktualną cenę AAPL/MSFT.
6. Kupujemy kilka akcji.
7. Pokazujemy zmniejszone saldo.
8. Pokazujemy pozycję w portfolio.
9. Pokazujemy wpis w historii transakcji.
10. Omawiamy, co będzie dalej.

### 21.4. Backup demo

Przed prezentacją przygotowujemy:
- screenshoty głównych ekranów,
- dane demo,
- tryb mock API,
- krótkie nagranie działania aplikacji.

---

## 22. Finalna prezentacja
### 22.1. Proponowana struktura 15–20 minut

|    Czas | Temat                     |
| ------: | ------------------------- |
|   2 min | Cel projektu              |
|   2 min | Stack technologiczny      |
|   3 min | Architektura backendu     |
|   2 min | Model danych              |
| 5–7 min | Demo aplikacji            |
|   2 min | Testy i jakość kodu       |
|   2 min | Podział pracy             |
| 1–2 min | Problemy i możliwy rozwój |

### 22.2. Finalny scenariusz demo
1. Logowanie.
2. Pokazanie salda.
3. Pobranie cen akcji.
4. Kupno akcji.
5. Portfolio po zakupie.
6. Sprzedaż części akcji.
7. Historia transakcji.
8. Ranking.
9. Krótkie pokazanie testów/CI.

---

## 23. Definition of Done  
  
Task uznajemy za skończony, gdy:  
- kod jest zmergowany do `develop`,  
- aplikacja się kompiluje,  
- główna funkcja działa lokalnie,  
- nie dodaliśmy sekretów ani lokalnych plików,  
- jeśli zmiana dotyczy logiki biznesowej, istnieje przynajmniej podstawowy test albo ręcznie sprawdzony scenariusz,  
- jeśli zmiana wpływa na uruchamianie projektu lub API, README/dokumentacja zostały zaktualizowane.  
  
Nie traktujemy tej listy jako biurokracji. Chodzi o to, żeby po zakończeniu taska inna osoba mogła normalnie pracować dalej.

---

## 24. Minimalna koordynacja zespołu  
  
Nie wprowadzamy formalnych daily ani rozbudowanego procesu komunikacji.  
  
Wystarczą trzy zasady:  
- jeśli zaczynasz większe zadanie, przypisz się do taska w YouTracku,  
- jeśli jesteś zablokowany, napisz do zespołu zamiast czekać kilka dni,  
- jeśli zmieniasz kontrakt API, strukturę bazy albo coś, co może zepsuć pracę innych osób, daj znać na wspólnym czacie.  
  
Komunikacja ma być lekka i praktyczna.

---

## 25. Priorytety projektu

Jeżeli brakuje czasu, priorytety są następujące:
1. Działający backend.
2. Działające kupno/sprzedaż.
3. Poprawne portfolio.
4. Historia transakcji.
5. Prosty, ale funkcjonalny UI.
6. Ranking.
7. Testy krytycznej logiki.
8. Dokumentacja.
9. Estetyka UI.
10. Funkcje opcjonalne.

Nie poświęcamy działania MVP dla funkcji opcjonalnych.

---

## 26. Ryzyka i plan awaryjny

### 26.1. API giełdowe nie działa lub ma limit

Plan:
- używamy `MockMarketDataService`,
- przygotowujemy tryb demo,
- cache'ujemy ceny,
- nie opieramy prezentacji wyłącznie na zewnętrznym API.

### 26.2. UI nie będzie gotowe na czas

Plan:
- backend testujemy przez Postmana/curl,
- robimy minimalne widoki tylko dla głównego flow,
- nie dopieszczamy CSS przed działaniem funkcji.

### 26.3. Konflikty w kodzie

Plan:
- pracujemy na małych branchach wychodzących z `develop`,
- regularnie pullujemy aktualny `develop`,
- większe zmiany wrzucamy przez PR,
- przy konflikcie w cudzym kodzie pytamy osobę, która ostatnio pracowała nad danym fragmentem.

### 26.4. Zbyt duży zakres

Plan:
- trzymamy się MVP,
- funkcje opcjonalne tylko po zakończeniu podstaw,
- duże issue dzielimy na mniejsze.
### 26.5. Brak testów

Plan:
- testy piszemy równolegle z logiką,
- minimum testów dla `TradingService`, `PortfolioService`, `LeaderboardService`,
- nie odkładamy wszystkich testów na ostatni dzień.

---

## 27. Przykładowe zadania startowe

### Setup / DevOps / repository  
- Initialize Play Framework project  
- Prepare basic repository structure  
- Create and configure `develop` branch  
- Configure sbt dependencies  
- Add `.gitignore`  
- Add GitHub Actions workflow  
- Add basic README  
- Add project guide

### Auth  
- Create `User` model  
- Create users table  
- Implement `UserRepository`  
- Implement registration endpoint  
- Implement login endpoint  
- Add password hashing  
- Add basic auth validation  
- Add auth tests

### Database
- Create initial database schema
- Add evolutions/migrations
- Create `transactions` table
- Create `holdings` table
- Create `price_cache` table
- Implement basic repositories
- Add demo seed data if needed

### Market data
- Define `MarketDataService`
- Implement `MockMarketDataService`
- Implement `FinnhubMarketDataService`
- Add quote cache
- Add `/api/stocks` endpoint
- Add `/api/stocks/:symbol/quote` endpoint
- Add API key configuration without committing secrets

### Trading
-  Create `Transaction` model
-  Create `Holding` model
-  Implement buy logic
-  Implement sell logic
-  Add insufficient cash validation
-  Add insufficient holdings validation
-  Add transaction history

### Portfolio
-  Implement portfolio calculation
-  Calculate stock value
-  Calculate total account value
-  Calculate profit/loss
-  Add portfolio endpoint
-  Add portfolio UI

### Leaderboard
-  Implement leaderboard calculation
-  Add leaderboard endpoint
-  Add leaderboard UI

### Testing / polish / helper tasks
- Add TradingService tests
- Add PortfolioService tests
- Add LeaderboardService tests
- Add basic API examples to README
- Add demo seed data
- Prepare simple demo flow for progress presentation

---

## 28. Minimalne standardy jakości

Projekt powinien spełniać następujące minimum:
- aplikacja uruchamia się lokalnie zgodnie z README,
- backend jest napisany w Scali,
- aplikacja ma warstwową strukturę,
- taski są planowane w YouTracku,
- większe zmiany trafiają przez Pull Request do `develop`,
- istnieje podział pracy,
- istnieją testy najważniejszej logiki,
- aplikacja ma podstawową dokumentację uruchomienia,
- demo działa bez ręcznego poprawiania bazy/kodu,
- klucze API nie są commitowane,
- każda osoba ma widoczny wkład w repo.

---

## 29. Słownik pojęć

|Pojęcie|Znaczenie|
|---|---|
|`Quote`|aktualna cena akcji pobrana z API lub mocka|
|`Holding`|liczba akcji danego symbolu posiadana przez użytkownika|
|`Transaction`|zapis kupna lub sprzedaży|
|`Portfolio`|saldo + posiadane akcje + wycena|
|`Cash balance`|dostępna gotówka użytkownika|
|`Average buy price`|średnia cena zakupu danej akcji|
|`Total account value`|gotówka + aktualna wartość akcji|
|`MarketDataService`|serwis odpowiedzialny za pobieranie cen|
|`Mock`|sztuczna implementacja używana w testach/demo|
|`MVP`|minimalna wersja produktu potrzebna do zaliczenia|

---

## 30. Decyzje techniczne

Tę sekcję aktualizujemy, gdy zespół podejmie ważną decyzję.

|Data|Decyzja|Powód|
|---|---|---|
|29.04|Backend w Scali|wymaganie projektu|
|29.04|Play Framework|prosty wybór do aplikacji webowej w Scali|
|29.04|Ograniczona lista symboli|mniejszy zakres, stabilniejsze demo|
|29.04|Mock API jako obowiązkowy element|zabezpieczenie przed awarią/limitem API|
|29.04|BigDecimal dla pieniędzy|uniknięcie błędów typowych dla Double|

---

## 31. Krótka zasada końcowa

Najpierw dowozimy stabilne MVP, potem poprawiamy wygląd i dodajemy funkcje opcjonalne.

Działający prosty projekt jest lepszy niż ambitny projekt, którego nie da się zaprezentować.