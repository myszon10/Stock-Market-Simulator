import { post, get, ApiError } from './client';

export const loginUser = async (username, password) => {
  let data;
  try {
    data = await post('/api/auth/login', { username, password });
  } catch (err) {
    if (err instanceof ApiError && err.status === 401) {
      throw new ApiError(401, 'INVALID_CREDENTIALS', err.message);
    }
    throw err;
  }
  return {
    userId: data.userId,
    username: data.username,
    cashBalance: data.cashBalance,
  };
};

export const registerUser = async (username, password) => {
  const data = await post('/api/auth/register', { username, password });
  return {
    userId: data.id,
    username: data.username,
    cashBalance: data.cashBalance,
  };
};

export const logoutUser = async () => {
  await post('/api/auth/logout');
};

export const fetchStocks = async () => get('/api/stocks');
