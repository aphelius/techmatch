import type { AuthTokenResponse } from "../types/api";

const SESSION_KEY = "techmatch.session";

export function saveSession(session: AuthTokenResponse) {
  localStorage.setItem(SESSION_KEY, JSON.stringify(session));
}

export function readSession(): AuthTokenResponse | null {
  const raw = localStorage.getItem(SESSION_KEY);
  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as AuthTokenResponse;
  } catch {
    localStorage.removeItem(SESSION_KEY);
    return null;
  }
}

export function getAccessToken() {
  return readSession()?.accessToken ?? null;
}

export function isAuthenticated() {
  return Boolean(getAccessToken());
}

export function logout() {
  localStorage.removeItem(SESSION_KEY);
}
