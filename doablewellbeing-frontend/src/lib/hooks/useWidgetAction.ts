"use client";

import { useState } from "react";
import { apiFetch } from "@/lib/api/apiFetch";

export function useWidgetAction() {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function run<T>(fn: () => Promise<T>) {
    setBusy(true);
    setError(null);
    try {
      return await fn();
    } catch (e: any) {
      setError(e?.message ?? "Action failed");
      throw e;
    } finally {
      setBusy(false);
    }
  }

  async function postJson<T>(path: string, body: any): Promise<T> {
    return run(() =>
      apiFetch<T>(path, { method: "POST", body: JSON.stringify(body) })
    );
  }

  async function postNoBody<T>(path: string): Promise<T> {
    return run(() =>
      apiFetch<T>(path, { method: "POST" })
    );
  }

  return { busy, error, run, postJson, postNoBody };
}
