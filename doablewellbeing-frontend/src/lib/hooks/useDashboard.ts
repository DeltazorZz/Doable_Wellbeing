"use client";

import { useCallback, useEffect, useState } from "react";
import type { DashboardView } from "@/types/dashboard";
import { fetchDefaultDashboard } from "@/lib/api/dashboardApi";

export function useDashboard() {
  const [dashboard, setDashboard] = useState<DashboardView | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const reload = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const d = await fetchDefaultDashboard();
      setDashboard(d);
    } catch (e: any) {
      setError(e?.message ?? "Failed to load dashboard");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { void reload(); }, [reload]);

  return { dashboard, setDashboard, loading, error, reload };
}
