# Stock-Market-Simulator
Stock market simulator built as a group project for the Scala programming course.

The application allows users to simulate investing in stocks using virtual money. It focuses on a basic portfolio flow: user account, starting balance, stock prices, buy/sell orders, portfolio, transaction history and leaderboard.


## Team
- Szymon Mażulis
- Bartosz Radomski
- Igor Oleksy
- Maciej Rozpędek
- Jakub Warkocki

## Tech stack
- Scala
- Play Framework
- sbt
- PostgreSQL
- React/Vite frontend

## Project structure
```text
backend/ - Scala Play backend
frontend/stock-frontend/ - React frontend
```

## Backend setup
Start the database:
```bash
cd backend
docker compose up -d
```

Run the backend:
```bash
sbt run
```

The backend runs on:
```
http://localhost:9000
```

Default database configuration:
```
DB_URL=jdbc:postgresql://localhost:5432/stock_market
DB_USER=simulator_user
DB_PASSWORD=simulator_pass
```

## Market data mode
The backend supports two market data modes:
```
mock
finnhub
```

The mode is configured with `MARKET_DATA_MODE`

Mock mode is the default and does not require an API key. It is recommended for local development and demo presentations.

PowerShell:
```PowerShell
$env:MARKET_DATA_MODE="mock"
```
Bash:
```bash
export MARKET_DATA_MODE="mock"
sbt run
```

Finnhub mode requires an API key:
Powershell:
```PowerShell
$env:MARKET_DATA_MODE="finnhub"
$env:FINNHUB_API_KEY="your_api_key_here"
sbt run
```
Bash:
```bash
export MARKET_DATA_MODE="finnhub"
export FINNHUB_API_KEY="your_api_key_here"
sbt run
```

## Frontend setup

```bash
cd frontend/stock-frontend
npm install
npm run dev
```

## Tests
Run all backend tests:
```bash
cd backend
sbt test
```

## Status
Project is currently in development