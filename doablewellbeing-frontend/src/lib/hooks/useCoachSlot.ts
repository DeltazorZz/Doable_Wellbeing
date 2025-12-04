import { useEffect, useState } from "react";
import { getCoachSlots, SlotView } from "../api/coachAvailabilityApi";

export function useCoachSlots(coachId: string, from: string, to: string) {
  const [slots, setSlots] = useState<SlotView[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function load() {
    try {
      setLoading(true);
      const data = await getCoachSlots(coachId, from, to);
      setSlots(data);
    } catch (err: any) {
      setError(err.message ?? "Unknown error");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, [coachId, from, to]);

  return { slots, loading, error, refresh: load };
}
