"use client";

import { useCallback, useEffect, useState } from "react";
import { apiFetch } from "@/lib/api/apiFetch";

export function useWidgetData<T>(widgetId: string) {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const reload = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const d = await apiFetch<T>(`/api/widgets/${widgetId}/data`);
      setData(d);
    } catch (e: any) {
      setError(e?.message ?? "Failed to load widget data");
      setData(null);
    } finally {
      setLoading(false);
    }
  }, [widgetId]);

  useEffect(() => { void reload(); }, [reload]);

  return { data, loading, error, reload };
}
