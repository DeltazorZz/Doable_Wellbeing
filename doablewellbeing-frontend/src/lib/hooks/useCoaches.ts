"use client";

import { useEffect, useState } from "react";
import { fetchCoaches, Coach } from "@/lib/api/coachApi";

export function useCoaches() {
  const [coaches, setCoaches] = useState<Coach[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true; 

    (async () => {
      try {
        const list = await fetchCoaches();
        if (active) setCoaches(list);
      } catch (err: any) {
        if (active) setError(err.message ?? "Failed to load coaches");
      } finally {
        if (active) setLoading(false);
      }
    })();

    return () => {
      active = false;
    };
  }, []);

  return { coaches, loading, error };
}
