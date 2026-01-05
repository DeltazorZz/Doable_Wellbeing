"use client";

import { useEffect } from "react";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";

export async function initCsrfToken() {
  try {
    const res = await fetch(`${API_BASE}/csrf-token`, {
      method: "GET",
      credentials: "include",
      headers: { Accept: "application/json" },
    });

    if (!res.ok) {
      console.error("CSRF init failed, status:", res.status);
      return;
    }

    const data: {
      headerName: string;
      parameterName: string;
      token: string;
    } = await res.json();

    // 1) Cookie – JS-ből beállítva
    document.cookie = `XSRF-TOKEN=${encodeURIComponent(
      data.token
    )}; Path=/; SameSite=Strict`;

    // 2) localStorage fallback
    if (typeof window !== "undefined") {
      window.localStorage.setItem("csrfToken", data.token);
    }

    console.log("CSRF token initialised:", data.token);
  } catch (e) {
    console.error("Failed to init CSRF token", e);
  }
}

export function CsrfBootstrap() {
  useEffect(() => {
    // app induláskor egyszer lefut
    initCsrfToken();
  }, []);

  return null;
}
