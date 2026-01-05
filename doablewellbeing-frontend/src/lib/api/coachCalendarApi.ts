import { CoachCalendarEventView } from "@/app/components/coach/Overview/types";
import { apiFetch } from "./apiFetch";



export async function getCoachCalendar(fromIso: string, toIso: string) {
  const params = new URLSearchParams({ from: fromIso, to: toIso }).toString();
  return apiFetch<CoachCalendarEventView[]>(`/api/coaches/me/calendar?${params}`);
}

export async function confirmAppointment(id: string) {
  return apiFetch(`/api/coaches/me/appointments/${id}/confirm`, { method: "PATCH" });
}
export async function declineAppointment(id: string, reason: string) {
  return apiFetch(`/api/coaches/me/appointments/${id}/decline`, {
    method: "PATCH",
    body: JSON.stringify({ reason }),
  });
}
export async function completeAppointment(id: string) {
  return apiFetch(`/api/coaches/me/appointments/${id}/complete`, { method: "PATCH" });
}
export async function cancelAppointment(id: string) {
  return apiFetch(`/api/coaches/me/appointments/${id}/cancel`, { method: "PATCH" });
}
