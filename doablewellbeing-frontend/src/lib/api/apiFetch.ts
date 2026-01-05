function getCsrfToken(): string | null {
  if (typeof document !== "undefined") {
    const match = document.cookie.match(
      new RegExp("(^|; )" + "XSRF-TOKEN".replace(/([.$?*|{}()[\]\\/+^])/g, "\\$1") + "=([^;]*)")
    );
    if (match) {
      return decodeURIComponent(match[2]);
    }
  }

  if (typeof window !== "undefined") {
    const stored = window.localStorage.getItem("csrfToken");
    if (stored) return stored;
  }

  return null;
}

export async function apiFetch<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const baseURL = process.env.NEXT_PUBLIC_API_BASE_URL || "";

  const headers: HeadersInit = {
    "Content-Type": "application/json",
    Accept: "application/json",
    ...(options.headers || {}),
  };

  // csak state-módosító kéréseknél kell CSRF header
  const method = (options.method || "GET").toUpperCase();
  if (method !== "GET" && method !== "HEAD" && method !== "OPTIONS") {
    const csrfToken = getCsrfToken();
    console.log("XSRF-TOKEN resolved:", csrfToken);
    if (csrfToken) {
      (headers as any)["X-XSRF-TOKEN"] = csrfToken;
    }
  }

  const res = await fetch(`${baseURL}${path}`, {
    ...options,
    credentials: "include",
    headers,
  });

  if (!res.ok) {
    let text = "";
    try {
      text = await res.text();
    } catch {}
    throw new Error(
      `API error: ${res.status} ${res.statusText} - ${text || "no details"}`
    );
  }

  if (res.status === 204) {
    return undefined as T;
  }

  return (await res.json()) as T;
}
