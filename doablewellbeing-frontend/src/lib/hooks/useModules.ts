"use client";

import { useEffect, useState } from "react";
import type { ModuleView } from "@/types/dashboard";
import { fetchModules } from "@/lib/api/dashboardApi";

export function useModules() {
  const [modules, setModules] = useState<ModuleView[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        setModules(await fetchModules());
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  return { modules, loading };
}
