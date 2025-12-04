import { useEffect, useState } from "react";
import { getMyAppointments, AppointmentView } from "../api/appointmentApi";

export function useMyAppointments() {
  const [appointments, setAppointments] = useState<AppointmentView[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function load() {
    try {
      setLoading(true);
      const data = await getMyAppointments();
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

  return {
    appointments,
    loading,
    error,
    refresh: load,
  };
}
