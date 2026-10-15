const ACCESS_TOKEN_KEY =
  import.meta.env.VITE_ACCESS_TOKEN_KEY;

if (!ACCESS_TOKEN_KEY) {
  throw new Error(
    "A variável VITE_ACCESS_TOKEN_KEY "
      + "não foi configurada."
  );
}

export function saveAccessToken(
  token: string
): void {
  localStorage.setItem(
    ACCESS_TOKEN_KEY,
    token
  );
}

export function getAccessToken():
  string | null {
  return localStorage.getItem(
    ACCESS_TOKEN_KEY
  );
}

export function removeAccessToken():
  void {
  localStorage.removeItem(
    ACCESS_TOKEN_KEY
  );
}