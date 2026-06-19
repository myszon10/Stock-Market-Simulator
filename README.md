# Stock Market Simulator

A full-stack web application for simulating stock market investments using virtual money. The project was developed as a group assignment for the Scala Programming course at Warsaw University of Technology.

Each user starts with **$100,000** and can build a portfolio by trading stocks, tracking performance and competing with other players on the leaderboard.

## Features

* User registration, login and session-based authentication
* Virtual starting balance of $100,000
* Stock quotes for 10 supported companies
* Buy and sell orders with balance and quantity validation
* Portfolio overview with:

  * available cash
  * current stock value
  * total account value
  * profit and loss
  * average purchase prices
* Complete transaction history
* Leaderboard ranked by total account value
* Mock market data mode for reliable local demos
* Optional live prices from the Finnhub API
* Database-backed market price cache

## Tech Stack

**Backend**

* Scala 3
* Play Framework
* sbt
* PostgreSQL
* Anorm

**Frontend**

* React
* Vite
* JavaScript

**Tools**

* Docker Compose
* GitHub Actions
* ScalaTest

## Project Structure

```
backend/                  Scala and Play backend
frontend/stock-frontend/ React and Vite frontend
```

## Running the Application

### Requirements

* Java and sbt
* Node.js and npm
* Docker with Docker Compose

### 1. Start the database

From the project root:

```
cd backend
docker compose up -d
```

PostgreSQL will be available on port `5433`. Database migrations are applied automatically when the backend starts.

### 2. Start the backend

While still inside the `backend` directory:

```
sbt run
```

The backend will run at:

```
http://localhost:9000
```

By default, the application uses mock market data and does not require an API key.

### 3. Start the frontend

In a separate terminal:

```
cd frontend/stock-frontend
npm install
npm run dev
```

Open the application at:

```
http://localhost:5173
```

## Market Data Configuration

The backend supports two market data modes:

* `mock` – fixed local prices, recommended for development and presentations
* `finnhub` – live market prices retrieved from the Finnhub API

To use live prices, create a `backend/.env` file:

```
MARKET_DATA_MODE=finnhub
FINNHUB_API_KEY=your_api_key
MARKET_DATA_CACHE_TTL_SECONDS=60
```

The `.env` file is ignored by Git and should never be committed.

## Tests

Run the backend test suite from the `backend` directory:

```
sbt test
```

Backend tests are also executed automatically by the GitHub Actions CI pipeline.

## Team

* Szymon Mażulis
* Bartosz Radomski
* Igor Oleksy
* Maciej Rozpędek
* Jakub Warkocki
