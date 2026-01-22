function getCsrfToken(): string | null {
  if (typeof document !== "undefined") {
    const match = document.cookie.match(
      new RegExp("(^|; )" + "XSRF-TOKEN".replace(/([.$?*|{}()[\]\\/+^])/g, "\\$1") + "=([^;]*)")
    );
    if (match) return decodeURIComponent(match[2]);
  }

  if (typeof window !== "undefined") {
    const stored = window.localStorage.getItem("csrfToken");
    if (stored) return stored;
  }

  return null;
}

type ApiFetchResult<T> =
  | { ok: true; status: number; data: T }
  | { ok: false; status: number; data: null; errorText?: string };

export async function apiFetch<T>(
  path: string,
  options: RequestInit = {}
): Promise<ApiFetchResult<T>> {
  const baseURL = process.env.NEXT_PUBLIC_API_BASE_URL || "";

  const headers: HeadersInit = {
    "Content-Type": "application/json",
    Accept: "application/json",
    ...(options.headers || {}),
  };

  // CSRF csak state-módosító kéréseknél
  const method = (options.method || "GET").toUpperCase();
  if (method !== "GET" && method !== "HEAD" && method !== "OPTIONS") {
    const csrfToken = getCsrfToken();
    if (csrfToken) (headers as any)["X-XSRF-TOKEN"] = csrfToken;
  }

  const res = await fetch(`${baseURL}${path}`, {
    ...options,
    credentials: "include",
    headers,
  });

  // ✅ 401/403: ne throw, hanem kontrollált "not ok"
  if (res.status === 401 || res.status === 403) {
    return { ok: false, status: res.status, data: null };
  }

  // Egyéb hibák: maradhat throw (ezek tényleg bugok E2E-ben is)
  if (!res.ok) {
    let text = "";
    try {
      text = await res.text();
    } catch {}
    throw new Error(`API error: ${res.status} ${res.statusText} - ${text || "no details"}`);
  }

  if (res.status === 204) {
    // @ts-expect-error intentional
    return { ok: true, status: 204, data: undefined };
  }

  return { ok: true, status: res.status, data: (await res.json()) as T };
}
