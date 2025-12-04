import { useEffect, useState } from "react";
import { getCoachAppointments, AppointmentView } from "../api/appointmentApi";

export function useCoachAppointments() {
  const [appointments, setAppointments] = useState<AppointmentView[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function load() {
    try {
      setLoading(true);
      const data = await getCoachAppointments();
      setAppointments(data);
    } catch (err: any) {
      setError(err.message ?? "Unknown error");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  return { appointments, loading, error, refresh: load };
}
