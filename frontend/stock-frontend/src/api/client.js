export class ApiError extends Error {
  constructor(status, code, message) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
  }
}

const parseBody = async (response) => {
  const text = await response.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
};

const buildError = (response, body) => {
  if (body && typeof body === 'object') {
    let code = null;
    if (body.error) {
      code = body.error;
    }
    let message = body.message;
    if (!message) {
      message = response.statusText;
    }
    if (!message) {
      message = 'Request failed';
    }
    return new ApiError(response.status, code, message);
  }

  let message = '';
  if (typeof body === 'string' && body.length > 0) {
    message = body;
  } else if (response.statusText) {
    message = response.statusText;
  } else {
    message = 'Request failed';
  }
  return new ApiError(response.status, null, message);
};

export const request = async (path, options = {}) => {
  const { body, headers, ...rest } = options;
  const init = {
    credentials: 'include',
    ...rest,
    headers: { Accept: 'application/json', ...(headers || {}) },
  };

  if (body !== undefined && body !== null) {
    if (typeof body === 'string') {
      init.body = body;
    } else {
      init.body = JSON.stringify(body);
    }
    if (!init.headers['Content-Type']) {
      init.headers['Content-Type'] = 'application/json';
    }
  }

  let response;
  try {
    response = await fetch(path, init);
  } catch (err) {
    let message = 'Network error';
    if (err && err.message) {
      message = err.message;
    }
    throw new ApiError(0, 'NETWORK_ERROR', message);
  }

  const payload = await parseBody(response);
  if (!response.ok) {
    throw buildError(response, payload);
  }
  return payload;
};

export const get = (path, options) => request(path, { ...options, method: 'GET' });

export const post = (path, body, options) =>
  request(path, { ...options, method: 'POST', body });

export const del = (path, options) => request(path, { ...options, method: 'DELETE' });

export default { request, get, post, del, ApiError };
