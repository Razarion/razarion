// Every platform here reports failures as a JSON body alongside a 4xx, so the status code on its
// own is never enough to debug with. These wrappers always carry the body into the thrown error.

export class HttpError extends Error {
  constructor(status, body, url) {
    const detail = typeof body === 'string' ? body : JSON.stringify(body, null, 2);
    super(`HTTP ${status} from ${url}\n${detail}`);
    this.name = 'HttpError';
    this.status = status;
    this.body = body;
    this.url = url;
  }
}

async function readBody(res) {
  const text = await res.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

export async function request(url, options = {}) {
  const res = await fetch(url, options);
  const body = await readBody(res);
  if (!res.ok) throw new HttpError(res.status, body, url);
  return { body, headers: res.headers, status: res.status };
}

export async function getJson(url, options = {}) {
  return (await request(url, { ...options, method: 'GET' })).body;
}

export async function postJson(url, payload, options = {}) {
  return (
    await request(url, {
      ...options,
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
      body: JSON.stringify(payload),
    })
  ).body;
}

export async function postForm(url, fields, options = {}) {
  return (
    await request(url, {
      ...options,
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded', ...(options.headers || {}) },
      body: new URLSearchParams(fields).toString(),
    })
  ).body;
}

export const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
